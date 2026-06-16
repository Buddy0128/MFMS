import { Languages } from 'lucide-react';
import { Language, useLanguage } from '../contexts/LanguageContext';

export default function LanguageSwitcher({ dark = false }: { dark?: boolean }) {
  const { language, setLanguage, t } = useLanguage();

  return (
    <label className={`inline-flex items-center gap-2 text-sm ${dark ? 'text-dark-300' : 'text-dark-600'}`}>
      <Languages size={17} aria-hidden="true" />
      <span className="sr-only">{t('language')}</span>
      <select
        value={language}
        onChange={(event) => setLanguage(event.target.value as Language)}
        aria-label={t('language')}
        className={`rounded-md border px-2 py-1.5 text-sm outline-none focus:ring-2 focus:ring-primary-500 ${
          dark
            ? 'border-dark-600 bg-dark-800 text-white'
            : 'border-gray-300 bg-white text-dark-700'
        }`}
      >
        <option value="en">{t('english')}</option>
        <option value="hi">{t('hindi')}</option>
        <option value="mr">{t('marathi')}</option>
      </select>
    </label>
  );
}
