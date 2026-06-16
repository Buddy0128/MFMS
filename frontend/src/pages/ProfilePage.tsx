import { useQuery } from '@tanstack/react-query';
import { memberApi } from '../services/services';
import LoadingSpinner from '../components/LoadingSpinner';
import Badge from '../components/Badge';
import { formatDate } from '../utils/format';
import { useLanguage } from '../contexts/LanguageContext';

export default function ProfilePage() {
  const { t } = useLanguage();
  const { data: member, isLoading } = useQuery({
    queryKey: ['member-profile'],
    queryFn: () => memberApi.profile().then(r => r.data),
  });

  if (isLoading) return <LoadingSpinner fullPage />;
  if (!member) return null;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">{t('my.profile')}</h1>
      <div className="card">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center text-primary-700 text-2xl font-bold">
            {member.fullName.charAt(0)}
          </div>
          <div>
            <h2 className="text-xl font-bold">{member.fullName}</h2>
            <p className="text-dark-500">{member.memberCode}</p>
          </div>
        </div>
        <div className="mt-6 space-y-3 text-sm">
          <div className="flex justify-between py-2 border-b"><span className="text-dark-500">{t('phone')}</span><span>{member.phoneNumber}</span></div>
          <div className="flex justify-between py-2 border-b"><span className="text-dark-500">{t('join.date')}</span><span>{formatDate(member.joinDate)}</span></div>
          <div className="flex justify-between py-2"><span className="text-dark-500">{t('status')}</span><Badge status={member.status} /></div>
        </div>
      </div>
    </div>
  );
}
