import { useState, useEffect } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Bell, LogOut, Menu, X, ChevronDown } from 'lucide-react'
import { notificationApi } from '../services/api'

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [unreadCount, setUnreadCount] = useState(0)
  const [mobileOpen, setMobileOpen] = useState(false)

  useEffect(() => {
    if (isAuthenticated) fetchUnreadCount()
  }, [isAuthenticated, location.pathname])

  useEffect(() => {
    setMobileOpen(false)
  }, [location.pathname])

  const fetchUnreadCount = async () => {
    try {
      const res = await notificationApi.getUnreadCount()
      setUnreadCount(res?.unreadCount || 0)
    } catch (_) {}
  }

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  const isSeeker = user?.role === 'JOB_SEEKER'
  const isRecruiter = user?.role === 'RECRUITER'
  const dashboardPath = isRecruiter ? '/recruiter/dashboard' : '/seeker/dashboard'

  const isActive = (path) => {
    if (path === '/') return location.pathname === '/'
    return location.pathname === path || location.pathname.startsWith(path + '/')
  }

  // Public nav links
  const publicLinks = [
    { to: '/jobs', label: 'Find Jobs' },
    { to: '/register', label: 'Companies' },
  ]

  // Seeker nav links
  const seekerLinks = [
    { to: '/seeker/dashboard', label: 'Overview' },
    { to: '/applicant/jobs', label: 'Find Jobs' },
    { to: '/applicant/applications', label: 'Applications' },
    { to: '/applicant/resume', label: 'Resumes' },
    { to: '/applicant/saved', label: 'Saved Jobs' },
    { to: '/applicant/profile', label: 'Profile' },
  ]

  // Recruiter nav links
  const recruiterLinks = [
    { to: '/recruiter/dashboard', label: 'Overview' },
    { to: '/recruiter/jobs', label: 'Jobs' },
    { to: '/recruiter/matches', label: 'Candidates' },
    { to: '/recruiter/shortlisted', label: 'Shortlisted' },
    { to: '/recruiter/analytics', label: 'Analytics' },
  ]

  const navLinks = isAuthenticated
    ? (isRecruiter ? recruiterLinks : seekerLinks)
    : publicLinks

  const initials = user?.fullName
    ? user.fullName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()
    : 'U'

  const NavLink = ({ to, label }) => (
    <Link
      to={to}
      className={[
        'text-sm font-medium transition-colors px-1 py-0.5 relative',
        isActive(to)
          ? 'text-[#11643B] font-semibold after:absolute after:bottom-[-1.15rem] after:left-0 after:right-0 after:h-[2px] after:bg-[#11643B] after:rounded-full'
          : 'text-[#626B65] hover:text-[#11643B]',
      ].join(' ')}
    >
      {label}
    </Link>
  )

  return (
    <>
      <nav className="sticky top-0 z-50 cl-nav">
        <div className="max-w-[76rem] mx-auto px-4 sm:px-6 lg:px-8 h-14 flex items-center justify-between gap-4">

          {/* Brand — Clean badge mark */}
          <Link
            to="/"
            className="flex items-center gap-2.5 shrink-0 group"
            aria-label="CareerLens home"
          >
            <div className="w-8 h-8 rounded bg-[#11643B] text-white font-bold text-xs flex items-center justify-center shadow-sm">
              CL
            </div>
            <span className="text-base font-bold text-[#202824] tracking-tight">
              Career<span className="text-[#11643B]">Lens</span>
            </span>
          </Link>

          {/* Desktop Nav */}
          <div className="hidden lg:flex items-center gap-6 flex-1 ml-6 border-l border-[#D9E1DB] pl-6">
            {navLinks.map(link => (
              <NavLink key={link.to} {...link} />
            ))}
          </div>

          {/* Right side */}
          <div className="flex items-center gap-2 shrink-0">
            {isAuthenticated ? (
              <>
                {/* Notifications bell */}
                <Link
                  to="/applicant/notifications"
                  className="relative p-2 text-[#626B65] hover:text-[#11643B] hover:bg-[#E8F0E9] rounded transition-colors"
                  aria-label={`Notifications${unreadCount > 0 ? `, ${unreadCount} unread` : ''}`}
                >
                  <Bell className="w-[18px] h-[18px]" />
                  {unreadCount > 0 && (
                    <span className="absolute -top-0.5 -right-0.5 w-4 h-4 rounded-full bg-[#11643B] text-white text-[10px] font-bold flex items-center justify-center">
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </Link>

                {/* User avatar + dropdown */}
                <div className="hidden md:flex items-center gap-2.5 pl-3 border-l border-[#D9E1DB] ml-1">
                  <div
                    className="w-8 h-8 rounded-full bg-[#E8F0E9] border border-[#D9E1DB] flex items-center justify-center text-[#11643B] text-xs font-bold shrink-0 select-none"
                    aria-label={user?.fullName}
                  >
                    {initials}
                  </div>
                  <div className="hidden lg:block text-left leading-snug">
                    <p className="text-xs font-semibold text-[#202824] truncate max-w-[120px]">{user?.fullName}</p>
                    <p className="text-[10px] text-[#626B65]">
                      {isRecruiter ? 'Recruiter' : 'Job Seeker'}
                    </p>
                  </div>
                  <button
                    onClick={handleLogout}
                    className="p-1.5 text-[#8F9992] hover:text-[#dc2626] hover:bg-[#FEE2E2] rounded transition-colors ml-0.5"
                    title="Sign out"
                    aria-label="Sign out"
                  >
                    <LogOut className="w-4 h-4" />
                  </button>
                </div>

                {/* Mobile menu toggle */}
                <button
                  className="lg:hidden p-2 text-[#626B65] hover:text-[#202824] hover:bg-[#E8F0E9] rounded transition-colors"
                  onClick={() => setMobileOpen(!mobileOpen)}
                  aria-label="Toggle navigation menu"
                  aria-expanded={mobileOpen}
                >
                  {mobileOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
                </button>
              </>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/login"
                  className="hidden sm:flex text-sm font-medium text-[#202824] hover:text-[#11643B] px-3 py-2 rounded hover:bg-[#E8F0E9] transition-colors"
                >
                  Sign in
                </Link>
                <Link
                  to="/register"
                  className="cl-btn-primary text-sm px-4 py-2 bg-[#11643B] hover:bg-[#0B4628]"
                >
                  Create account
                </Link>
                {/* Mobile menu toggle for public */}
                <button
                  className="sm:hidden p-2 text-[#626B65] hover:text-[#202824] hover:bg-[#E8F0E9] rounded transition-colors"
                  onClick={() => setMobileOpen(!mobileOpen)}
                  aria-label="Toggle navigation menu"
                  aria-expanded={mobileOpen}
                >
                  {mobileOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
                </button>
              </div>
            )}
          </div>
        </div>
      </nav>

      {/* Mobile Drawer */}
      <div
        className={`fixed inset-0 z-40 lg:hidden transition-all duration-300 ${mobileOpen ? 'pointer-events-auto' : 'pointer-events-none'}`}
        aria-hidden={!mobileOpen}
        role="dialog"
        aria-modal="true"
        aria-label="Navigation menu"
      >
        {/* Backdrop */}
        <div
          className={`absolute inset-0 bg-[#0f172a]/40 transition-opacity duration-300 ${mobileOpen ? 'opacity-100' : 'opacity-0'}`}
          onClick={() => setMobileOpen(false)}
        />

        {/* Drawer panel */}
        <div
          className={`absolute left-0 top-0 h-full w-72 max-w-[85vw] bg-white shadow-xl flex flex-col transition-transform duration-300 ease-out ${mobileOpen ? 'translate-x-0' : '-translate-x-full'}`}
        >
          {/* Drawer header */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-[#D9E1DB]">
            <Link to="/" className="flex items-center gap-2" onClick={() => setMobileOpen(false)}>
              <div className="w-7 h-7 rounded bg-[#11643B] text-white font-bold text-xs flex items-center justify-center">
                CL
              </div>
              <span className="text-sm font-bold text-[#202824]">Career<span className="text-[#11643B]">Lens</span></span>
            </Link>
            <button
              onClick={() => setMobileOpen(false)}
              className="p-1.5 rounded hover:bg-[#E8F0E9] text-[#626B65] transition-colors"
              aria-label="Close menu"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          {/* Drawer nav */}
          <nav className="flex-1 overflow-y-auto px-4 py-4 space-y-0.5">
            {!isAuthenticated && (
              <>
                <MobileNavLink to="/jobs" label="Find Jobs" onClose={() => setMobileOpen(false)} isActive={isActive} />
                <MobileNavLink to="/login" label="Sign in" onClose={() => setMobileOpen(false)} isActive={isActive} />
                <div className="pt-3 pb-1">
                  <Link
                    to="/register"
                    onClick={() => setMobileOpen(false)}
                    className="cl-btn-primary w-full justify-center text-sm bg-[#11643B] hover:bg-[#0B4628]"
                  >
                    Create account
                  </Link>
                </div>
              </>
            )}

            {isAuthenticated && (
              <>
                {navLinks.map(link => (
                  <MobileNavLink key={link.to} to={link.to} label={link.label} onClose={() => setMobileOpen(false)} isActive={isActive} />
                ))}
                <div className="pt-2">
                  <MobileNavLink
                    to="/applicant/notifications"
                    label={`Notifications${unreadCount > 0 ? ` (${unreadCount})` : ''}`}
                    onClose={() => setMobileOpen(false)}
                    isActive={isActive}
                  />
                </div>
              </>
            )}
          </nav>

          {/* Drawer footer */}
          {isAuthenticated && (
            <div className="px-5 py-4 border-t border-[#D9E1DB] flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-full bg-[#E8F0E9] border border-[#D9E1DB] flex items-center justify-center text-[#11643B] text-xs font-bold shrink-0">
                  {initials}
                </div>
                <div>
                  <p className="text-xs font-semibold text-[#202824] truncate max-w-[140px]">{user?.fullName}</p>
                  <p className="text-[10px] text-[#626B65]">{isRecruiter ? 'Recruiter' : 'Job Seeker'}</p>
                </div>
              </div>
              <button
                onClick={handleLogout}
                className="p-2 text-[#8F9992] hover:text-[#dc2626] hover:bg-[#FEE2E2] rounded transition-colors"
                aria-label="Sign out"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          )}
        </div>
      </div>
    </>
  )
}

function MobileNavLink({ to, label, onClose, isActive }) {
  const active = isActive(to)
  return (
    <Link
      to={to}
      onClick={onClose}
      className={[
        'flex items-center px-3 py-2.5 rounded text-sm font-medium transition-colors',
        active
          ? 'bg-[#E8F0E9] text-[#11643B] font-semibold'
          : 'text-[#202824] hover:bg-[#E8F0E9] hover:text-[#11643B]',
      ].join(' ')}
    >
      {label}
    </Link>
  )
}
