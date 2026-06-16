import { ReactNode } from 'react';
import { TrendingUp, TrendingDown } from 'lucide-react';
import { formatCurrency } from '../utils/format';

interface StatCardProps {
  title: string;
  value: string | number;
  icon?: ReactNode;
  trend?: 'up' | 'down';
  subtitle?: string;
  color?: 'green' | 'blue' | 'orange' | 'red' | 'gray';
  currency?: boolean;
}

const colorMap = {
  green: 'bg-primary-50 text-primary-700 border-primary-100',
  blue: 'bg-blue-50 text-blue-700 border-blue-100',
  orange: 'bg-orange-50 text-orange-700 border-orange-100',
  red: 'bg-red-50 text-red-700 border-red-100',
  gray: 'bg-gray-50 text-dark-700 border-gray-100',
};

export default function StatCard({ title, value, icon, trend, subtitle, color = 'green', currency = true }: StatCardProps) {
  const displayValue = typeof value === 'number' && currency ? formatCurrency(value) : value;
  return (
    <div className={`card border ${colorMap[color].split(' ').slice(2).join(' ')}`}>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-dark-500 font-medium">{title}</p>
          <p className="text-2xl font-bold mt-1 text-dark-900">{displayValue}</p>
          {subtitle && <p className="text-xs text-dark-400 mt-1">{subtitle}</p>}
        </div>
        <div className={`p-2.5 rounded-lg ${colorMap[color].split(' ').slice(0, 2).join(' ')}`}>
          {icon || (trend === 'up' ? <TrendingUp size={20} /> : trend === 'down' ? <TrendingDown size={20} /> : null)}
        </div>
      </div>
    </div>
  );
}
