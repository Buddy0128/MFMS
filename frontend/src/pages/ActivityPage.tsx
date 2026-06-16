import { useQuery } from '@tanstack/react-query';
import { activityApi } from '../services/services';
import LoadingSpinner from '../components/LoadingSpinner';
import { formatDateTime } from '../utils/format';
import { useLanguage } from '../contexts/LanguageContext';
import { translateAction, translateDescription } from '../utils/translateActivity';

export default function ActivityPage() {
  const { language, t } = useLanguage();
  const { data: logs, isLoading } = useQuery({
    queryKey: ['activity-logs'],
    queryFn: () => activityApi.getAll().then(r => r.data),
  });

  if (isLoading) return <LoadingSpinner fullPage />;

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">{t('activity.logs')}</h1>
      <div className="card divide-y divide-gray-50">
        {logs?.map(log => (
          <div key={log.id} className="py-4 first:pt-0 last:pb-0">
            <div className="flex justify-between items-start">
              <div>
                <p className="font-medium">{translateAction(log.action, t)}</p>
                <p className="text-sm text-dark-500 mt-1">{translateDescription(log.description, language)}</p>
                <p className="text-xs text-primary-600 mt-1">{t('by')}: {log.adminName}</p>
              </div>
              <span className="text-xs text-dark-400 whitespace-nowrap">{formatDateTime(log.createdAt)}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
