import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { Plus, Search, Eye } from 'lucide-react';
import { memberApi } from '../services/services';
import Modal from '../components/Modal';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import { formatCurrency } from '../utils/format';
import { useLanguage } from '../contexts/LanguageContext';

export default function MembersPage() {
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ fullName: '', phoneNumber: '', joinDate: '', totalDeposit: '' });
  const qc = useQueryClient();
  const { t } = useLanguage();

  const { data: members, isLoading } = useQuery({
    queryKey: ['members', search],
    queryFn: () => memberApi.getAll(search || undefined).then(r => r.data),
  });

  const createMutation = useMutation({
    mutationFn: (data: object) => memberApi.create(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['members'] }); setShowModal(false); setForm({ fullName: '', phoneNumber: '', joinDate: '', totalDeposit: '' }); },
  });

  if (isLoading) return <LoadingSpinner fullPage />;

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <h1 className="text-2xl font-bold">{t('members')}</h1>
        <button onClick={() => setShowModal(true)} className="btn-primary flex items-center gap-2">
          <Plus size={18} /> {t('add.member')}
        </button>
      </div>

      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-dark-400" size={18} />
        <input className="input pl-10" placeholder={t('search.members')} value={search} onChange={e => setSearch(e.target.value)} />
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full min-w-[680px] text-sm">
          <thead><tr className="text-left text-dark-500 border-b">
            <th className="pb-3 pr-4">{t('member.code')}</th><th className="pb-3 pr-4">{t('name')}</th>
            <th className="pb-3 pr-4">{t('total.deposit')}</th><th className="pb-3 pr-4">{t('pending.months')}</th>
            <th className="pb-3 pr-4">{t('status')}</th><th className="pb-3">{t('action')}</th>
          </tr></thead>
          <tbody>
            {members?.map(m => (
              <tr key={m.id} className="border-b border-gray-50 hover:bg-gray-50">
                <td className="py-3 pr-4 font-mono text-primary-700">{m.memberCode}</td>
                <td className="py-3 pr-4 font-medium">{m.fullName}</td>
                <td className="py-3 pr-4 font-semibold">{formatCurrency(m.totalDeposit)}</td>
                <td className="py-3 pr-4">{m.pendingMonths}</td>
                <td className="py-3 pr-4"><Badge status={m.status} /></td>
                <td className="py-3">
                  <Link to={`/admin/members/${m.id}`} className="text-primary-600 hover:text-primary-700"><Eye size={18} /></Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal isOpen={showModal} onClose={() => setShowModal(false)} title={t('add.member')}>
        <form onSubmit={e => { e.preventDefault(); createMutation.mutate({ ...form, totalDeposit: Number(form.totalDeposit || 0) }); }} className="space-y-4">
          <div><label className="label">{t('full.name')}</label><input className="input" required value={form.fullName} onChange={e => setForm({...form, fullName: e.target.value})} /></div>
          <div><label className="label">{t('phone.number')}</label><input className="input" required minLength={10} maxLength={10} inputMode="numeric" value={form.phoneNumber} onChange={e => setForm({...form, phoneNumber: e.target.value.replace(/\D/g,'')})} /></div>
          <div><label className="label">{t('total.deposit')}</label><input type="number" min={0} step={100} className="input" value={form.totalDeposit} onChange={e => setForm({...form, totalDeposit: e.target.value})} /></div>
          <div><label className="label">{t('join.date')}</label><input type="date" className="input" value={form.joinDate} onChange={e => setForm({...form, joinDate: e.target.value})} /></div>
          {createMutation.error && (
            <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700">
              {t('could.not.save.member')}
            </p>
          )}
          <button type="submit" className="btn-primary w-full" disabled={createMutation.isPending}>{t('create.member')}</button>
        </form>
      </Modal>
    </div>
  );
}
