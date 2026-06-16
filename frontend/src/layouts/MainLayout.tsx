import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Users, Wallet, HandCoins, UserPlus,
  FileText, Activity, Settings, LogOut, Menu, X, FileUp,
} from 'lucide-react';
import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useLanguage } from '../contexts/LanguageContext';
import LanguageSwitcher from '../components/LanguageSwitcher';

const adminLinks = [
  { to: '/admin', icon: LayoutDashboard, label: 'dashboard', end: true },
  { to: '/admin/members', icon: Users, label: 'members' },
  { to: '/admin/contributions', icon: Wallet, label: 'contributions' },
  { to: '/admin/loans', icon: HandCoins, label: 'loans' },
  { to: '/admin/borrowers', icon: UserPlus, label: 'borrowers' },
  { to: '/admin/reports', icon: FileText, label: 'reports' },
  { to: '/admin/activity', icon: Activity, label: 'activity' },
  { to: '/admin/import', icon: FileUp, label: 'excel.import' },
  { to: '/admin/settings', icon: Settings, label: 'settings' },
];

const memberLinks = [
  { to: '/member', icon: LayoutDashboard, label: 'dashboard', end: true },
  { to: '/member/contributions', icon: Wallet, label: 'contributions' },
  { to: '/member/loans', icon: HandCoins, label: 'loans' },
  { to: '/member/profile', icon: Settings, label: 'profile' },
];

export default function Layout({ children }: { children: React.ReactNode }) {
  const { user, logout, isAdmin } = useAuth();
  const { t } = useLanguage();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const links = isAdmin ? adminLinks : memberLinks;

  const NavItems = ({ mobile }: { mobile?: boolean }) => (
    <>
      {links.map(({ to, icon: Icon, label, end }) => (
        <NavLink
          key={to}
          to={to}
          end={end}
          onClick={() => mobile && setSidebarOpen(false)}
          className={({ isActive }) =>
            `flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-colors ${
              isActive ? 'bg-primary-600 text-white' : 'text-dark-300 hover:bg-dark-800 hover:text-white'
            }`
          }
        >
          <Icon size={20} />
          {t(label)}
        </NavLink>
      ))}
    </>
  );

  return (
    <div className="min-h-screen flex flex-col md:flex-row">
      {/* Desktop Sidebar */}
      <aside className="hidden md:flex md:w-64 md:flex-col bg-dark-900 text-white min-h-screen">
        <div className="p-5 border-b border-dark-700">
          <h1 className="text-lg font-bold text-primary-400 leading-tight">{t('app.name')}</h1>
          <p className="text-xs text-dark-400 mt-1">{t('mandal.fund.management')}</p>
        </div>
        <nav className="flex-1 p-4 space-y-1"><NavItems /></nav>
        <div className="p-4 border-t border-dark-700">
          <p className="text-sm text-dark-300 truncate">{user?.name}</p>
          <p className="text-xs text-dark-500">{t(user?.role?.toLowerCase() ?? '')}</p>
          <div className="mt-3"><LanguageSwitcher dark /></div>
          <button onClick={logout} className="mt-3 flex items-center gap-2 text-sm text-red-400 hover:text-red-300">
            <LogOut size={16} /> {t('logout')}
          </button>
        </div>
      </aside>

      {/* Mobile Header */}
      <header className="md:hidden bg-dark-900 text-white px-4 py-3 flex items-center justify-between sticky top-0 z-40">
        <h1 className="max-w-[11rem] text-sm font-bold leading-tight text-primary-400">{t('app.name')}</h1>
        <LanguageSwitcher dark />
        <button onClick={() => setSidebarOpen(!sidebarOpen)}>
          {sidebarOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </header>

      {/* Mobile Sidebar Overlay */}
      {sidebarOpen && (
        <div className="md:hidden fixed inset-0 z-50 flex">
          <div className="fixed inset-0 bg-black/50" onClick={() => setSidebarOpen(false)} />
          <aside className="relative w-64 bg-dark-900 text-white min-h-full p-4">
            <nav className="space-y-1 mt-8"><NavItems mobile /></nav>
            <div className="mt-6"><LanguageSwitcher dark /></div>
            <button onClick={logout} className="mt-6 flex items-center gap-2 text-sm text-red-400">
              <LogOut size={16} /> {t('logout')}
            </button>
          </aside>
        </div>
      )}

      {/* Main Content */}
      <main className="flex-1 pb-20 md:pb-6">
        <div className="p-4 md:p-6 max-w-7xl mx-auto">{children}</div>
      </main>

      {/* Mobile Bottom Nav */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 flex justify-around py-2 z-40">
        {links.slice(0, 4).map(({ to, icon: Icon, label, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `flex flex-col items-center gap-0.5 px-2 py-1 text-xs ${
                isActive ? 'text-primary-600' : 'text-dark-400'
              }`
            }
          >
            <Icon size={20} />
            <span>{t(label)}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
