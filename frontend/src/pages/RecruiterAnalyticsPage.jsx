import { useState, useEffect } from 'react'
import { 
  BarChart3, TrendingUp, Briefcase, Users, 
  Calendar, Award, AlertCircle, ArrowUpRight 
} from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { aiApi } from '../services/api'
import { Link } from 'react-router-dom'

export default function RecruiterAnalyticsPage() {
  const [analytics, setAnalytics] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [days, setDays] = useState(30)

  const fetchAnalytics = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await aiApi.getRecruiterAnalytics(days)
      setAnalytics(data || {
        activeJobs: 0,
        totalApplicants: 0,
        shortlisted: 0,
        interviews: 0,
        selected: 0,
        rejected: 0,
        applicationTrend: [],
        jobPerformance: []
      })
    } catch (err) {
      console.error('Failed to fetch analytics:', err)
      setError(err.response?.data?.message || 'Failed to load recruiter analytics data.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchAnalytics()
  }, [days])

  // Calculate maximum application count for scaling
  const maxTrendValue = analytics?.applicationTrend?.reduce((max, p) => Math.max(max, p.applications), 1) || 1

  return (
    <div className="cl-page-recruiter">
      <Navbar />

      <main className="cl-main">
        <div className="cl-panel p-5 sm:p-6 space-y-6">
          {/* Header */}
        <div className="cl-card p-5 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <div 
              className="cl-pipeline-badge inline-flex items-center gap-2 px-3 py-1 rounded-none bg-[#EAF5EE] text-[#0E5C34] text-xs font-semibold mb-2"
              style={{ borderRadius: 0 }}
            >
              <BarChart3 className="w-3.5 h-3.5" /> Pipeline Analytics
            </div>
            <h1 className="text-2xl sm:text-3xl font-bold text-[#0f172a] tracking-tight">Recruiter Analytics & Insights</h1>
            <p className="text-sm text-[#64748b] mt-1">
              Real-time recruitment metrics aggregated from posted jobs, applications, and AI match scores.
            </p>
          </div>

          {/* Time Range Selector */}
          <div 
            className="cl-segmented-control flex items-center gap-1.5 bg-[#f8f9fb] border border-[#e4e8ef] p-1 rounded-none text-xs font-semibold"
            style={{ borderRadius: 0 }}
          >
            {[7, 30, 90].map((d) => (
              <button
                key={d}
                type="button"
                onClick={() => setDays(d)}
                style={{ borderRadius: 0 }}
                className={`cl-segmented-item px-3 py-1.5 rounded-none transition-all ${
                  days === d 
                    ? 'bg-[#0E5C34] text-white shadow-sm font-semibold' 
                    : 'text-[#64748b] hover:text-[#0f172a] hover:bg-white/80'
                }`}
              >
                Last {d} Days
              </button>
            ))}
          </div>
        </div>

        {error && (
          <div className="cl-alert cl-alert-error" role="alert">
            <AlertCircle className="w-5 h-5 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {loading ? (
          <div className="space-y-6">
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
              {[1, 2, 3, 4, 5, 6].map((i) => (
                <div key={i} className="cl-card p-5 animate-pulse space-y-3">
                  <div className="h-4 bg-slate-100 rounded w-1/2"></div>
                  <div className="h-8 bg-slate-200 rounded w-3/4"></div>
                </div>
              ))}
            </div>
          </div>
        ) : analytics ? (
          <div className="space-y-6">
            
            {/* Top Overview KPI Cards */}
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
              
              <div className="cl-card p-5 space-y-2">
                <span className="text-xs text-[#64748b] font-medium block">Active Jobs</span>
                <div className="text-2xl sm:text-3xl font-bold text-[#0E5C34] mono">{analytics.activeJobs}</div>
                <span className="text-[11px] text-[#0E5C34] font-semibold flex items-center gap-1">
                  <Briefcase className="w-3 h-3" /> Open Listings
                </span>
              </div>

              <div className="cl-card p-5 space-y-2">
                <span className="text-xs text-[#64748b] font-medium block">Total Applicants</span>
                <div className="text-2xl sm:text-3xl font-bold text-[#0891b2] mono">{analytics.totalApplicants}</div>
                <span className="text-[11px] text-[#0891b2] font-semibold flex items-center gap-1">
                  <Users className="w-3 h-3" /> Submissions
                </span>
              </div>

              <div className="cl-card p-5 space-y-2">
                <span className="text-xs text-[#64748b] font-medium block">Shortlisted</span>
                <div className="text-2xl sm:text-3xl font-bold text-[#7c3aed] mono">{analytics.shortlisted}</div>
                <span className="text-[11px] text-[#7c3aed] font-semibold">Qualified</span>
              </div>

              <div className="cl-card p-5 space-y-2">
                <span className="text-xs text-[#64748b] font-medium block">Interviews</span>
                <div className="text-2xl sm:text-3xl font-bold text-[#d97706] mono">{analytics.interviews}</div>
                <span className="text-[11px] text-[#d97706] font-semibold flex items-center gap-1">
                  <Calendar className="w-3 h-3" /> Scheduled
                </span>
              </div>

              <div className="cl-card p-5 space-y-2">
                <span className="text-xs text-[#64748b] font-medium block">Selected</span>
                <div className="text-2xl sm:text-3xl font-bold text-[#059669] mono">{analytics.selected}</div>
                <span className="text-[11px] text-[#059669] font-semibold flex items-center gap-1">
                  <Award className="w-3 h-3" /> Hired
                </span>
              </div>

              <div className="cl-card p-5 space-y-2">
                <span className="text-xs text-[#64748b] font-medium block">Rejected</span>
                <div className="text-2xl sm:text-3xl font-bold text-[#e11d48] mono">{analytics.rejected}</div>
                <span className="text-[11px] text-[#e11d48] font-semibold">Closed</span>
              </div>

            </div>

            {/* Application Trend Chart */}
            <div className="cl-card p-6 space-y-4">
              <div>
                <h3 className="text-base font-bold text-[#0f172a] flex items-center gap-2">
                  <TrendingUp className="w-4 h-4 text-[#0E5C34]" /> Application Inflow Trend (Last {days} Days)
                </h3>
                <p className="text-xs text-[#64748b] mt-0.5">Daily applicant volume across your active job postings.</p>
              </div>

              {/* Bar Chart Container */}
              <div className="h-52 w-full bg-[#f8f9fb]/60 p-4 rounded-md border border-[#e4e8ef] flex items-end justify-between gap-2 overflow-x-auto">
                {analytics.applicationTrend?.map((point, i) => {
                  const heightPct = Math.round((point.applications / maxTrendValue) * 100)
                  return (
                    <div key={i} className="flex-1 flex flex-col items-center gap-1.5 h-full justify-end group min-w-[14px]">
                      <div className="text-[10px] text-[#64748b] font-bold opacity-0 group-hover:opacity-100 transition-opacity">
                        {point.applications}
                      </div>
                      <div 
                        className="w-full bg-[#0E5C34] group-hover:bg-[#0A4527] rounded-t transition-all duration-300"
                        style={{ height: `${Math.max(6, heightPct)}%` }}
                        title={`${point.date}: ${point.applications} applications`}
                      />
                      <span className="text-[9px] text-[#94a3b8] font-mono hidden sm:block truncate w-full text-center">
                        {point.date.substring(5)}
                      </span>
                    </div>
                  )
                })}
              </div>
            </div>

            {/* Job Performance Breakdown Table */}
            <div className="cl-card p-6 space-y-4">
              <div className="flex justify-between items-center border-b border-[#f1f4f9] pb-4">
                <h3 className="text-base font-bold text-[#0f172a]">Job Posting Performance Breakdown</h3>
                <Link to="/recruiter/jobs" className="text-xs font-semibold text-[#0E5C34] hover:text-[#0A4527] flex items-center gap-1">
                  Manage Jobs <ArrowUpRight className="w-3.5 h-3.5" />
                </Link>
              </div>

              {analytics.jobPerformance?.length > 0 ? (
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs text-[#334155]">
                    <thead className="bg-[#f8f9fb] text-[#64748b] uppercase text-[10px] tracking-wider border-b border-[#e4e8ef]">
                      <tr>
                        <th className="p-3 font-semibold">Job Title</th>
                        <th className="p-3 font-semibold">Company</th>
                        <th className="p-3 text-center font-semibold">Status</th>
                        <th className="p-3 text-center font-semibold">Total Applicants</th>
                        <th className="p-3 text-center font-semibold">Shortlisted</th>
                        <th className="p-3 text-center font-semibold">Interviews</th>
                        <th className="p-3 text-center font-semibold">Selected</th>
                        <th className="p-3 text-center font-semibold">Avg Match Score</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#f1f4f9]">
                      {analytics.jobPerformance.map((job) => (
                        <tr key={job.jobId} className="hover:bg-[#f8f9fb] transition-colors">
                          <td className="p-3 font-bold text-[#0f172a]">{job.title}</td>
                          <td className="p-3 text-[#64748b]">{job.companyName || '—'}</td>
                          <td className="p-3 text-center">
                            <span className={`cl-badge ${job.active ? 'cl-badge-green' : 'cl-badge-slate'}`}>
                              {job.active ? 'Active' : 'Closed'}
                            </span>
                          </td>
                          <td className="p-3 text-center font-bold text-[#0891b2]">{job.totalApplicants}</td>
                          <td className="p-3 text-center text-[#7c3aed] font-semibold">{job.shortlisted}</td>
                          <td className="p-3 text-center text-[#d97706] font-semibold">{job.interviews}</td>
                          <td className="p-3 text-center text-[#059669] font-bold">{job.selected}</td>
                          <td className="p-3 text-center">
                            <span className="cl-badge cl-badge-blue font-bold">
                              {job.averageMatchScore > 0 ? `${Math.round(job.averageMatchScore)}%` : 'N/A'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="text-center py-8 text-xs text-[#64748b]">
                  No job posting performance data available yet.
                </div>
              )}
            </div>

          </div>
        ) : null}
        </div>
      </main>

      <Footer />
    </div>
  )
}
