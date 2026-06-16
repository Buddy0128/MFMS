import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import Layout from '../layouts/MainLayout';

interface ProtectedRouteProps {
  role?: 'ADMIN' | 'MEMBER';
}

export default function ProtectedRoute({ role }: ProtectedRouteProps) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (role && user.role !== role) {
    return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/member'} replace />;
  }
  return <Layout><Outlet /></Layout>;
}
