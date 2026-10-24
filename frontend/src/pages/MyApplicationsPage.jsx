import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { 
  FileText, CheckCircle2, Clock, AlertCircle, Building2, 
  MapPin, Calendar, MessageSquare, Search, ArrowRight
} from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { jobSeekerApi } from '../services/api'
import { Link } from 'react-router-dom'

export default function MyApplicationsPage() {
  const [applications, setApplications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const fetchApplications = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await jobSeekerApi.getApplications()
      setApplications(data || [])
    } catch (err) {
      console.error('Failed to load applications:', err)
      setError(err?.response?.data?.message || 'Failed to load applications. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchApplications()
  }, [])

  const getStatusBadge = (status) => {
    switch (status) {
      case 'SHORTLISTED':
        return <span className="cl-badge cl-badge-green">Shortlisted</span>
      case 'INTERVIEW':
        return <span className="cl-badge cl-badge-green">Interview Scheduled</span>
      case 'SELECTED':
        return <span className="cl-badge cl-badge-green">Selected ✓</span>
      case 'REJECTED':
        return <span className="cl-badge cl-badge-rose">Not Selected</span>
      case 'UNDER_REVIEW':
        return <span className="cl-badge cl-badge-amber">Under Review</span>
      default:
        return <span className="cl-badge cl-badge-slate">Applied</span>
    }
  }

  return (
    <div className="cl-page-jobseeker">
      <Navbar />

      <main className="cl-main">
        <div className="cl-panel p-5 sm:p-6 space-y-6">
          {/* Header */}
        <div className="cl-page-header flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <div className="cl-eyebrow mb-1">
              <FileText className="w-3.5 h-3.5 text-[#0E5C34]" /> Application Tracker
            </div>
            <h1 className="cl-page-title">Your Job Applications</h1>
            <p className="cl-page-subtitle">Track the status and recruiter updates for all positions you've applied to.</p>
          </div>
          <Link to="/applicant/jobs" className="cl-btn-primary text-sm shrink-0">
            <Search className="w-4 h-4" />
            Find more jobs
          </Link>
        </div>

        {error && (
          <div className="cl-alert cl-alert-error" role="alert">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {loading ? (
          <div className="space-y-4">
            {[1, 2, 3].map((n) => (
              <div key={n} className="cl-card p-6 space-y-3">
                <div className="cl-skeleton h-5 w-1/3 rounded" />
                <div className="cl-skeleton h-4 w-1/4 rounded" />
              </div>
            ))}
          </div>
        ) : applications.length > 0 ? (
          <div className="space-y-4">
            {applications.map((app) => (
              <motion.div
                key={app.id}
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                className="cl-card p-6 space-y-4 hover:border-[#B9E3C6] transition-all"
              >
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                  <div className="space-y-1.5 flex-1 min-w-0">
                    <h3 className="text-lg font-bold text-[#1B211D]">{app.jobTitle || 'Job Position'}</h3>
                    <div className="flex flex-wrap items-center gap-4 text-xs text-[#5B655E]">
                      <span className="font-semibold text-[#1B211D] flex items-center gap-1.5">
                        <Building2 className="w-4 h-4 text-[#0E5C34]" /> {app.companyName || 'Company'}
                      </span>
                      {app.location && (
                        <span className="flex items-center gap-1">
                          <MapPin className="w-3.5 h-3.5 text-[#8F9992]" /> {app.location}
                        </span>
                      )}
                      <span className="flex items-center gap-1 mono">
                        <Clock className="w-3.5 h-3.5 text-[#8F9992]" /> Applied {new Date(app.appliedAt).toLocaleDateString()}
                      </span>
                    </div>
                  </div>
                  <div>{getStatusBadge(app.status)}</div>
                </div>

                {(app.interviewDate || app.recruiterNotes) && (
                  <div className="cl-card-nested p-4 space-y-2 text-xs">
                    {app.interviewDate && (
                      <div className="flex items-center gap-2 text-[#0E5C34] font-semibold mono">
                        <Calendar className="w-4 h-4 text-[#0E5C34]" />
                        <span>Interview Scheduled: {new Date(app.interviewDate).toLocaleString()}</span>
                      </div>
                    )}
                    {app.recruiterNotes && (
                      <div className="flex items-start gap-2 text-[#3A4139]">
                        <MessageSquare className="w-4 h-4 text-[#0E5C34] mt-0.5 shrink-0" />
                        <div>
                          <span className="font-semibold text-[#1B211D]">Recruiter Note: </span>
                          {app.recruiterNotes}
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </motion.div>
            ))}
          </div>
        ) : (
          <div className="cl-empty">
            <FileText className="cl-empty-icon" />
            <h3 className="cl-empty-title">No applications yet</h3>
            <p className="cl-empty-text">
              Explore open opportunities and start applying to track your progress here.
            </p>
            <Link to="/applicant/jobs" className="cl-btn-primary text-sm mt-4">
              <Search className="w-4 h-4" /> Browse Jobs <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        )}
        </div>
      </main>

      <Footer />
    </div>
  )
}
