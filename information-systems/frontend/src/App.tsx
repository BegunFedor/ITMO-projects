import { Navigate, Route, Routes } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import type { UserRole } from './api/types';
import { useAuth } from './auth/AuthContext';
import { Layout, navigation } from './components/Layout';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { RequestCardPage } from './pages/RequestCardPage';
import { NotificationsPage } from './pages/NotificationsPage';
import { MyRequestsPage } from './pages/resident/MyRequestsPage';
import { NewRequestPage } from './pages/resident/NewRequestPage';
import { MetersPage } from './pages/resident/MetersPage';
import { AnnouncementsPage } from './pages/resident/AnnouncementsPage';
import { MyComplaintsPage } from './pages/resident/MyComplaintsPage';
import { CompanyRequestsPage } from './pages/company/CompanyRequestsPage';
import { ResidentsPage } from './pages/company/ResidentsPage';
import { CompanyAnnouncementsPage } from './pages/company/CompanyAnnouncementsPage';
import { CompanyOrdersPage } from './pages/company/CompanyOrdersPage';
import { TasksPage } from './pages/employee/TasksPage';
import { OverduePage } from './pages/inspector/OverduePage';
import { InspectorComplaintsPage } from './pages/inspector/InspectorComplaintsPage';
import { InspectorOrdersPage } from './pages/inspector/InspectorOrdersPage';

function RoleRoute({ role, element }: { role: UserRole; element: JSX.Element }) {
  const { user } = useAuth();
  return user?.role === role ? element : <Navigate to="/" replace />;
}

export function App() {
  const { user, loading } = useAuth();
  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 20 }}><CircularProgress /></Box>;
  }
  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }
  const r = (role: UserRole, element: JSX.Element) => <RoleRoute role={role} element={element} />;
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Navigate to={navigation[user.role][0].to} replace />} />
        <Route path="requests/:id" element={<RequestCardPage />} />
        <Route path="notifications" element={<NotificationsPage />} />
        <Route path="my/requests" element={r('RESIDENT', <MyRequestsPage />)} />
        <Route path="my/requests/new" element={r('RESIDENT', <NewRequestPage />)} />
        <Route path="my/meters" element={r('RESIDENT', <MetersPage />)} />
        <Route path="my/announcements" element={r('RESIDENT', <AnnouncementsPage />)} />
        <Route path="my/complaints" element={r('RESIDENT', <MyComplaintsPage />)} />
        <Route path="company/requests" element={r('COMPANY_ADMIN', <CompanyRequestsPage />)} />
        <Route path="company/residents" element={r('COMPANY_ADMIN', <ResidentsPage />)} />
        <Route path="company/announcements" element={r('COMPANY_ADMIN', <CompanyAnnouncementsPage />)} />
        <Route path="company/orders" element={r('COMPANY_ADMIN', <CompanyOrdersPage />)} />
        <Route path="work/tasks" element={r('EMPLOYEE', <TasksPage />)} />
        <Route path="inspection/overdue" element={r('INSPECTOR', <OverduePage />)} />
        <Route path="inspection/complaints" element={r('INSPECTOR', <InspectorComplaintsPage />)} />
        <Route path="inspection/orders" element={r('INSPECTOR', <InspectorOrdersPage />)} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
