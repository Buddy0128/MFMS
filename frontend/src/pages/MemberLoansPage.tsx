import { useQuery } from '@tanstack/react-query';
import { loanApi } from '../services/services';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import { formatCurrency } from '../utils/format';
import { useLanguage } from '../contexts/LanguageContext';

export default function MemberLoansPage() {
  const { t } = useLanguage();
  const { data, isLoading } = useQuery({
    queryKey: ['member-loans'],
    queryFn: () => loanApi.memberLoans().then(r => r.data),
  });

  if (isLoading) return <LoadingSpinner fullPage />;

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">{t('my.loans')}</h1>
      {data?.map(l => (
        <div key={l.id} className="card">
          <div className="flex justify-between"><span className="font-semibold">{t('loan')} #{l.id}</span><Badge status={l.status} /></div>
          <div className="grid grid-cols-2 gap-2 mt-3 text-sm">
            <div><p className="text-dark-400">{t('amount')}</p><p className="font-medium">{formatCurrency(l.loanAmount)}</p></div>
            <div><p className="text-dark-400">{t('outstanding')}</p><p className="font-medium">{formatCurrency(l.outstandingAmount)}</p></div>
            <div><p className="text-dark-400">{t('monthly.interest')}</p><p className="font-medium">{formatCurrency(l.monthlyInterest)}</p></div>
            <div><p className="text-dark-400">{t('paid.interest')}</p><p className="font-medium">{formatCurrency(l.totalInterestPaid)}</p></div>
          </div>
        </div>
      ))}
      {!data?.length && <p className="text-dark-400">{t('no.loans')}</p>}
    </div>
  );
}
