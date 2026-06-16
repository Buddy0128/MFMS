import { Link } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext';

export default function NotFoundPage() {
  const { t } = useLanguage();
  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="text-center">
        <h1 className="text-6xl font-bold text-primary-600">404</h1>
        <p className="text-dark-500 mt-2">{t('page.not.found')}</p>
        <Link to="/login" className="btn-primary inline-block mt-6">{t('go.to.login')}</Link>
      </div>
    </div>
  );
}
