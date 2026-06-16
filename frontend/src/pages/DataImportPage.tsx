import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { CheckCircle2, FileSpreadsheet, Upload, XCircle } from 'lucide-react';
import { importApi } from '../services/services';
import { formatDateTime } from '../utils/format';
import type { ImportResult } from '../types';
import { useLanguage } from '../contexts/LanguageContext';

export default function DataImportPage() {
  const [file, setFile] = useState<File | null>(null);
  const [result, setResult] = useState<ImportResult | null>(null);
  const queryClient = useQueryClient();
  const { t } = useLanguage();

  const { data: history } = useQuery({
    queryKey: ['import-history'],
    queryFn: () => importApi.history().then(response => response.data),
  });

  const importMutation = useMutation({
    mutationFn: (selectedFile: File) => importApi.upload(selectedFile),
    onSuccess: response => {
      setResult(response.data);
      setFile(null);
      queryClient.invalidateQueries();
    },
  });

  const errorMessage = importMutation.error
    ? t('import.failed')
    : null;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">{t('excel.import')}</h1>
        <p className="mt-1 text-sm text-dark-500">
          {t('excel.help')}
        </p>
      </div>

      <section className="card">
        <div className="flex items-start gap-3">
          <div className="rounded-lg bg-primary-50 p-2.5 text-primary-700">
            <FileSpreadsheet size={22} />
          </div>
          <div className="min-w-0 flex-1">
            <h2 className="font-semibold">{t('upload.excel.file')}</h2>
            <p className="mt-1 text-sm text-dark-500">
              {t('excel.columns')}
            </p>
          </div>
        </div>

        <label className="mt-5 flex min-h-32 cursor-pointer flex-col items-center justify-center border border-dashed border-gray-300 p-5 text-center hover:border-primary-500 hover:bg-primary-50/40">
          <Upload className="mb-2 text-primary-600" size={26} />
          <span className="break-all text-sm font-medium">{file ? file.name : t('choose.excel')}</span>
          <span className="mt-1 text-xs text-dark-400">{t('matched.by')}</span>
          <input
            type="file"
            accept=".xlsx,.xls"
            className="sr-only"
            onChange={event => {
              setFile(event.target.files?.[0] ?? null);
              setResult(null);
              importMutation.reset();
            }}
          />
        </label>

        {errorMessage && (
          <div className="mt-4 flex items-start gap-2 bg-red-50 p-3 text-sm text-red-700">
            <XCircle className="mt-0.5 shrink-0" size={17} />
            <span>{errorMessage}</span>
          </div>
        )}

        <button
          type="button"
          className="btn-primary mt-4 flex items-center gap-2"
          disabled={!file || importMutation.isPending}
          onClick={() => file && importMutation.mutate(file)}
        >
          <Upload size={17} />
          {importMutation.isPending ? t('importing') : t('import.recalculate')}
        </button>
      </section>

      {result && (
        <section className="card border-primary-200">
          <div className="flex items-center gap-2 text-primary-700">
            <CheckCircle2 size={20} />
            <h2 className="font-semibold">{t('import.completed')}</h2>
          </div>
          <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
            <Summary label={t('rows.processed')} value={result.totalRowsProcessed} />
            <Summary label={t('new.members')} value={result.newMembersAdded} />
            <Summary label={t('members.updated')} value={result.existingMembersUpdated} />
            <Summary label={t('failed.records')} value={result.failedRecords} danger={result.failedRecords > 0} />
          </div>
          <p className="mt-3 text-xs text-dark-400">{formatDateTime(result.importDateTime)}</p>
          {result.errors.length > 0 && (
            <div className="mt-4 bg-red-50 p-3 text-sm text-red-700">
              {result.errors.map(error => <p key={error}>{error}</p>)}
            </div>
          )}
        </section>
      )}

      <section>
        <h2 className="mb-3 text-lg font-semibold">{t('import.history')}</h2>
        <div className="card overflow-x-auto">
          <table className="w-full min-w-[720px] text-sm">
            <thead>
              <tr className="border-b text-left text-dark-500">
                <th className="pb-3 pr-4">{t('file')}</th>
                <th className="pb-3 pr-4">{t('import.date')}</th>
                <th className="pb-3 pr-4">{t('rows')}</th>
                <th className="pb-3 pr-4">{t('added')}</th>
                <th className="pb-3 pr-4">{t('updated')}</th>
                <th className="pb-3 pr-4">{t('failed')}</th>
                <th className="pb-3">{t('imported.by')}</th>
              </tr>
            </thead>
            <tbody>
              {history?.map(item => (
                <tr key={item.id} className="border-b border-gray-50">
                  <td className="py-3 pr-4 font-medium">{item.fileName}</td>
                  <td className="py-3 pr-4">{formatDateTime(item.importedAt)}</td>
                  <td className="py-3 pr-4">{item.totalRows}</td>
                  <td className="py-3 pr-4">{item.newMembers}</td>
                  <td className="py-3 pr-4">{item.updatedMembers}</td>
                  <td className="py-3 pr-4">{item.failedRecords}</td>
                  <td className="py-3">{item.importedBy}</td>
                </tr>
              ))}
              {history?.length === 0 && (
                <tr><td colSpan={7} className="py-8 text-center text-dark-400">{t('no.imports')}</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

function Summary({ label, value, danger = false }: { label: string; value: number; danger?: boolean }) {
  return (
    <div className="border border-gray-100 bg-gray-50 p-3">
      <p className="text-xs text-dark-500">{label}</p>
      <p className={`mt-1 text-xl font-bold ${danger ? 'text-red-600' : 'text-dark-900'}`}>{value}</p>
    </div>
  );
}
