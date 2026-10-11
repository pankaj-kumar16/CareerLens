import { useState, useEffect } from 'react'
import { Bookmark, Building2, MapPin, Briefcase, Trash2, AlertCircle, ExternalLink } from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { jobSeekerApi, jobApi } from '../services/api'
import { Link } from 'react-router-dom'

export default function SavedJobsPage() {
  const [savedJobs, setSavedJobs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [applySuccess, setApplySuccess] = useState('')

  useEffect(() => {
    fetchSavedJobs()
  }, [])

  const fetchSavedJobs = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await jobSeekerApi.getSavedJobs()
      setSavedJobs(data || [])
    } catch (err) {
      setError('Failed to load saved jobs.')
    } finally {
      setLoading(false)
    }
  }

  const handleUnsave = async (jobId) => {
    try {
      await jobSeekerApi.unsaveJob(jobId)
      setSavedJobs(savedJobs.filter(s => s.jobId !== jobId))
    } catch (err) {
      setError('Failed to remove saved job.')
    }
  }

  const handleApply = async (jobId) => {
    try {
      await jobApi.apply(jobId)
      setApplySuccess('Application submitted successfully!')
      setTimeout(() => setApplySuccess(''), 4000)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit application.')
    }
  }

  return (
    <div className="cl-page-jobseeker">
      <Navbar />

      <main className="cl-main">
        <div className="cl-panel p-5 sm:p-6 space-y-6">
          {/* Page header */}
        <div className="cl-page-header">
          <div className="flex items-center gap-2 mb-1 text-sm font-semibold text-[#0E5C34]">
            <Bookmark className="w-4 h-4" /> Bookmarks
          </div>
          <h1 className="cl-page-title">Saved Jobs</h1>
          <p className="cl-page-subtitle">Review and apply to your saved opportunities anytime.</p>
        </div>

        {error && (
          <div className="cl-alert cl-alert-error" role="alert">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {applySuccess && (
          <div className="cl-alert cl-alert-success" role="status">
            <span>{applySuccess}</span>
          </div>
        )}

        {loading ? (
          <div className="py-20 flex justify-center items-center">
            <div className="cl-spinner" />
          </div>
        ) : savedJobs.length > 0 ? (
          <div className="space-y-4">
            {savedJobs.map((item) => {
              const job = item.job
              if (!job) return null
              return (
                <div
                  key={item.id}
                  className="cl-card p-5 sm:p-6 flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
                >
                  <div className="space-y-2 flex-1 min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <h3 className="text-base font-bold text-[#0f172a]">{job.title}</h3>
                      {job.workMode && (
                        <span className="cl-badge cl-badge-blue">{job.workMode}</span>
                      )}
                    </div>

                    <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-[#64748b]">
                      <span className="font-medium text-[#334155] flex items-center gap-1.5">
                        <Building2 className="w-4 h-4 text-[#94a3b8]" />
                        {job.companyName}
                      </span>
                      {job.location && (
                        <span className="flex items-center gap-1">
                          <MapPin className="w-3.5 h-3.5" />
                          {job.location}
                        </span>
                      )}
                      {job.jobType && (
                        <span className="flex items-center gap-1">
                          <Briefcase className="w-3.5 h-3.5" />
                          {job.jobType.replace('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase())}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-3 w-full md:w-auto justify-end pt-3 md:pt-0 border-t md:border-0 border-[#e4e8ef]">
                    <button
                      onClick={() => handleUnsave(job.id)}
                      className="p-2.5 rounded-lg border border-[#fecaca] bg-[#fef2f2] text-[#dc2626] hover:bg-[#fee2e2] transition-colors"
                      title="Remove from saved"
                      aria-label="Remove from saved jobs"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>

                    <button
                      onClick={() => handleApply(job.id)}
                      className="cl-btn-primary text-sm py-2 px-5"
                    >
                      Apply Now
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        ) : (
          <div className="cl-empty">
            <Bookmark className="cl-empty-icon" />
            <p className="cl-empty-title">No saved jobs</p>
            <p className="cl-empty-text">
              Bookmark jobs while browsing to save them here for later.
            </p>
            <Link to="/jobs" className="cl-btn-primary text-sm mt-4">
              Browse Jobs <ExternalLink className="w-3.5 h-3.5 ml-1" />
            </Link>
          </div>
        )}
        </div>
      </main>

      <Footer />
    </div>
  )
}
