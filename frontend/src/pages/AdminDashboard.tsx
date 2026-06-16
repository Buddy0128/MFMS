import { useQuery } from '@tanstack/react-query';
import { Users, Wallet, TrendingUp, PiggyBank, HandCoins, AlertCircle, CalendarDays } from 'lucide-react';
import { dashboardApi } from '../services/services';
import StatCard from '../components/StatCard';
import LoadingSpinner from '../components/LoadingSpinner';
import Badge from '../components/Badge';
import { ContributionChart, InterestChart, LoanDistributionChart } from '../components/Charts';
import { formatCurrency, formatDate } from '../utils/format';
import { useLanguage } from '../contexts/LanguageContext';
import { translateAction, translateDescription } from '../utils/translateActivity';

export default function AdminDashboard() {
  const { language, t } = useLanguage();
  const { data: dashboard, isLoading } = useQuery({
    queryKey: ['admin-dashboard'],
    queryFn: () => dashboardApi.admin().then(r => r.data),
  });

  if (isLoading) return <LoadingSpinner fullPage />;
  if (!dashboard) return null;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-dark-900">{t('admin.dashboard')}</h1>
        <p className="text-dark-500 text-sm mt-1">{t('fund.overview')}</p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard title={t('total.members')} value={dashboard.totalMembers} icon={<Users size={20} />} color="green" currency={false} />
        <StatCard title={t('total.deposits')} value={dashboard.totalContributions} icon={<Wallet size={20} />} color="blue" />
        <StatCard title={t('pending.deposits')} value={dashboard.totalPendingContributionAmount} icon={<AlertCircle size={20} />} color="orange" />
        <StatCard title={t('interest.earned')} value={dashboard.totalInterestEarned} icon={<TrendingUp size={20} />} color="green" />
        <StatCard title={t('money.available')} value={dashboard.availableFund} icon={<PiggyBank size={20} />} color="green" />
        <StatCard title={t('money.given.loan')} value={dashboard.moneyLoanedOut} icon={<HandCoins size={20} />} color="orange" />
        <StatCard title={t('active.loans')} value={dashboard.activeLoans} color="blue" currency={false} />
        <StatCard title={t('expected.months')} value={dashboard.expectedContributionMonths} icon={<CalendarDays size={20} />} color="gray" currency={false} />
      </div>

      <div className="grid md:grid-cols-3 gap-4">
        <div className="card md:col-span-2">
          <h3 className="font-semibold mb-4">{t('monthly.contribution.trend')}</h3>
          <ContributionChart data={dashboard.contributionTrend} />
        </div>
        <div className="card">
          <h3 className="font-semibold mb-4">{t('loan.distribution')}</h3>
          <LoanDistributionChart data={dashboard.loanDistribution} />
        </div>
      </div>

      <div className="card">
        <h3 className="font-semibold mb-4">{t('interest.collection.trend')}</h3>
        <InterestChart data={dashboard.interestTrend} />
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        <div className="card">
          <h3 className="font-semibold mb-3">{t('recent.contributions')}</h3>
          <div className="space-y-2">
            {dashboard.recentContributions?.map(c => (
              <div key={c.id} className="flex justify-between items-center py-2 border-b border-gray-50 last:border-0">
                <div>
                  <p className="text-sm font-medium">{c.memberName}</p>
                  <p className="text-xs text-dark-400">{c.month}/{c.year}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm font-semibold">{formatCurrency(c.amount)}</p>
                  <Badge status={c.status} />
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          <h3 className="font-semibold mb-3">{t('recent.loans')}</h3>
          <div className="space-y-2">
            {dashboard.recentLoans?.map(l => (
              <div key={l.id} className="flex justify-between items-center py-2 border-b border-gray-50 last:border-0">
                <div>
                  <p className="text-sm font-medium">{l.borrowerName}</p>
                  <p className="text-xs text-dark-400">{t('loan')} #{l.id}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm font-semibold">{formatCurrency(l.loanAmount)}</p>
                  <Badge status={l.status} />
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="card">
        <h3 className="font-semibold mb-3">{t('recent.activity')}</h3>
        <div className="space-y-3">
          {dashboard.recentActivities?.map(a => (
            <div key={a.id} className="flex gap-3 text-sm">
              <div className="w-2 h-2 rounded-full bg-primary-500 mt-2 shrink-0" />
              <div>
                <p><span className="font-medium">{a.adminName}</span> - {translateAction(a.action, t)}</p>
                <p className="text-dark-400 text-xs">{translateDescription(a.description, language)}</p>
                <p className="text-dark-300 text-xs">{formatDate(a.createdAt)}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
