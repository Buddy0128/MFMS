import { useQuery } from '@tanstack/react-query';
import { contributionApi } from '../services/services';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import { formatCurrency } from '../utils/format';
import { useLanguage } from '../contexts/LanguageContext';

export default function MemberContributionsPage() {
  const { t } = useLanguage();
  const { data, isLoading } = useQuery({
    queryKey: ['member-contributions'],
    queryFn: () => contributionApi.memberContributions().then(r => r.data),
  });
  const { data: summary } = useQuery({
    queryKey: ['member-contribution-summary'],
    queryFn: () => contributionApi.memberOwnSummary().then(r => r.data),
  });

  if (isLoading) return <LoadingSpinner fullPage />;

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">{t('my.contributions')}</h1>
      {summary && (
        <div className="grid grid-cols-2 gap-4">
          <Summary label={t('total.deposit')} value={formatCurrency(summary.totalDeposit)} />
          <Summary label={t('paid.months')} value={String(summary.paidMonths)} />
          <Summary label={t('pending.months')} value={String(summary.pendingMonths)} />
          <Summary label={t('last.paid.month')} value={summary.lastPaidMonth ?? t('no.payment')} />
          <Summary label={t('completion')} value={`${summary.completionPercentage}%`} />
          <Summary label={t('extra.amount')} value={formatCurrency(summary.extraAmount)} />
        </div>
      )}
      {summary?.validationMessage && (
        <div className="card border-orange-100 bg-orange-50 text-sm font-medium text-orange-700">
          {summary.validationMessage}
        </div>
      )}
      <div className="card">
        {data?.map(c => (
          <div key={c.id} className="flex justify-between py-3 border-b border-gray-50">
            <span>{t(`month.${c.month}`)} {c.year}</span>
            <span className="font-medium">{formatCurrency(c.amount)}</span>
            <Badge status={c.status} />
          </div>
        ))}
      </div>
    </div>
  );
}

function Summary({ label, value }: { label: string; value: string }) {
  return (
    <div className="card">
      <p className="text-xs text-dark-400">{label}</p>
      <p className="text-lg font-bold mt-1">{value}</p>
    </div>
  );
}
