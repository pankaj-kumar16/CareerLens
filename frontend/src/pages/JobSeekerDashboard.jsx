import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import {
  Search, Briefcase, FileText, AlertCircle,
  ArrowRight, UserCheck, Upload, CheckCircle2
} from 'lucide-react'
import { Link } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { useAuth } from '../context/AuthContext'
import { profileApi, jobSeekerApi } from '../services/api'

export default function JobSeekerDashboard() {
  const { user } = useAuth()
  const [completeness, setCompleteness] = useState(null)
  const [applications, setApplications] = useState([])
  const [loadingProfile, setLoadingProfile] = useState(true)
  const [loadingApps, setLoadingApps] = useState(true)

  useEffect(() => {
    profileApi.getCompleteness()
      .then(setCompleteness)
      .catch(() => {})
      .finally(() => setLoadingProfile(false))

    jobSeekerApi.getApplications()
      .then(data => setApplications(data || []))
      .catch(() => {})
      .finally(() => setLoadingApps(false))
  }, [])

  const firstName = user?.fullName?.split(' ')[0] || 'there'
  const recentApps = applications.slice(0, 5)
  const pendingApps = applications.filter(a => ['APPLIED', 'UNDER_REVIEW'].includes(a.status)).length

  const completenessPercent = completeness?.percentage ?? null
  const completenessNeedsWork = completenessPercent !== null && completenessPercent < 80

  return (
    <div className="cl-page-jobseeker">
      <Navbar />

      <main className="cl-main">
        
        {/* Translucent Main Dashboard Panel */}
        <div className="cl-panel p-5 sm:p-6 space-y-6">

          {/* Welcome header & quick action banner */}
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.35 }}
            className="flex flex-col md:flex-row md:items-center justify-between gap-6 pb-6 border-b border-[#E4E2DB]"
          >
            <div>
              <div className="cl-eyebrow mb-1">APPLICANT DASHBOARD</div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-[#1B211D]">
                Welcome back, {firstName}
              </h1>
              <p className="text-sm text-[#5B655E] mt-1">
                Here's what's happening with your job search and applications today.
              </p>
            </div>
            <div className="flex items-center gap-3 shrink-0">
              <Link to="/applicant/jobs" className="cl-btn-secondary text-sm font-semibold">
                <Search className="w-4 h-4 text-[#0E5C34]" />
                Find jobs
              </Link>
              <Link to="/applicant/resume" className="cl-btn-primary text-sm font-semibold shadow-sm">
                <Upload className="w-4 h-4" />
                Manage resumes
              </Link>
            </div>
          </motion.div>

          {/* Profile completeness — show if incomplete */}
          {!loadingProfile && completenessNeedsWork && (
            <div className="bg-[#FEF3C7] border border-[#FDE68A] rounded px-5 py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-lg bg-[#F59E0B]/20 flex items-center justify-center shrink-0 mt-0.5">
                  <UserCheck className="w-4 h-4 text-[#B45309]" />
                </div>
                <div>
                  <p className="text-sm font-bold text-[#92400E]">
                    Your profile is {completenessPercent}% complete
                  </p>
                  <p className="text-xs text-[#B45309] mt-0.5">
                    {completeness?.tip || 'A complete profile improves your visibility to recruiters.'}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-4 shrink-0">
                <div className="flex items-center gap-2 hidden sm:flex">
                  <div className="w-24 h-1.5 bg-[#FDE68A] rounded-full overflow-hidden">
                    <div
                      className="h-full bg-[#B45309] rounded-full transition-all duration-700"
                      style={{ width: `${completenessPercent}%` }}
                    />
                  </div>
                  <span className="text-xs font-bold text-[#92400E] mono">{completenessPercent}%</span>
                </div>
                <Link
                  to="/applicant/profile"
                  className="text-xs font-bold text-[#B45309] hover:text-[#92400E] transition-colors flex items-center gap-1 uppercase tracking-wider"
                >
                  Complete profile <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            </div>
          )}

          {/* Grid of Dashboard Action Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {[
              {
                to: '/applicant/jobs',
                icon: Search,
                label: 'Find Jobs',
                desc: 'Browse active listings',
                count: 'Explore roles',
              },
              {
                to: '/applicant/applications',
                icon: FileText,
                label: 'Applications',
                desc: 'Track progress & updates',
                count: loadingApps ? '—' : `${applications.length} total`,
              },
              {
                to: '/applicant/resume',
                icon: Upload,
                label: 'Resumes',
                desc: 'Manage versions & ATS',
                count: 'Inspect score',
              },
              {
                to: '/applicant/saved',
                icon: Briefcase,
                label: 'Saved Jobs',
                desc: 'Bookmarked positions',
                count: 'View saved',
              },
            ].map(({ to, icon: Icon, label, desc, count }) => (
              <Link
                key={to}
                to={to}
                className="cl-card p-5 group flex flex-col justify-between h-full"
              >
                <div>
                  <div className="w-10 h-10 rounded-xl bg-[#EAF5EE] border border-[#B9E3C6] flex items-center justify-center mb-4 group-hover:scale-105 transition-transform">
                    <Icon className="w-5 h-5 text-[#0E5C34]" />
                  </div>
                  <h3 className="text-base font-bold text-[#1B211D] group-hover:text-[#0E5C34] transition-colors">
                    {label}
                  </h3>
                  <p className="text-xs text-[#5B655E] mt-1">{desc}</p>
                </div>
                <div className="pt-4 mt-4 border-t border-[#EDEAE3] flex items-center justify-between text-xs font-semibold text-[#0E5C34]">
                  <span className="mono">{count}</span>
                  <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
                </div>
              </Link>
            ))}
          </div>

          {/* Recent Applications List */}
          <section className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-base font-bold text-[#1B211D]">Recent Applications</h2>
              <Link
                to="/applicant/applications"
                className="text-xs font-bold text-[#0E5C34] hover:text-[#0A4527] flex items-center gap-1 transition-colors uppercase tracking-wider"
              >
                View all applications <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            {loadingApps ? (
              <div className="bg-white border border-[#E4E2DB] rounded-xl p-6 space-y-3">
                {[1,2,3].map(i => (
                  <div key={i} className="flex items-center gap-4">
                    <div className="cl-skeleton h-4 w-48 rounded" />
                    <div className="cl-skeleton h-4 w-24 rounded ml-auto" />
                  </div>
                ))}
              </div>
            ) : recentApps.length > 0 ? (
              <div className="cl-card divide-y divide-[#EDEAE3] overflow-hidden">
                {recentApps.map((app) => (
                  <div
                    key={app.id}
                    className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 px-5 py-4 hover:bg-[#EAF5EE]/40 transition-colors"
                  >
                    <div>
                      <p className="text-sm font-bold text-[#1B211D]">{app.jobTitle || 'Job Position'}</p>
                      <p className="text-xs text-[#5B655E] mt-0.5 mono">
                        {app.companyName} · Applied {new Date(app.appliedAt).toLocaleDateString()}
                      </p>
                    </div>
                    <StatusChip status={app.status} />
                  </div>
                ))}
              </div>
            ) : (
              <div className="cl-empty py-10">
                <FileText className="cl-empty-icon" />
                <p className="cl-empty-title">No applications yet</p>
                <p className="cl-empty-text">Browse open positions and apply to start tracking your progress.</p>
                <Link to="/applicant/jobs" className="cl-btn-primary text-sm mt-4">
                  Find jobs <ArrowRight className="w-4 h-4" />
                </Link>
              </div>
            )}
          </section>

          {/* Profile completeness detail — show if loaded */}
          {!loadingProfile && completeness && (
            <section className="space-y-4 pt-4 border-t border-[#E4E2DB]">
              <div className="flex items-center justify-between">
                <h2 className="text-base font-bold text-[#1B211D]">Profile Completeness</h2>
                <Link
                  to="/applicant/profile"
                  className="text-xs font-bold text-[#0E5C34] hover:text-[#0A4527] flex items-center gap-1 transition-colors uppercase tracking-wider"
                >
                  Edit profile <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>

              <div className="cl-card p-6">
                <div className="flex items-center justify-between mb-3">
                  <div>
                    <span className="text-2xl font-bold text-[#1B211D] mono">{completeness.percentage}%</span>
                    <span className="text-sm text-[#5B655E] ml-2">completed</span>
                  </div>
                  {completeness.percentage === 100 && (
                    <span className="cl-badge cl-badge-green">
                      <CheckCircle2 className="w-3 h-3" /> Complete
                    </span>
                  )}
                </div>
                <div className="cl-progress-track mb-2">
                  <div
                    className="cl-progress-fill"
                    style={{ width: `${completeness.percentage}%` }}
                  />
                </div>
                {completeness.tip && (
                  <p className="text-xs text-[#5B655E] mt-2">{completeness.tip}</p>
                )}

                {completeness.sections?.length > 0 && (
                  <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-2 mt-4 pt-4 border-t border-[#EDEAE3]">
                    {completeness.sections.map((sec, i) => (
                      <Link
                        key={i}
                        to="/applicant/profile"
                        className={[
                          'flex items-center gap-2 p-2.5 rounded-lg text-xs font-semibold transition-colors',
                          sec.complete
                            ? 'text-[#0E5C34] bg-[#EAF5EE]'
                            : 'text-[#B45309] bg-[#FEF3C7] hover:bg-[#FDE68A]',
                        ].join(' ')}
                      >
                        {sec.complete
                          ? <CheckCircle2 className="w-3.5 h-3.5 shrink-0" />
                          : <div className="w-3.5 h-3.5 rounded-full border-2 border-current shrink-0" />
                        }
                        <span className="truncate">{sec.name}</span>
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            </section>
          )}

        </div>
      </main>

      <Footer />
    </div>
  )
}

function StatusChip({ status }) {
  const map = {
    SHORTLISTED: { label: 'Shortlisted', cls: 'cl-badge cl-badge-green' },
    INTERVIEW:   { label: 'Interview', cls: 'cl-badge cl-badge-green' },
    SELECTED:    { label: 'Selected', cls: 'cl-badge cl-badge-green' },
    REJECTED:    { label: 'Not selected', cls: 'cl-badge cl-badge-rose' },
    UNDER_REVIEW:{ label: 'Under review', cls: 'cl-badge cl-badge-amber' },
    APPLIED:     { label: 'Applied', cls: 'cl-badge cl-badge-slate' },
  }
  const { label, cls } = map[status] || { label: status, cls: 'cl-badge cl-badge-slate' }
  return <span className={cls}>{label}</span>
}
