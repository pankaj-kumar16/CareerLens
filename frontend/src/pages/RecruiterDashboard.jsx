import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import {
  Briefcase, Users, Plus, ChevronRight, Award, Calendar, BarChart3
} from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { useAuth } from '../context/AuthContext'
import { aiApi, recruiterApi } from '../services/api'
import { Link, useNavigate } from 'react-router-dom'

export default function RecruiterDashboard() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [analytics, setAnalytics] = useState(null)
  const [jobs, setJobs] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => { fetchDashboardData() }, [])

  const fetchDashboardData = async () => {
    setLoading(true)
    try {
      const [analyticsData, jobsData] = await Promise.all([
        aiApi.getRecruiterAnalytics(30),
        recruiterApi.getJobs(0, 5),
      ])
      setAnalytics(analyticsData)
      setJobs(jobsData.content || jobsData || [])
    } catch (err) {
      console.error('Failed to load dashboard metrics:', err)
    } finally {
      setLoading(false)
    }
  }

  const firstName = user?.fullName?.split(' ')[0] || 'there'

  const stats = [
    {
      label: 'Active jobs',
      value: analytics?.activeJobs ?? '—',
      icon: Briefcase,
      accent: 'text-[#0E5C34]',
      bg: 'bg-[#0E5C34]/15 border border-[#0E5C34]/25',
    },
    {
      label: 'Applicants',
      value: analytics?.totalApplicants ?? '—',
      icon: Users,
      accent: 'text-[#0E5C34]',
      bg: 'bg-[#0E5C34]/15 border border-[#0E5C34]/25',
    },
    {
      label: 'Shortlisted',
      value: analytics?.shortlisted ?? '—',
      icon: Award,
      accent: 'text-[#0E5C34]',
      bg: 'bg-[#0E5C34]/15 border border-[#0E5C34]/25',
    },
    {
      label: 'Interviews',
      value: analytics?.interviews ?? '—',
      icon: Calendar,
      accent: 'text-[#B45309]',
      bg: 'bg-[#B45309]/15 border border-[#B45309]/25',
    },
    {
      label: 'Selected',
      value: analytics?.selected ?? '—',
      icon: Award,
      accent: 'text-[#0E5C34]',
      bg: 'bg-[#0E5C34]/15 border border-[#0E5C34]/25',
    },
  ]

  return (
    <div className="cl-page-recruiter">
      <Navbar />

      <main className="cl-main">
        
        {/* Translucent Main Dashboard Panel */}
        <div className="cl-panel p-5 sm:p-6 space-y-6">

          {/* Header */}
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.35 }}
            className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-[#E4E2DB]"
          >
            <div>
              <div className="cl-eyebrow mb-1">RECRUITER DASHBOARD</div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-[#1B211D]">
                Welcome back, {firstName}
              </h1>
              <p className="text-sm text-[#5B655E] mt-1">
                Manage your active job listings and review incoming candidate applications.
              </p>
            </div>
            <div className="flex items-center gap-3 shrink-0">
              <Link to="/recruiter/analytics" className="cl-btn-secondary text-sm font-semibold">
                <BarChart3 className="w-4 h-4 text-[#0E5C34]" />
                Analytics
              </Link>
              <Link to="/recruiter/jobs/new" className="cl-btn-primary text-sm font-semibold shadow-sm">
                <Plus className="w-4 h-4" />
                Post job
              </Link>
            </div>
          </motion.div>

          {/* Stats — real data only */}
          {!loading && analytics && (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
              {stats.map(({ label, value, icon: Icon, accent, bg }) => (
                <div key={label} className="cl-card p-4.5 flex flex-col justify-between h-full">
                  <div className={`w-9 h-9 rounded ${bg} border border-[#B9E3C6] flex items-center justify-center mb-3`}>
                    <Icon className={`w-4 h-4 ${accent}`} />
                  </div>
                  <div>
                    <p className="text-2xl font-bold text-[#1B211D] mono">{value}</p>
                    <p className="text-xs text-[#5B655E] mt-0.5 font-semibold">{label}</p>
                  </div>
                </div>
              ))}
            </div>
          )}

          {loading && (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
              {[1,2,3,4,5].map(i => (
                <div key={i} className="cl-stat-tile">
                  <div className="cl-skeleton h-9 w-9 rounded-lg mb-3" />
                  <div className="cl-skeleton h-7 w-12 rounded mb-1.5" />
                  <div className="cl-skeleton h-3 w-16 rounded" />
                </div>
              ))}
            </div>
          )}

          {/* Recent job postings */}
          <section className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-base font-bold text-[#1B211D]">Your Job Listings</h2>
              <Link
                to="/recruiter/jobs"
                className="text-xs font-bold text-[#0E5C34] hover:text-[#0A4527] flex items-center gap-1 transition-colors uppercase tracking-wider"
              >
                View all jobs <ChevronRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            {loading ? (
              <div className="cl-card p-5 space-y-4">
                {[1,2,3].map(i => (
                  <div key={i} className="flex items-center gap-4">
                    <div className="cl-skeleton h-4 w-48 rounded" />
                    <div className="cl-skeleton h-4 w-24 rounded ml-auto" />
                    <div className="cl-skeleton h-8 w-28 rounded" />
                  </div>
                ))}
              </div>
            ) : jobs.length > 0 ? (
              <div className="cl-card divide-y divide-[#EDEAE3] overflow-hidden">
                {jobs.map((job) => (
                  <div
                    key={job.id}
                    className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 px-5 py-4 hover:bg-[#EAF5EE]/40 transition-colors"
                  >
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="text-sm font-bold text-[#1B211D] truncate">{job.title}</p>
                        <span
                          className={[
                            'cl-badge shrink-0',
                            job.active ? 'cl-badge-green' : 'cl-badge-slate',
                          ].join(' ')}
                        >
                          {job.active ? 'Active' : 'Closed'}
                        </span>
                      </div>
                      <p className="text-xs text-[#5B655E] mt-0.5 mono">
                        {[job.location, job.workMode, `Posted ${new Date(job.createdAt).toLocaleDateString()}`]
                          .filter(Boolean).join(' · ')}
                      </p>
                    </div>

                    <button
                      onClick={() => navigate(`/recruiter/jobs/${job.id}/applicants`)}
                      className="cl-btn-secondary text-xs py-1.5 px-3 shrink-0 self-start sm:self-center font-semibold"
                    >
                      Applicants <ChevronRight className="w-3.5 h-3.5" />
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <div className="cl-empty py-10">
                <Briefcase className="cl-empty-icon" />
                <p className="cl-empty-title">No job listings yet</p>
                <p className="cl-empty-text">
                  Create your first job listing to start receiving candidate applications.
                </p>
                <Link to="/recruiter/jobs/new" className="cl-btn-primary text-sm mt-4">
                  <Plus className="w-4 h-4" />
                  Post your first job
                </Link>
              </div>
            )}
          </section>

          {/* Quick links */}
          <section className="pt-4 border-t border-[#E4E2DB]">
            <h2 className="text-base font-bold text-[#1B211D] mb-4">Quick Access</h2>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              {[
                { to: '/recruiter/jobs', icon: Briefcase, label: 'All Jobs', accent: 'text-[#0E5C34]', bg: 'bg-[#0E5C34]/15 border border-[#0E5C34]/25' },
                { to: '/recruiter/matches', icon: Users, label: 'Candidates', accent: 'text-[#0E5C34]', bg: 'bg-[#0E5C34]/15 border border-[#0E5C34]/25' },
                { to: '/recruiter/shortlisted', icon: Award, label: 'Shortlisted', accent: 'text-[#0E5C34]', bg: 'bg-[#0E5C34]/15 border border-[#0E5C34]/25' },
                { to: '/recruiter/analytics', icon: BarChart3, label: 'Analytics', accent: 'text-[#B45309]', bg: 'bg-[#B45309]/15 border border-[#B45309]/25' },
              ].map(({ to, icon: Icon, label, accent, bg }) => (
                <Link
                  key={to}
                  to={to}
                  className="cl-card p-4 group"
                >
                  <div className={`w-9 h-9 rounded ${bg} border border-[#B9E3C6] flex items-center justify-center mb-3`}>
                    <Icon className={`w-4 h-4 ${accent}`} />
                  </div>
                  <p className="text-sm font-bold text-[#1B211D] group-hover:text-[#0E5C34] transition-colors">
                    {label}
                  </p>
                </Link>
              ))}
            </div>
          </section>

        </div>
      </main>

      <Footer />
    </div>
  )
}
