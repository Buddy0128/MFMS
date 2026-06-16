import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { translations, type TranslationLanguage } from '../i18n';

export type Language = TranslationLanguage;

type TranslationParams = Record<string, string | number>;

interface LanguageContextValue {
  language: Language;
  setLanguage: (language: Language) => void;
  t: (key: string, params?: TranslationParams) => string;
}

const LanguageContext = createContext<LanguageContextValue | undefined>(undefined);

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  const [language, setLanguage] = useState<Language>(() => {
    const saved = localStorage.getItem('mfms_language');
    return saved === 'hi' || saved === 'mr' ? saved : 'en';
  });

  useEffect(() => {
    localStorage.setItem('mfms_language', language);
    document.documentElement.lang = language;
  }, [language]);

  const value = useMemo<LanguageContextValue>(() => ({
    language,
    setLanguage,
    t: (key, params) => {
      let text = translations[language][key] ?? translations.en[key] ?? key;
      Object.entries(params ?? {}).forEach(([name, replacement]) => {
        text = text.split(`{${name}}`).join(String(replacement));
      });
      return text;
    },
  }), [language]);

  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
}

export function useLanguage() {
  const context = useContext(LanguageContext);
  if (!context) throw new Error('useLanguage must be used inside LanguageProvider');
  return context;
}
