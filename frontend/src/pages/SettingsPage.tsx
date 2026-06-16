import { useAuth } from '../contexts/AuthContext';
import { useLanguage } from '../contexts/LanguageContext';
import LanguageSwitcher from '../components/LanguageSwitcher';

export default function SettingsPage() {
  const { user } = useAuth();
  const { t } = useLanguage();
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">{t('settings')}</h1>
      <div className="card">
        <h3 className="font-semibold mb-3">{t('language')}</h3>
        <LanguageSwitcher />
      </div>
      <div className="card">
        <h3 className="font-semibold mb-4">{t('account.information')}</h3>
        <div className="space-y-3 text-sm">
          <div className="flex justify-between py-2 border-b"><span className="text-dark-500">{t('name')}</span><span className="font-medium">{user?.name}</span></div>
          <div className="flex justify-between py-2 border-b"><span className="text-dark-500">{t('phone')}</span><span className="font-medium">{user?.phoneNumber}</span></div>
          <div className="flex justify-between py-2"><span className="text-dark-500">{t('role')}</span><span className="font-medium">{t(user?.role?.toLowerCase() ?? '')}</span></div>
        </div>
      </div>
      <div className="card">
        <h3 className="font-semibold mb-2">{t('about.mfms')}</h3>
        <p className="text-sm text-dark-500">{t('about.description')}</p>
      </div>
    </div>
  );
}
