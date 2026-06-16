import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { contributionApi } from '../services/services';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import { formatCurrency } from '../utils/format';
import { AlertCircle, ChevronLeft, ChevronRight } from 'lucide-react';
import { useLanguage } from '../contexts/LanguageContext';

export default function ContributionsPage() {
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(1);
  const pageSize = 25;
  const qc = useQueryClient();
  const { t } = useLanguage();

  const { data: contributions, isLoading, isError } = useQuery({
    queryKey: ['contributions', statusFilter],
    queryFn: () => contributionApi.getAll(statusFilter ? { status: statusFilter } : {}).then(r => r.data),
  });

  const { data: report, isError: reportError } = useQuery({
    queryKey: ['pending-contribution-report'],
    queryFn: () => contributionApi.pendingReport().then(r => r.data),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) => contributionApi.updateStatus(id, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['contributions'] });
      qc.invalidateQueries({ queryKey: ['pending-contribution-report'] });
      qc.invalidateQueries({ queryKey: ['admin-dashboard'] });
    },
  });

  const totalRows = contributions?.length ?? 0;
  const totalPages = Math.max(1, Math.ceil(totalRows / pageSize));
  const paginatedContributions = useMemo(
    () => contributions?.slice((page - 1) * pageSize, page * pageSize) ?? [],
    [contributions, page]
  );

  useEffect(() => {
    setPage(1);
  }, [statusFilter]);

  useEffect(() => {
    if (page > totalPages) setPage(totalPages);
  }, [page, totalPages]);

  if (isLoading) return <LoadingSpinner fullPage />;

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold">{t('contributions')}</h1>
        <p className="text-sm text-dark-500 mt-1">{t('deposit.auto.note')}</p>
      </div>

      {report && (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <Summary label={t('expected.months')} value={String(report.expectedMonths)} />
          <Summary label={t('expected.collection')} value={formatCurrency(report.totalExpectedCollection)} />
          <Summary label={t('actual.collection')} value={formatCurrency(report.totalActualCollection)} />
          <Summary label={t('pending.collection')} value={formatCurrency(report.totalPendingCollection)} />
        </div>
      )}

      {(isError || reportError) && (
        <div className="card border-red-100 bg-red-50 text-sm font-medium text-red-700">
          {t('load.contributions.error')}
        </div>
      )}

      <div className="card flex gap-3 text-sm text-dark-600">
        <AlertCircle className="text-primary-600 shrink-0 mt-0.5" size={18} />
        <span>{t('deposit.action.note', { paid: t('mark.paid'), pending: t('mark.pending') })}</span>
      </div>

      <div className="flex gap-2">
        {['', 'PAID', 'PENDING'].map(s => (
          <button key={s || 'all'} onClick={() => setStatusFilter(s)}
            className={`px-3 py-1.5 rounded-lg text-sm ${statusFilter === s ? 'bg-primary-600 text-white' : 'bg-white border text-dark-600'}`}>
            {s ? t(s.toLowerCase()) : t('all')}
          </button>
        ))}
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full min-w-[680px] text-sm">
          <thead><tr className="text-left text-dark-500 border-b">
            <th className="pb-3 pr-4">{t('member')}</th><th className="pb-3 pr-4">{t('month')}</th><th className="pb-3 pr-4">{t('amount')}</th>
            <th className="pb-3 pr-4">{t('status')}</th><th className="pb-3">{t('action')}</th>
          </tr></thead>
          <tbody>
            {paginatedContributions.map(c => (
              <tr key={c.id} className="border-b border-gray-50">
                <td className="py-3 pr-4"><p className="font-medium">{c.memberName}</p><p className="text-xs text-dark-400">{c.memberCode}</p></td>
                <td className="py-3 pr-4">{t(`month.${c.month}`)} {c.year}</td>
                <td className="py-3 pr-4 font-semibold">{formatCurrency(c.amount)}</td>
                <td className="py-3 pr-4"><Badge status={c.status} /></td>
                <td className="py-3">
                  {c.status === 'PENDING' ? (
                    <button
                      onClick={() => statusMutation.mutate({ id: c.id, status: 'PAID' })}
                      disabled={statusMutation.isPending}
                      className="text-primary-600 text-xs font-semibold disabled:opacity-50"
                    >
                      {t('mark.paid')}
                    </button>
                  ) : (
                    <button
                      onClick={() => statusMutation.mutate({ id: c.id, status: 'PENDING' })}
                      disabled={statusMutation.isPending}
                      className="text-orange-600 text-xs font-semibold disabled:opacity-50"
                    >
                      {t('mark.pending')}
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {contributions?.length === 0 && (
              <tr>
                <td colSpan={5} className="py-8 text-center text-dark-400">
                  {t('no.contribution.rows')}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-dark-500">
          {t('showing', { from: totalRows === 0 ? 0 : (page - 1) * pageSize + 1, to: Math.min(page * pageSize, totalRows), total: totalRows })}
        </p>
        <div className="flex items-center gap-2">
          <button
            className="btn-secondary flex items-center gap-1 disabled:opacity-50"
            onClick={() => setPage(p => Math.max(1, p - 1))}
            disabled={page === 1}
          >
            <ChevronLeft size={16} /> {t('previous')}
          </button>
          <span className="text-sm font-medium text-dark-600">{t('page')} {page} {t('of')} {totalPages}</span>
          <button
            className="btn-secondary flex items-center gap-1 disabled:opacity-50"
            onClick={() => setPage(p => Math.min(totalPages, p + 1))}
            disabled={page === totalPages}
          >
            {t('next')} <ChevronRight size={16} />
          </button>
        </div>
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
