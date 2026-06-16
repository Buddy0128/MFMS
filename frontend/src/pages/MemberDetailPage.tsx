import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { memberApi } from '../services/services';
import LoadingSpinner from '../components/LoadingSpinner';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { formatCurrency } from '../utils/format';
import { ArrowLeft, Pencil } from 'lucide-react';
import { useLanguage } from '../contexts/LanguageContext';

export default function MemberDetailPage() {
  const { id } = useParams();
  const [showEditModal, setShowEditModal] = useState(false);
  const [editForm, setEditForm] = useState({
    fullName: '',
    phoneNumber: '',
    joinDate: '',
    totalDeposit: '',
  });
  const qc = useQueryClient();
  const { t } = useLanguage();

  const { data, isLoading } = useQuery({
    queryKey: ['member', id],
    queryFn: () => memberApi.getById(Number(id)).then(r => r.data),
  });

  const updateMutation = useMutation({
    mutationFn: () => memberApi.update(Number(id), {
      fullName: editForm.fullName.trim(),
      phoneNumber: editForm.phoneNumber,
      joinDate: editForm.joinDate,
      totalDeposit: Number(editForm.totalDeposit),
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['member', id] });
      qc.invalidateQueries({ queryKey: ['members'] });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
      setShowEditModal(false);
    },
  });

  if (isLoading) return <LoadingSpinner fullPage />;
  if (!data) return null;
  const summary = data.contributionSummary;

  const openEditModal = () => {
    setEditForm({
      fullName: data.member.fullName,
      phoneNumber: data.member.phoneNumber,
      joinDate: data.member.joinDate,
      totalDeposit: String(data.member.totalDeposit),
    });
    setShowEditModal(true);
  };

  return (
    <div className="space-y-6">
      <Link to="/admin/members" className="flex items-center gap-2 text-primary-600 text-sm"><ArrowLeft size={16} /> {t('back')}</Link>

      <div className="card">
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-2xl font-bold">{data.member.fullName}</h1>
            <p className="text-dark-500">{data.member.memberCode} · {data.member.phoneNumber}</p>
          </div>
          <div className="flex items-center gap-3">
            <Badge status={data.member.status} />
            <button
              type="button"
              onClick={openEditModal}
              className="btn-secondary flex items-center gap-2"
            >
              <Pencil size={16} /> {t('edit.member')}
            </button>
          </div>
        </div>
        <div className="grid grid-cols-2 gap-4 mt-4 md:grid-cols-3">
          <Summary label={t('total.deposit')} value={formatCurrency(summary.totalDeposit)} />
          <Summary label={t('paid.months')} value={String(summary.paidMonths)} />
          <Summary label={t('pending.months')} value={String(summary.pendingMonths)} />
          <Summary label={t('last.paid.month')} value={summary.lastPaidMonth ?? t('no.payment')} />
          <Summary label={t('completion')} value={`${summary.completionPercentage}%`} />
          <Summary label={t('extra.amount')} value={formatCurrency(summary.extraAmount)} />
          <Summary label={t('current.loan.amount')} value={formatCurrency(data.outstandingAmount)} />
          <Summary label={t('total.interest.paid')} value={formatCurrency(data.totalInterestPaid)} />
        </div>
        {summary.validationMessage && (
          <div className="mt-4 rounded-lg border border-orange-100 bg-orange-50 px-3 py-2 text-sm font-medium text-orange-700">
            {summary.validationMessage}
          </div>
        )}
      </div>

      <div className="card">
        <h3 className="font-semibold mb-3">{t('contribution.timeline')}</h3>
        {data.contributions.map(c => (
          <div key={c.id} className="flex justify-between py-2 border-b border-gray-50 text-sm">
            <span>{t(`month.${c.month}`)} {c.year}</span>
            <span>{formatCurrency(c.amount)}</span>
            <Badge status={c.status} />
          </div>
        ))}
      </div>

      <div className="card">
        <h3 className="font-semibold mb-3">{t('loans')}</h3>
        {data.loans.map(l => (
          <Link key={l.id} to={`/admin/loans/${l.id}`} className="block py-3 border-b border-gray-50 hover:bg-gray-50 -mx-2 px-2 rounded">
            <div className="flex justify-between">
              <span className="font-medium">{t('loan')} #{l.id}</span>
              <Badge status={l.status} />
            </div>
            <p className="text-sm text-dark-500 mt-1">{t('outstanding')}: {formatCurrency(l.outstandingAmount)}</p>
          </Link>
        ))}
      </div>

      <Modal isOpen={showEditModal} onClose={() => setShowEditModal(false)} title={t('edit.member')}>
        <form onSubmit={event => { event.preventDefault(); updateMutation.mutate(); }} className="space-y-4">
          <div>
            <label className="label">{t('full.name')}</label>
            <input
              className="input"
              required
              value={editForm.fullName}
              onChange={event => setEditForm({ ...editForm, fullName: event.target.value })}
            />
          </div>
          <div>
            <label className="label">{t('phone.number')}</label>
            <input
              className="input"
              required
              minLength={10}
              maxLength={10}
              inputMode="numeric"
              value={editForm.phoneNumber}
              onChange={event => setEditForm({
                ...editForm,
                phoneNumber: event.target.value.replace(/\D/g, ''),
              })}
            />
          </div>
          <div>
            <label className="label">{t('total.deposit')}</label>
            <input
              type="number"
              min={0}
              step={100}
              className="input"
              required
              value={editForm.totalDeposit}
              onChange={event => setEditForm({ ...editForm, totalDeposit: event.target.value })}
            />
          </div>
          <div>
            <label className="label">{t('join.date')}</label>
            <input
              type="date"
              className="input"
              required
              value={editForm.joinDate}
              onChange={event => setEditForm({ ...editForm, joinDate: event.target.value })}
            />
          </div>
          {updateMutation.error && (
            <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700">
              {t('could.not.save.member')}
            </p>
          )}
          <button type="submit" className="btn-primary w-full" disabled={updateMutation.isPending}>
            {t('save')}
          </button>
        </form>
      </Modal>
    </div>
  );
}

function Summary({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs text-dark-400">{label}</p>
      <p className="text-lg font-bold">{value}</p>
    </div>
  );
}
