import type { Language } from '../contexts/LanguageContext';

const actionKeys: Record<string, string> = {
  'Created Member': 'activity.created.member',
  'Updated Member': 'activity.updated.member',
  'Disabled Member': 'activity.disabled.member',
  'Imported Excel Data': 'activity.imported.excel.data',
  'Updated Contribution Status': 'activity.updated.contribution.status',
  'Issued Loan': 'activity.issued.loan',
  'Closed Loan': 'activity.closed.loan',
  'Added Principal Payment': 'activity.added.principal.payment',
  'Added Interest Payment': 'activity.added.interest.payment',
  'Created Borrower': 'activity.created.borrower',
  'Updated Borrower': 'activity.updated.borrower',
};

export function translateAction(action: string, t: (key: string) => string) {
  return t(actionKeys[action] ?? action);
}

export function translateDescription(description: string, language: Language) {
  if (language === 'en') return description;
  return description
    .split('Member:').join(language === 'hi' ? 'सदस्य:' : 'सदस्य:')
    .split('Loan:').join(language === 'hi' ? 'उधार:' : 'उधार:')
    .split('rows').join(language === 'hi' ? 'लाइन' : 'लाइन')
    .split('failed').join(language === 'hi' ? 'गलत' : 'चुकले')
    .split('marked').join(language === 'hi' ? 'बदला' : 'बदलला')
    .split('as PAID').join(language === 'hi' ? 'जमा हुआ' : 'भरले')
    .split('as PENDING').join(language === 'hi' ? 'बाकी' : 'बाकी');
}
