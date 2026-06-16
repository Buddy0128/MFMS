import { useLanguage } from '../contexts/LanguageContext';

interface BadgeProps {
  status: string;
  variant?: 'default' | 'success' | 'warning' | 'danger';
}

const variants: Record<string, string> = {
  PAID: 'bg-primary-100 text-primary-800',
  ACTIVE: 'bg-primary-100 text-primary-800',
  PENDING: 'bg-yellow-100 text-yellow-800',
  CLOSED: 'bg-gray-100 text-gray-700',
  INACTIVE: 'bg-red-100 text-red-800',
  MEMBER: 'bg-blue-100 text-blue-800',
  EXTERNAL: 'bg-purple-100 text-purple-800',
};

export default function Badge({ status, variant }: BadgeProps) {
  const { t } = useLanguage();
  const cls = variant
    ? { success: 'bg-primary-100 text-primary-800', warning: 'bg-yellow-100 text-yellow-800', danger: 'bg-red-100 text-red-800', default: 'bg-gray-100 text-gray-700' }[variant]
    : variants[status] || 'bg-gray-100 text-gray-700';

  return (
    <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-semibold ${cls}`}>
      {t(status.toLowerCase())}
    </span>
  );
}
