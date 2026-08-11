import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { ThemeProvider } from './contexts/ThemeContext';
import { ToastProvider } from './components/ui/Toast';
import AppLayout from './components/layout/AppLayout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import ConsultationPage from './pages/ConsultationPage';
import HistoryPage from './pages/HistoryPage';
import KnowledgePage from './pages/KnowledgePage';
import ProfilePage from './pages/ProfilePage';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { token, loading } = useAuth();
  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-10 w-10 animate-spin rounded-full border-2 border-brand-400 border-t-transparent" />
      </div>
    );
  }
  if (!token) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

function PublicRoute({ children }: { children: React.ReactNode }) {
  const { token, loading } = useAuth();
  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-10 w-10 animate-spin rounded-full border-2 border-brand-400 border-t-transparent" />
      </div>
    );
  }
  if (token) return <Navigate to="/dashboard" replace />;
  return <>{children}</>;
}

export default function App() {
  return (
    <BrowserRouter>
      <ThemeProvider>
        <ToastProvider>
          <AuthProvider>
          <Routes>
            <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
            <Route path="/register" element={<PublicRoute><RegisterPage /></PublicRoute>} />
            <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/consultation" element={<ConsultationPage />} />
              <Route path="/history" element={<HistoryPage />} />
              <Route path="/history/:id" element={<HistoryPage />} />
              <Route path="/knowledge" element={<KnowledgePage />} />
              <Route path="/profile" element={<ProfilePage />} />
            </Route>
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </AuthProvider>
      </ToastProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
}
