import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { authApi } from '../services/services';
import { useLanguage } from '../contexts/LanguageContext';
import LanguageSwitcher from '../components/LanguageSwitcher';

export default function LoginPage() {
  const [mode, setMode] = useState<'admin' | 'member'>('admin');
  const [adminForm, setAdminForm] = useState({ phone: '', pin: '' });
  const [memberForm, setMemberForm] = useState({ phone: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = mode === 'admin'
        ? await authApi.adminLogin(adminForm.phone, adminForm.pin)
        : await authApi.memberLogin(memberForm.phone);
      login(res.data);
      navigate(res.data.role === 'ADMIN' ? '/admin' : '/member');
    } catch {
      setError(t('login.failed'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-700 via-primary-600 to-dark-800 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="mb-4 flex justify-end">
          <LanguageSwitcher dark />
        </div>
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-white rounded-2xl shadow-lg mb-4">
            <span className="text-2xl font-bold text-primary-600">M</span>
          </div>
          <h1 className="text-3xl font-bold text-white">{t('app.name')}</h1>
          <p className="text-primary-100 mt-2">{t('mandal.fund.management.system')}</p>
        </div>

        <div className="bg-white rounded-2xl shadow-xl p-6 md:p-8">
          <div className="flex rounded-lg bg-gray-100 p-1 mb-6">
            <button
              type="button"
              onClick={() => { setMode('admin'); setError(''); }}
              className={`flex-1 py-2 rounded-md text-sm font-medium transition ${mode === 'admin' ? 'bg-white shadow text-primary-700' : 'text-dark-500'}`}
            >
              {t('admin.login')}
            </button>
            <button
              type="button"
              onClick={() => { setMode('member'); setError(''); }}
              className={`flex-1 py-2 rounded-md text-sm font-medium transition ${mode === 'member' ? 'bg-white shadow text-primary-700' : 'text-dark-500'}`}
            >
              {t('member.login')}
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label">{t('phone.number')}</label>
              <input
                type="tel"
                className="input"
                placeholder="9876543210"
                maxLength={10}
                value={mode === 'admin' ? adminForm.phone : memberForm.phone}
                onChange={(e) => {
                  const phone = e.target.value.replace(/\D/g, '');
                  if (mode === 'admin') {
                    setAdminForm(current => ({ ...current, phone }));
                  } else {
                    setMemberForm({ phone });
                  }
                }}
                required
              />
            </div>

            {mode === 'admin' && (
              <div>
                <label className="label">{t('four.digit.pin')}</label>
                <input
                  type="password"
                  className="input"
                  placeholder="••••"
                  maxLength={4}
                  value={adminForm.pin}
                  onChange={(e) => setAdminForm(current => ({
                    ...current,
                    pin: e.target.value.replace(/\D/g, ''),
                  }))}
                  required
                />
              </div>
            )}

            {error && (
              <div className="bg-red-50 text-red-700 text-sm p-3 rounded-lg">{error}</div>
            )}

            <button type="submit" disabled={loading} className="btn-primary w-full">
              {loading ? t('signing.in') : t('sign.in')}
            </button>
          </form>

          {mode === 'admin' && (
            <p className="text-xs text-dark-400 mt-4 text-center">
              {t('demo.admin')}
            </p>
          )}
          {mode === 'member' && (
            <p className="text-xs text-dark-400 mt-4 text-center">
              {t('demo.member')}
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
