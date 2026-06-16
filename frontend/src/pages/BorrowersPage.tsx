import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { borrowerApi } from '../services/services';
import Modal from '../components/Modal';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import { Plus, Eye } from 'lucide-react';
import { useLanguage } from '../contexts/LanguageContext';

export default function BorrowersPage() {
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ fullName: '', phoneNumber: '', address: '' });
  const qc = useQueryClient();
  const { t } = useLanguage();

  const { data: borrowers, isLoading } = useQuery({
    queryKey: ['borrowers'],
    queryFn: () => borrowerApi.getAll().then(r => r.data),
  });

  const createMutation = useMutation({
    mutationFn: (data: object) => borrowerApi.create(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['borrowers'] }); setShowModal(false); setForm({ fullName: '', phoneNumber: '', address: '' }); },
  });

  if (isLoading) return <LoadingSpinner fullPage />;

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">{t('external.borrowers')}</h1>
        <button onClick={() => setShowModal(true)} className="btn-primary flex items-center gap-2"><Plus size={18} /> {t('add.borrower')}</button>
      </div>

      <div className="grid gap-3">
        {borrowers?.map(b => (
          <div key={b.id} className="card flex justify-between items-center">
            <div>
              <p className="font-semibold">{b.fullName}</p>
              <p className="text-sm text-dark-500">{b.phoneNumber}</p>
              {b.address && <p className="text-xs text-dark-400 mt-1">{b.address}</p>}
            </div>
            <div className="flex items-center gap-3">
              <Badge status={b.status} />
              <Link to={`/admin/borrowers/${b.id}`} className="text-primary-600"><Eye size={18} /></Link>
            </div>
          </div>
        ))}
      </div>

      <Modal isOpen={showModal} onClose={() => setShowModal(false)} title={t('add.external.borrower')}>
        <form onSubmit={e => { e.preventDefault(); createMutation.mutate(form); }} className="space-y-4">
          <div><label className="label">{t('full.name')}</label><input className="input" required value={form.fullName} onChange={e => setForm({...form, fullName: e.target.value})} /></div>
          <div><label className="label">{t('phone')}</label><input className="input" required maxLength={10} value={form.phoneNumber} onChange={e => setForm({...form, phoneNumber: e.target.value.replace(/\D/g,'')})} /></div>
          <div><label className="label">{t('address')}</label><textarea className="input" rows={2} value={form.address} onChange={e => setForm({...form, address: e.target.value})} /></div>
          <button type="submit" className="btn-primary w-full">{t('save')}</button>
        </form>
      </Modal>
    </div>
  );
}
