import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { 
  Plus, Briefcase, Users, Edit3, Trash2, CheckCircle2, 
  AlertCircle, ToggleLeft, ToggleRight, Calendar, MapPin, ChevronRight 
} from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { recruiterApi } from '../services/api'

export default function RecruiterJobsPage() {
  const navigate = useNavigate()
  const [jobs, setJobs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    fetchRecruiterJobs()
  }, [])

  const fetchRecruiterJobs = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await recruiterApi.getJobs()
      setJobs(data.content || data || [])
    } catch (err) {
      setError('Failed to load posted jobs.')
    } finally {
      setLoading(false)
    }
  }

  const handleDeleteJob = async (jobId) => {
    if (!window.confirm('Are you sure you want to delete this job posting?')) return
    try {
      await recruiterApi.deleteJob(jobId)
      setJobs(jobs.filter(j => j.id !== jobId))
      setSuccess('Job deleted successfully.')
      setTimeout(() => setSuccess(''), 3000)
    } catch (err) {
      setError('Failed to delete job.')
    }
  }

  const handleToggleActive = async (job) => {
    try {
      const updated = await recruiterApi.updateJob(job.id, {
        title: job.title,
        description: job.description,
        location: job.location,
        workMode: job.workMode,
        jobType: job.jobType,
        active: !job.active,
      })
      setJobs(jobs.map(j => j.id === job.id ? updated : j))
    } catch (err) {
      setError('Failed to toggle job active status.')
    }
  }

  return (
    <div className="cl-page-recruiter">
      <Navbar />

      <main className="cl-main">
        <div className="cl-panel p-5 sm:p-6 space-y-6">
          {/* Header */}
        <div className="cl-card p-5 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-lg bg-[#ebf0ff] text-[#1a56db] text-xs font-semibold mb-2">
              <Briefcase className="w-3.5 h-3.5" /> Job Postings
            </div>
            <h1 className="text-2xl sm:text-3xl font-bold text-[#0f172a] tracking-tight">Your Job Listings</h1>
            <p className="text-sm text-[#64748b] mt-1">Manage active postings, view candidate applications, and edit job requirements.</p>
          </div>

          <Link
            to="/recruiter/jobs/new"
            className="cl-btn-primary text-sm flex items-center gap-2"
          >
            <Plus className="w-4 h-4" /> Post New Job
          </Link>
        </div>

        {error && (
          <div className="cl-alert cl-alert-error" role="alert">
            <AlertCircle className="w-5 h-5 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div className="cl-alert cl-alert-success" role="status">
            <CheckCircle2 className="w-5 h-5 flex-shrink-0" />
            <span>{success}</span>
          </div>
        )}

        {loading ? (
          <div className="space-y-4">
            {[1, 2, 3].map((n) => (
              <div key={n} className="cl-card p-6 animate-pulse space-y-3">
                <div className="h-5 bg-slate-200 rounded w-1/3"></div>
                <div className="h-4 bg-slate-100 rounded w-1/4"></div>
              </div>
            ))}
          </div>
        ) : jobs.length > 0 ? (
          <div className="space-y-4">
            {jobs.map((job) => (
              <motion.div
                key={job.id}
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                className="cl-card p-6 flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
              >
                <div className="space-y-2 flex-1 min-w-0">
                  <div className="flex items-center gap-3 flex-wrap">
                    <h3 className="text-lg font-bold text-[#0f172a]">{job.title}</h3>
                    <span className={`cl-badge ${job.active ? 'cl-badge-green' : 'cl-badge-slate'}`}>
                      {job.active ? 'Active' : 'Closed'}
                    </span>
                  </div>

                  <div className="flex flex-wrap items-center gap-3 text-xs text-[#64748b]">
                    {job.location && (
                      <span className="flex items-center gap-1">
                        <MapPin className="w-3.5 h-3.5" /> {job.location}
                      </span>
                    )}
                    {job.workMode && <span>• {job.workMode}</span>}
                    {job.jobType && <span>• {job.jobType}</span>}
                    {job.createdAt && (
                      <span className="flex items-center gap-1">
                        • <Calendar className="w-3.5 h-3.5" /> Posted {new Date(job.createdAt).toLocaleDateString()}
                      </span>
                    )}
                  </div>
                </div>

                <div className="flex flex-wrap items-center gap-2.5 w-full md:w-auto justify-end pt-3 md:pt-0 border-t md:border-0 border-[#f1f4f9]">
                  <button
                    onClick={() => handleToggleActive(job)}
                    className="p-2 text-[#64748b] hover:text-[#0f172a] bg-[#f8f9fb] hover:bg-[#f1f5f9] rounded-xl border border-[#e4e8ef] transition-colors"
                    title={job.active ? 'Deactivate Job' : 'Activate Job'}
                    aria-label={job.active ? 'Deactivate job' : 'Activate job'}
                  >
                    {job.active ? (
                      <ToggleRight className="w-5 h-5 text-[#059669]" />
                    ) : (
                      <ToggleLeft className="w-5 h-5 text-[#94a3b8]" />
                    )}
                  </button>

                  <button
                    onClick={() => navigate(`/recruiter/jobs/${job.id}/applicants`)}
                    className="cl-btn-primary text-xs py-2 px-3.5 flex items-center gap-1.5"
                  >
                    <Users className="w-3.5 h-3.5" /> Applicants
                  </button>

                  <button
                    onClick={() => navigate(`/recruiter/jobs/${job.id}`)}
                    className="p-2 bg-white hover:bg-[#F1F4F2] text-[#202824] rounded border border-[#D9E1DB] transition-colors"
                    title="Edit Job"
                    aria-label="Edit job"
                  >
                    <Edit3 className="w-4 h-4" />
                  </button>

                  <button
                    onClick={() => handleDeleteJob(job.id)}
                    className="p-2 bg-white hover:bg-rose-50 text-rose-600 rounded border border-rose-200 transition-colors"
                    title="Delete Job"
                    aria-label="Delete job"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </motion.div>
            ))}
          </div>
        ) : (
          <div className="cl-empty">
            <div className="w-14 h-14 rounded-full bg-[#0E5C34]/10 border border-[#0E5C34]/20 flex items-center justify-center mx-auto mb-4 text-[#0E5C34]">
              <Briefcase className="w-7 h-7" />
            </div>
            <h3 className="text-base font-bold text-[#0f172a]">No jobs posted yet</h3>
            <p className="text-sm text-[#64748b] mt-1.5 max-w-sm mx-auto">
              Create your first job listing to start receiving candidate applications and AI matches.
            </p>
            <div className="mt-6">
              <Link to="/recruiter/jobs/new" className="cl-btn-primary inline-flex items-center gap-2">
                <Plus className="w-4 h-4" /> Post Your First Job
              </Link>
            </div>
          </div>
        )}
        </div>
      </main>

      <Footer />
    </div>
  )
}
