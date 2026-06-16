import en from './en';
import hi from './hi';
import mr from './mr';

export type TranslationLanguage = 'en' | 'hi' | 'mr';

export const translations: Record<TranslationLanguage, Record<string, string>> = {
  en,
  hi,
  mr,
};
