import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { borrowerApi } from '../services/services';
import LoadingSpinner from '../components/LoadingSpinner';
import Badge from '../components/Badge';
import { formatCurrency } from '../utils/format';
import { ArrowLeft } from 'lucide-react';
import { useLanguage } from '../contexts/LanguageContext';

export default function BorrowerDetailPage() {
  const { id } = useParams();
  const { t } = useLanguage();
  const { data, isLoading } = useQuery({
    queryKey: ['borrower', id],
    queryFn: () => borrowerApi.getById(Number(id)).then(r => r.data),
  });

  if (isLoading) return <LoadingSpinner fullPage />;
  if (!data) return null;

  return (
    <div className="space-y-6">
      <Link to="/admin/borrowers" className="flex items-center gap-2 text-primary-600 text-sm"><ArrowLeft size={16} /> {t('back')}</Link>
      <div className="card">
        <h1 className="text-2xl font-bold">{data.borrower.fullName}</h1>
        <p className="text-dark-500">{data.borrower.phoneNumber}</p>
        <div className="grid grid-cols-2 gap-4 mt-4">
          <div><p className="text-xs text-dark-400">{t('outstanding')}</p><p className="font-bold text-lg">{formatCurrency(data.outstandingAmount)}</p></div>
          <div><p className="text-xs text-dark-400">{t('pending.interest')}</p><p className="font-bold text-lg">{formatCurrency(data.pendingInterest)}</p></div>
        </div>
      </div>
      <div className="card">
        <h3 className="font-semibold mb-3">{t('loans')}</h3>
        {data.loans.map(l => (
          <Link key={l.id} to={`/admin/loans/${l.id}`} className="block py-3 border-b border-gray-50">
            <div className="flex justify-between"><span>{t('loan')} #{l.id}</span><Badge status={l.status} /></div>
            <p className="text-sm text-dark-500">{t('outstanding')}: {formatCurrency(l.outstandingAmount)}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
