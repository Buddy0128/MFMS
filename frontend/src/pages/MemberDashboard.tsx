import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '../services/services';
import StatCard from '../components/StatCard';
import LoadingSpinner from '../components/LoadingSpinner';
import Badge from '../components/Badge';
import { formatCurrency, formatDate } from '../utils/format';
import { Wallet, HandCoins, TrendingUp, PiggyBank } from 'lucide-react';
import { useLanguage } from '../contexts/LanguageContext';

export default function MemberDashboard() {
  const { t } = useLanguage();
  const { data, isLoading } = useQuery({
    queryKey: ['member-dashboard'],
    queryFn: () => dashboardApi.member().then(r => r.data),
  });

  if (isLoading) return <LoadingSpinner fullPage />;
  if (!data) return null;
  const summary = data.contributionSummary;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-dark-900">{t('welcome', { name: data.member.fullName })}</h1>
        <p className="text-dark-500 text-sm">{data.member.memberCode}</p>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <StatCard title={t('total.deposit')} value={summary.totalDeposit} icon={<Wallet size={20} />} />
        <StatCard title={t('paid.months')} value={summary.paidMonths} currency={false} />
        <StatCard title={t('pending.months')} value={summary.pendingMonths} color="orange" currency={false} />
        <StatCard title={t('last.paid.month')} value={summary.lastPaidMonth ?? t('no.payment')} color="gray" />
        <StatCard title={t('completion')} value={`${summary.completionPercentage}%`} color="green" currency={false} />
        <StatCard title={t('current.loan.amount')} value={data.outstandingPrincipal} icon={<HandCoins size={20} />} color="orange" />
        <StatCard title={t('total.interest.paid')} value={data.totalInterestPaid} icon={<TrendingUp size={20} />} color="green" />
      </div>

      {summary.validationMessage && (
        <div className="card border-orange-100 bg-orange-50 text-orange-700 text-sm font-medium">
          {summary.validationMessage}
        </div>
      )}

      <div className="card flex items-center justify-between">
        <span className="text-sm font-medium text-dark-500">{t('mandal.available.fund')}</span>
        <span className="text-lg font-bold text-primary-700">{formatCurrency(data.availableFund)}</span>
      </div>

      <div className="card">
        <h3 className="font-semibold mb-3">{t('contribution.history')}</h3>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead><tr className="text-left text-dark-500 border-b">
              <th className="pb-2">{t('month')}</th><th className="pb-2">{t('amount')}</th><th className="pb-2">{t('status')}</th>
            </tr></thead>
            <tbody>
              {data.contributions.map(c => (
                <tr key={c.id} className="border-b border-gray-50">
                  <td className="py-2">{t(`month.${c.month}`)} {c.year}</td>
                  <td>{formatCurrency(c.amount)}</td>
                  <td><Badge status={c.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card">
        <h3 className="font-semibold mb-3">{t('my.loans')}</h3>
        {data.loans.length === 0 ? (
          <p className="text-dark-400 text-sm">{t('no.loans')}</p>
        ) : (
          data.loans.map(l => (
            <div key={l.id} className="py-3 border-b border-gray-50 last:border-0">
              <div className="flex justify-between">
                <span className="font-medium">{t('loan')} #{l.id}</span>
                <Badge status={l.status} />
              </div>
              <div className="grid grid-cols-2 gap-2 mt-2 text-sm text-dark-500">
                <span>{t('amount')}: {formatCurrency(l.loanAmount)}</span>
                <span>{t('outstanding')}: {formatCurrency(l.outstandingAmount)}</span>
                <span>{t('monthly.interest')}: {formatCurrency(l.monthlyInterest)}</span>
                <span>{t('paid.interest')}: {formatCurrency(l.totalInterestPaid)}</span>
              </div>
            </div>
          ))
        )}
      </div>

      <div className="card">
        <h3 className="font-semibold mb-3">{t('recent.transactions')}</h3>
        {data.recentTransactions.map(transaction => (
          <div key={transaction.id} className="flex justify-between py-2 border-b border-gray-50 text-sm">
            <span>{t('loan')} #{transaction.loanId}</span>
            <span className="font-medium">{formatCurrency(transaction.amount)}</span>
            <span className="text-dark-400">{formatDate(transaction.paymentDate)}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
