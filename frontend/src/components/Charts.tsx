import { Line, Doughnut, Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS, CategoryScale, LinearScale, PointElement,
  LineElement, BarElement, ArcElement, Title, Tooltip, Legend, Filler,
} from 'chart.js';
import type { ChartDataPoint } from '../types';
import { useLanguage } from '../contexts/LanguageContext';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Title, Tooltip, Legend, Filler);

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    x: { grid: { display: false } },
    y: { grid: { color: '#f3f4f6' }, beginAtZero: true },
  },
};

export function ContributionChart({ data }: { data: ChartDataPoint[] }) {
  const { t } = useLanguage();
  const chartData = {
    labels: data.map(d => d.label),
    datasets: [{
      label: t('contributions'),
      data: data.map(d => d.value),
      borderColor: '#16a34a',
      backgroundColor: 'rgba(22, 163, 74, 0.1)',
      fill: true,
      tension: 0.4,
    }],
  };
  return <div className="h-64"><Line data={chartData} options={chartOptions} /></div>;
}

export function InterestChart({ data }: { data: ChartDataPoint[] }) {
  const { t } = useLanguage();
  const chartData = {
    labels: data.map(d => d.label),
    datasets: [{
      label: t('interest.earned'),
      data: data.map(d => d.value),
      backgroundColor: '#22c55e',
      borderRadius: 6,
    }],
  };
  return <div className="h-64"><Bar data={chartData} options={chartOptions} /></div>;
}

export function LoanDistributionChart({ data }: { data: ChartDataPoint[] }) {
  const { t } = useLanguage();
  const chartData = {
    labels: data.map(d => d.category ? t(d.category.toLowerCase()) : d.label),
    datasets: [{
      data: data.map(d => d.value),
      backgroundColor: ['#16a34a', '#6366f1'],
      borderWidth: 0,
    }],
  };
  return (
    <div className="h-64">
      <Doughnut data={chartData} options={{ responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }} />
    </div>
  );
}
