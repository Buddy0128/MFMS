import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { loanApi, paymentApi } from '../services/services';
import LoadingSpinner from '../components/LoadingSpinner';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { formatCurrency, formatDate } from '../utils/format';
import { ArrowLeft, Pencil } from 'lucide-react';
import { useLanguage } from '../contexts/LanguageContext';

export default function LoanDetailPage() {
  const { id } = useParams();
  const [showPrincipal, setShowPrincipal] = useState(false);
  const [showInterest, setShowInterest] = useState(false);
  const [showEdit, setShowEdit] = useState(false);
  const [amount, setAmount] = useState('');
  const [editForm, setEditForm] = useState({ loanAmount: '', loanDate: '' });
  const qc = useQueryClient();
  const { t } = useLanguage();

  const { data: loan, isLoading } = useQuery({
    queryKey: ['loan', id],
    queryFn: () => loanApi.getById(Number(id)).then(r => r.data),
  });

  const { data: principalPayments } = useQuery({
    queryKey: ['principal', id],
    queryFn: () => loanApi.principalPayments(Number(id)).then(r => r.data),
  });

  const { data: interestPayments } = useQuery({
    queryKey: ['interest', id],
    queryFn: () => loanApi.interestPayments(Number(id)).then(r => r.data),
  });

  const principalMutation = useMutation({
    mutationFn: (data: object) => paymentApi.principal(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['loan', id] }); qc.invalidateQueries({ queryKey: ['principal', id] }); setShowPrincipal(false); setAmount(''); },
  });

  const interestMutation = useMutation({
    mutationFn: (data: object) => paymentApi.interest(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['loan', id] }); qc.invalidateQueries({ queryKey: ['interest', id] }); setShowInterest(false); setAmount(''); },
  });

  const closeMutation = useMutation({
    mutationFn: () => loanApi.close(Number(id)),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['loan', id] }),
  });

  const updateMutation = useMutation({
    mutationFn: () => loanApi.update(Number(id), {
      loanAmount: Number(editForm.loanAmount),
      loanDate: editForm.loanDate,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['loan', id] });
      qc.invalidateQueries({ queryKey: ['loans'] });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
      setShowEdit(false);
    },
  });

  if (isLoading) return <LoadingSpinner fullPage />;
  if (!loan) return null;

  const today = new Date().toISOString().split('T')[0];
  const openEditModal = () => {
    setEditForm({
      loanAmount: String(loan.loanAmount),
      loanDate: loan.loanDate,
    });
    setShowEdit(true);
  };

  return (
    <div className="space-y-6">
      <Link to="/admin/loans" className="flex items-center gap-2 text-primary-600 text-sm"><ArrowLeft size={16} /> {t('back')}</Link>

      <div className="card">
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-2xl font-bold">{t('loan')} #{loan.id}</h1>
            <p className="text-dark-500">{loan.borrowerName} · <Badge status={loan.borrowerType} /></p>
          </div>
          <div className="flex items-center gap-3">
            <Badge status={loan.status} />
            <button type="button" onClick={openEditModal} className="btn-secondary flex items-center gap-2">
              <Pencil size={16} /> {t('edit.loan')}
            </button>
          </div>
        </div>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4">
          <div><p className="text-xs text-dark-400">{t('loan.amount')}</p><p className="font-bold">{formatCurrency(loan.loanAmount)}</p></div>
          <div><p className="text-xs text-dark-400">{t('outstanding')}</p><p className="font-bold text-orange-600">{formatCurrency(loan.outstandingAmount)}</p></div>
          <div><p className="text-xs text-dark-400">{t('monthly.interest')}</p><p className="font-bold">{formatCurrency(loan.monthlyInterest)}</p></div>
          <div><p className="text-xs text-dark-400">{t('pending.interest')}</p><p className="font-bold text-red-600">{formatCurrency(loan.pendingInterest)}</p></div>
        </div>
        <p className="text-sm text-dark-400 mt-2">{t('rate')}: {loan.interestRate}% · {t('date')}: {formatDate(loan.loanDate)}</p>

        {loan.status === 'ACTIVE' && (
          <div className="flex gap-2 mt-4 flex-wrap">
            <button onClick={() => setShowPrincipal(true)} className="btn-primary">{t('add.principal.payment')}</button>
            <button onClick={() => setShowInterest(true)} className="btn-secondary">{t('record.interest')}</button>
            {loan.outstandingAmount === 0 && (
              <button onClick={() => closeMutation.mutate()} className="btn-secondary">{t('close.loan')}</button>
            )}
          </div>
        )}
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        <div className="card">
          <h3 className="font-semibold mb-3">{t('principal.payments')}</h3>
          {principalPayments?.map(p => (
            <div key={p.id} className="flex justify-between py-2 border-b border-gray-50 text-sm">
              <span>{formatDate(p.paymentDate)}</span>
              <span className="font-medium">{formatCurrency(p.amount)}</span>
            </div>
          ))}
          {!principalPayments?.length && <p className="text-dark-400 text-sm">{t('no.payments.yet')}</p>}
        </div>
        <div className="card">
          <h3 className="font-semibold mb-3">{t('interest.payments')}</h3>
          {interestPayments?.map(p => (
            <div key={p.id} className="flex justify-between py-2 border-b border-gray-50 text-sm">
              <span>{formatDate(p.paymentDate)}</span>
              <span className="font-medium">{formatCurrency(p.amount)}</span>
            </div>
          ))}
          {!interestPayments?.length && <p className="text-dark-400 text-sm">{t('no.payments.yet')}</p>}
        </div>
      </div>

      <Modal isOpen={showPrincipal} onClose={() => setShowPrincipal(false)} title={t('principal.payment')}>
        <form onSubmit={e => { e.preventDefault(); principalMutation.mutate({ loanId: Number(id), amount: Number(amount), paymentDate: today }); }} className="space-y-4">
          <div><label className="label">{t('amount')} (₹)</label><input type="number" className="input" required min={1} max={loan.outstandingAmount} value={amount} onChange={e => setAmount(e.target.value)} /></div>
          <p className="text-sm text-dark-400">{t('outstanding')}: {formatCurrency(loan.outstandingAmount)}</p>
          <button type="submit" className="btn-primary w-full">{t('submit.payment')}</button>
        </form>
      </Modal>

      <Modal isOpen={showInterest} onClose={() => setShowInterest(false)} title={t('interest.payment')}>
        <form onSubmit={e => { e.preventDefault(); interestMutation.mutate({ loanId: Number(id), amount: Number(amount), paymentDate: today }); }} className="space-y-4">
          <div><label className="label">{t('amount')} (₹)</label><input type="number" className="input" required min={1} value={amount || loan.monthlyInterest} onChange={e => setAmount(e.target.value)} /></div>
          <p className="text-sm text-dark-400">{t('suggested.monthly')}: {formatCurrency(loan.monthlyInterest)}</p>
          <button type="submit" className="btn-primary w-full">{t('record.interest')}</button>
        </form>
      </Modal>

      <Modal isOpen={showEdit} onClose={() => setShowEdit(false)} title={t('edit.loan')}>
        <form onSubmit={e => { e.preventDefault(); updateMutation.mutate(); }} className="space-y-4">
          <div>
            <label className="label">{t('loan.amount.rupee')}</label>
            <input
              type="number"
              min={1}
              className="input"
              required
              value={editForm.loanAmount}
              onChange={e => setEditForm({ ...editForm, loanAmount: e.target.value })}
            />
          </div>
          <div>
            <label className="label">{t('date')}</label>
            <input
              type="date"
              className="input"
              required
              value={editForm.loanDate}
              onChange={e => setEditForm({ ...editForm, loanDate: e.target.value })}
            />
          </div>
          <p className="text-sm text-dark-400">
            {t('outstanding')}: {formatCurrency(loan.outstandingAmount)}
          </p>
          {updateMutation.error && (
            <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700">
              {t('could.not.save.loan')}
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
