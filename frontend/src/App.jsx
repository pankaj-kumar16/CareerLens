import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import JobSeekerDashboard from './pages/JobSeekerDashboard'
import RecruiterDashboard from './pages/RecruiterDashboard'
import ProtectedRoute from './components/ProtectedRoute'

import ResumePage from './pages/ResumePage'
import ProfilePage from './pages/ProfilePage'
import JobSearchPage from './pages/JobSearchPage'
import JobSeekerMatchesPage from './pages/JobSeekerMatchesPage'
import MyApplicationsPage from './pages/MyApplicationsPage'
import SavedJobsPage from './pages/SavedJobsPage'
import NotificationsPage from './pages/NotificationsPage'

import RecruiterJobsPage from './pages/RecruiterJobsPage'
import RecruiterMatchesPage from './pages/RecruiterMatchesPage'
import RecruiterAnalyticsPage from './pages/RecruiterAnalyticsPage'
import JobCreateEditPage from './pages/JobCreateEditPage'
import JobApplicantsPage from './pages/JobApplicantsPage'
import ShortlistedCandidatesPage from './pages/ShortlistedCandidatesPage'

function DashboardRedirect() {
  const { user, isAuthenticated, loading } = useAuth()

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#F1F4F2]">
        <div className="cl-spinner" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return user?.role === 'RECRUITER' 
    ? <Navigate to="/recruiter/dashboard" replace /> 
    : <Navigate to="/seeker/dashboard" replace />
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/jobs" element={<JobSearchPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          
          <Route path="/dashboard" element={<DashboardRedirect />} />
          
          {/* Job Seeker Protected Routes */}
          <Route
            path="/seeker/dashboard"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <JobSeekerDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/applicant/profile"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/seeker/profile"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/applicant/resume"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <ResumePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/seeker/resume"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <ResumePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/applicant/jobs"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <JobSearchPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/seeker/jobs"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <JobSearchPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/applicant/matches"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <JobSeekerMatchesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/seeker/matches"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <JobSeekerMatchesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/applicant/notifications"
            element={
              <ProtectedRoute>
                <NotificationsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute>
                <NotificationsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/applicant/applications"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <MyApplicationsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/seeker/applications"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <MyApplicationsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/applicant/saved"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <SavedJobsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/seeker/saved"
            element={
              <ProtectedRoute allowedRole="JOB_SEEKER">
                <SavedJobsPage />
              </ProtectedRoute>
            }
          />

          {/* Recruiter Protected Routes */}
          <Route
            path="/recruiter/dashboard"
            element={
              <ProtectedRoute allowedRole="RECRUITER">
                <RecruiterDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/jobs"
            element={
              <ProtectedRoute allowedRole="RECRUITER">
                <RecruiterJobsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/matches"
            element={
              <ProtectedRoute allowedRole="RECRUITER">
                <RecruiterMatchesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/analytics"
            element={
              <ProtectedRoute allowedRole="RECRUITER">
                <RecruiterAnalyticsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/jobs/new"
            element={
              <ProtectedRoute allowedRole="RECRUITER">
                <JobCreateEditPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/jobs/:id"
            element={
              <ProtectedRoute allowedRole="RECRUITER">
                <JobCreateEditPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/jobs/:id/applicants"
            element={
              <ProtectedRoute allowedRole="RECRUITER">
                <JobApplicantsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/shortlisted"
            element={
              <ProtectedRoute allowedRole="RECRUITER">
                <ShortlistedCandidatesPage />
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
