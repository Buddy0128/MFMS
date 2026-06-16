import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { loanApi, memberApi, borrowerApi } from '../services/services';
import Modal from '../components/Modal';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import { formatCurrency, formatDate } from '../utils/format';
import { Plus, Eye } from 'lucide-react';
import { useLanguage } from '../contexts/LanguageContext';

export default function LoansPage() {
  const [statusFilter, setStatusFilter] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ borrowerType: 'MEMBER', borrowerId: '', loanAmount: '' });
  const qc = useQueryClient();
  const { t } = useLanguage();

  const { data: loans, isLoading } = useQuery({
    queryKey: ['loans', statusFilter],
    queryFn: () => loanApi.getAll(statusFilter ? { status: statusFilter } : {}).then(r => r.data),
  });

  const { data: members } = useQuery({ queryKey: ['members-list'], queryFn: () => memberApi.getAll().then(r => r.data) });
  const { data: borrowers } = useQuery({ queryKey: ['borrowers-list'], queryFn: () => borrowerApi.getAll().then(r => r.data) });

  const createMutation = useMutation({
    mutationFn: (data: object) => loanApi.issue(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['loans'] }); setShowModal(false); },
  });

  if (isLoading) return <LoadingSpinner fullPage />;

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">{t('loans')}</h1>
        <button onClick={() => setShowModal(true)} className="btn-primary flex items-center gap-2"><Plus size={18} /> {t('issue.loan')}</button>
      </div>

      <div className="flex gap-2">
        {['', 'ACTIVE', 'CLOSED'].map(s => (
          <button key={s || 'all'} onClick={() => setStatusFilter(s)}
            className={`px-3 py-1.5 rounded-lg text-sm ${statusFilter === s ? 'bg-primary-600 text-white' : 'bg-white border text-dark-600'}`}>
            {s ? t(s.toLowerCase()) : t('all')}
          </button>
        ))}
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full min-w-[760px] text-sm">
          <thead><tr className="text-left text-dark-500 border-b">
            <th className="pb-3 pr-4">{t('id')}</th><th className="pb-3 pr-4">{t('borrower')}</th><th className="pb-3 pr-4">{t('type')}</th>
            <th className="pb-3 pr-4">{t('amount')}</th><th className="pb-3 pr-4">{t('outstanding')}</th><th className="pb-3 pr-4">{t('status')}</th><th className="pb-3">{t('view')}</th>
          </tr></thead>
          <tbody>
            {loans?.map(l => (
              <tr key={l.id} className="border-b border-gray-50">
                <td className="py-3 pr-4 font-mono">#{l.id}</td>
                <td className="py-3 pr-4 font-medium">{l.borrowerName}</td>
                <td className="py-3 pr-4"><Badge status={l.borrowerType} /></td>
                <td className="py-3 pr-4">{formatCurrency(l.loanAmount)}</td>
                <td className="py-3 pr-4 font-semibold">{formatCurrency(l.outstandingAmount)}</td>
                <td className="py-3 pr-4"><Badge status={l.status} /></td>
                <td className="py-3"><Link to={`/admin/loans/${l.id}`} className="text-primary-600"><Eye size={18} /></Link></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal isOpen={showModal} onClose={() => setShowModal(false)} title={t('issue.loan')}>
        <form onSubmit={e => { e.preventDefault(); createMutation.mutate({ ...form, borrowerId: Number(form.borrowerId), loanAmount: Number(form.loanAmount) }); }} className="space-y-4">
          <div><label className="label">{t('borrower.type')}</label>
            <select className="input" value={form.borrowerType} onChange={e => setForm({...form, borrowerType: e.target.value, borrowerId: ''})}>
              <option value="MEMBER">{t('member.interest')}</option>
              <option value="EXTERNAL">{t('external.interest')}</option>
            </select>
          </div>
          <div><label className="label">{t('borrower')}</label>
            <select className="input" required value={form.borrowerId} onChange={e => setForm({...form, borrowerId: e.target.value})}>
              <option value="">{t('select.borrower')}</option>
              {form.borrowerType === 'MEMBER'
                ? members?.map(m => <option key={m.id} value={m.id}>{m.fullName}</option>)
                : borrowers?.map(b => <option key={b.id} value={b.id}>{b.fullName}</option>)}
            </select>
          </div>
          <div><label className="label">{t('loan.amount.rupee')}</label><input type="number" className="input" required min={1} value={form.loanAmount} onChange={e => setForm({...form, loanAmount: e.target.value})} /></div>
          <button type="submit" className="btn-primary w-full">{t('issue.loan')}</button>
        </form>
      </Modal>
    </div>
  );
}
