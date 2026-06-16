import { reportApi, downloadBlob } from '../services/services';
import { FileSpreadsheet, FileText } from 'lucide-react';
import { useLanguage } from '../contexts/LanguageContext';

export default function ReportsPage() {
  const { t } = useLanguage();
  const download = async (fn: () => Promise<{ data: Blob }>, filename: string) => {
    const res = await fn();
    downloadBlob(res.data, filename);
  };

  const reports = [
    { title: t('member.report'), desc: t('member.report.desc'), icon: FileSpreadsheet, action: () => download(reportApi.membersExcel, 'members-report.xlsx') },
    { title: t('contribution.report'), desc: t('contribution.report.desc'), icon: FileSpreadsheet, action: () => download(reportApi.contributionsExcel, 'contributions-report.xlsx') },
    { title: t('loan.report'), desc: t('loan.report.desc'), icon: FileSpreadsheet, action: () => download(reportApi.loansExcel, 'loans-report.xlsx') },
    { title: t('fund.report'), desc: t('fund.report.desc'), icon: FileText, action: () => download(reportApi.fundPdf, 'fund-report.pdf') },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">{t('reports')}</h1>
        <p className="text-dark-500 text-sm">{t('reports.help')}</p>
      </div>
      <div className="grid sm:grid-cols-2 gap-4">
        {reports.map(r => (
          <div key={r.title} className="card hover:shadow-md transition cursor-pointer" onClick={r.action}>
            <div className="flex items-start gap-4">
              <div className="p-3 bg-primary-50 rounded-lg text-primary-600"><r.icon size={24} /></div>
              <div>
                <h3 className="font-semibold">{r.title}</h3>
                <p className="text-sm text-dark-400 mt-1">{r.desc}</p>
                <button className="text-primary-600 text-sm font-medium mt-2">{t('download.report')}</button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
