import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { Sparkles, Download, Building2, Calendar, MessageSquare, AlertCircle, Users } from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { recruiterApi } from '../services/api'

export default function ShortlistedCandidatesPage() {
  const [shortlisted, setShortlisted] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const fetchShortlisted = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await recruiterApi.getShortlisted()
      setShortlisted(data || [])
    } catch (err) {
      console.error('Failed to load shortlisted candidates:', err)
      setError(err?.response?.data?.message || 'Failed to load shortlisted candidates. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchShortlisted()
  }, [])

  const handleDownloadResume = async (applicationId, filename) => {
    try {
      const res = await recruiterApi.downloadCandidateResume(applicationId)
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', filename || 'candidate_resume.pdf')
      document.body.appendChild(link)
      link.click()
      link.remove()
    } catch (err) {
      console.error('Failed to download candidate resume:', err)
      setError('Failed to download candidate resume.')
    }
  }

  return (
    <div className="cl-page-recruiter">
      <Navbar />

      <main className="cl-main">
        <div className="cl-panel p-5 sm:p-6 space-y-6">
          {/* Header */}
        <div className="cl-card p-5">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-lg bg-[#ecfdf5] text-[#059669] text-xs font-semibold mb-2">
            <Sparkles className="w-3.5 h-3.5" /> Pipeline Focus
          </div>
          <h1 className="text-2xl sm:text-3xl font-bold text-[#0f172a] tracking-tight">Shortlisted Candidates</h1>
          <p className="text-sm text-[#64748b] mt-1">Review top candidates marked as Shortlisted across all your active job postings.</p>
        </div>

        {error && (
          <div className="cl-alert cl-alert-error" role="alert">
            <AlertCircle className="w-5 h-5 flex-shrink-0" />
            <span>{error}</span>
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
        ) : shortlisted.length > 0 ? (
          <div className="space-y-4">
            {shortlisted.map((app) => (
              <motion.div
                key={app.id}
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                className="cl-card p-6 flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
              >
                <div className="space-y-2 flex-1 min-w-0">
                  <div className="flex items-center gap-3 flex-wrap">
                    <h3 className="text-lg font-bold text-[#0f172a]">{app.applicantName || 'Candidate'}</h3>
                    <span className="cl-badge cl-badge-green font-bold">
                      Shortlisted
                    </span>
                  </div>

                  <div className="flex flex-wrap items-center gap-4 text-xs text-[#64748b]">
                    <span className="font-semibold text-[#334155] flex items-center gap-1.5">
                      <Building2 className="w-3.5 h-3.5 text-[#1a56db]" /> {app.jobTitle || 'Job Position'}
                    </span>
                    <span>Email: {app.applicantEmail}</span>
                    {app.applicantPhone && <span>Phone: {app.applicantPhone}</span>}
                  </div>

                  {app.applicantSkills && app.applicantSkills.length > 0 && (
                    <div className="flex flex-wrap gap-1 pt-1">
                      {app.applicantSkills.map((sk, i) => (
                        <span key={i} className="px-2 py-0.5 bg-[#f1f5f9] border border-[#e4e8ef] rounded text-[11px] text-[#334155] font-medium">
                          {sk}
                        </span>
                      ))}
                    </div>
                  )}
                </div>

                <div className="flex items-center gap-3 w-full md:w-auto justify-end border-t md:border-0 border-[#f1f4f9] pt-3 md:pt-0">
                  <button
                    onClick={() => handleDownloadResume(app.id, app.resumeOriginalFileName)}
                    className="cl-btn-primary text-xs py-2 px-3.5 flex items-center gap-1.5 shrink-0"
                  >
                    <Download className="w-3.5 h-3.5" /> Download Resume
                  </button>
                </div>
              </motion.div>
            ))}
          </div>
        ) : (
          <div className="cl-empty">
            <div className="w-14 h-14 rounded-full bg-[#0E5C34]/10 border border-[#0E5C34]/20 flex items-center justify-center mx-auto mb-4 text-[#0E5C34]">
              <Sparkles className="w-7 h-7" />
            </div>
            <h3 className="text-base font-bold text-[#0f172a]">No shortlisted candidates yet</h3>
            <p className="text-sm text-[#64748b] mt-1.5 max-w-sm mx-auto">
              Change applicant status to Shortlisted in your candidate views to track them here in a single workspace.
            </p>
          </div>
        )}
        </div>
      </main>

      <Footer />
    </div>
  )
}
