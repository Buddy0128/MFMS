import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import AdminDashboard from './pages/AdminDashboard';
import MemberDashboard from './pages/MemberDashboard';
import MembersPage from './pages/MembersPage';
import MemberDetailPage from './pages/MemberDetailPage';
import ContributionsPage from './pages/ContributionsPage';
import LoansPage from './pages/LoansPage';
import LoanDetailPage from './pages/LoanDetailPage';
import BorrowersPage from './pages/BorrowersPage';
import BorrowerDetailPage from './pages/BorrowerDetailPage';
import ReportsPage from './pages/ReportsPage';
import ActivityPage from './pages/ActivityPage';
import SettingsPage from './pages/SettingsPage';
import ProfilePage from './pages/ProfilePage';
import MemberContributionsPage from './pages/MemberContributionsPage';
import MemberLoansPage from './pages/MemberLoansPage';
import NotFoundPage from './pages/NotFoundPage';
import DataImportPage from './pages/DataImportPage';
import { LanguageProvider } from './contexts/LanguageContext';

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } },
});

function RootRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/member'} replace />;
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <LanguageProvider>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/" element={<RootRedirect />} />
              <Route path="/login" element={<LoginPage />} />

              <Route element={<ProtectedRoute role="ADMIN" />}>
                <Route path="/admin" element={<AdminDashboard />} />
                <Route path="/admin/members" element={<MembersPage />} />
                <Route path="/admin/members/:id" element={<MemberDetailPage />} />
                <Route path="/admin/contributions" element={<ContributionsPage />} />
                <Route path="/admin/loans" element={<LoansPage />} />
                <Route path="/admin/loans/:id" element={<LoanDetailPage />} />
                <Route path="/admin/borrowers" element={<BorrowersPage />} />
                <Route path="/admin/borrowers/:id" element={<BorrowerDetailPage />} />
                <Route path="/admin/reports" element={<ReportsPage />} />
                <Route path="/admin/activity" element={<ActivityPage />} />
                <Route path="/admin/import" element={<DataImportPage />} />
                <Route path="/admin/settings" element={<SettingsPage />} />
              </Route>

              <Route element={<ProtectedRoute role="MEMBER" />}>
                <Route path="/member" element={<MemberDashboard />} />
                <Route path="/member/contributions" element={<MemberContributionsPage />} />
                <Route path="/member/loans" element={<MemberLoansPage />} />
                <Route path="/member/profile" element={<ProfilePage />} />
              </Route>

              <Route path="*" element={<NotFoundPage />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </LanguageProvider>
    </QueryClientProvider>
  );
}
