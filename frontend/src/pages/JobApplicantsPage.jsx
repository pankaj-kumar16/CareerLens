import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { 
  Users, Download, CheckCircle2, AlertCircle, ArrowLeft, 
  BookOpen, Briefcase, Sparkles, Calendar, MessageSquare, ShieldCheck, Check, XCircle 
} from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { recruiterApi, aiApi } from '../services/api'

export default function JobApplicantsPage() {
  const { id: jobId } = useParams()
  const navigate = useNavigate()

  const [job, setJob] = useState(null)
  const [rankedCandidates, setRankedCandidates] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Edit notes state map
  const [notesState, setNotesState] = useState({})
  const [interviewDateState, setInterviewDateState] = useState({})

  useEffect(() => {
    fetchData()
  }, [jobId])

  const fetchData = async () => {
    setLoading(true)
    setError('')
    try {
      const [jobData, rankedData] = await Promise.all([
        recruiterApi.getJobById(jobId),
        aiApi.getRankedCandidates(jobId)
      ])
      setJob(jobData)
      setRankedCandidates(rankedData || [])

      const initialNotes = {}
      const initialDates = {}
      ;(rankedData || []).forEach(cand => {
        initialNotes[cand.applicationId] = cand.recruiterNotes || ''
        initialDates[cand.applicationId] = cand.interviewDate ? cand.interviewDate.substring(0, 16) : ''
      })
      setNotesState(initialNotes)
      setInterviewDateState(initialDates)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load applicants for this job.')
    } finally {
      setLoading(false)
    }
  }

  const handleStatusChange = async (applicationId, newStatus) => {
    setError('')
    setSuccess('')
    try {
      const interviewDate = interviewDateState[applicationId] 
        ? new Date(interviewDateState[applicationId]).toISOString() 
        : null

      const updated = await recruiterApi.updateApplicationStatus(applicationId, {
        status: newStatus,
        interviewDate
      })

      setRankedCandidates(rankedCandidates.map(c => c.applicationId === applicationId ? { ...c, status: updated.status } : c))
      setSuccess(`Application status updated to ${newStatus}.`)
      setTimeout(() => setSuccess(''), 3000)
    } catch (err) {
      setError('Failed to update status.')
    }
  }

  const handleSaveNotes = async (applicationId) => {
    setError('')
    setSuccess('')
    try {
      const notes = notesState[applicationId] || ''
      await recruiterApi.updateApplicationNotes(applicationId, { recruiterNotes: notes })
      setSuccess('Recruiter notes saved.')
      setTimeout(() => setSuccess(''), 3000)
    } catch (err) {
      setError('Failed to save notes.')
    }
  }

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
      setError('Failed to download candidate resume.')
    }
  }

  return (
    <div className="cl-page-recruiter">
      <Navbar />

      <main className="cl-main">
        <div className="cl-panel p-5 sm:p-6 space-y-6">
          <button
          onClick={() => navigate('/recruiter/jobs')}
          className="inline-flex items-center gap-2 text-xs font-semibold text-[#64748b] hover:text-[#0f172a] transition-colors"
        >
          <ArrowLeft className="w-4 h-4" /> Back to My Jobs
        </button>

        <div className="cl-card p-5">
          <span className="cl-badge cl-badge-blue mb-2 font-semibold">
            {job?.companyName || 'Job Listing'}
          </span>
          <h1 className="text-2xl sm:text-3xl font-bold text-[#0f172a] tracking-tight mt-1">
            Applicants for "{job?.title || 'Job'}"
          </h1>
          <p className="text-sm text-[#64748b] mt-1">
            Review candidate applications ranked by AI fit score, manage interview schedules, download resumes, and save recruiter notes.
          </p>
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
              <div key={n} className="cl-card p-5 animate-pulse space-y-4">
                <div className="h-5 bg-slate-200 rounded w-1/3"></div>
                <div className="h-4 bg-slate-100 rounded w-1/4"></div>
              </div>
            ))}
          </div>
        ) : rankedCandidates.length > 0 ? (
          <div className="space-y-5">
            {rankedCandidates.map((cand, idx) => {
              const score = Math.round(cand.overallMatchScore ?? cand.matchScore ?? 0)
              const scoreBadge = score >= 85 ? 'cl-badge-green' : score >= 65 ? 'cl-badge-blue' : 'cl-badge-amber'

              return (
                <motion.div
                  key={cand.applicationId}
                  initial={{ opacity: 0, y: 6 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="cl-card p-5 space-y-5 hover:border-[#B9E3C6] transition-all"
                >
                  {/* Header info */}
                  <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 border-b border-[#f1f4f9] pb-4">
                    <div className="flex items-start gap-4">
                      <div className="w-9 h-9 rounded-full bg-[#f1f5f9] border border-[#e4e8ef] flex items-center justify-center font-bold text-xs text-[#1a56db] shrink-0">
                        #{idx + 1}
                      </div>

                      <div>
                        <div className="flex items-center gap-3">
                          <h3 className="text-lg font-bold text-[#0f172a]">{cand.candidateName || 'Candidate'}</h3>
                          <span className={`cl-badge ${scoreBadge} font-bold`}>
                            {score}% Match
                          </span>
                        </div>
                        <div className="flex flex-wrap items-center gap-4 text-xs text-[#64748b] mt-1">
                          <span>Email: {cand.candidateEmail || cand.email}</span>
                          {(cand.candidatePhone || cand.phone) && <span>Phone: {cand.candidatePhone || cand.phone}</span>}
                          <span>Applied: {new Date(cand.appliedAt).toLocaleDateString()}</span>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-3 w-full md:w-auto justify-end">
                      {/* Status Dropdown */}
                      <select
                        value={cand.status || cand.applicationStatus || 'APPLIED'}
                        onChange={(e) => handleStatusChange(cand.applicationId, e.target.value)}
                        className="cl-input py-1.5 px-3 text-xs font-semibold w-auto"
                        aria-label="Application status"
                      >
                        <option value="APPLIED">Applied</option>
                        <option value="UNDER_REVIEW">Under Review</option>
                        <option value="SHORTLISTED">Shortlisted</option>
                        <option value="INTERVIEW">Interview</option>
                        <option value="SELECTED">Selected</option>
                        <option value="REJECTED">Rejected</option>
                      </select>

                      <button
                        onClick={() => handleDownloadResume(cand.applicationId, cand.resumeOriginalFileName)}
                        className="cl-btn-primary text-xs py-2 px-3.5 flex items-center gap-1.5 shrink-0"
                      >
                        <Download className="w-3.5 h-3.5" /> Resume
                      </button>
                    </div>
                  </div>

                  {/* AI Explanation & Breakdown */}
                  <div className="cl-card-nested p-4 flex flex-col md:flex-row justify-between items-start md:items-center gap-3 text-xs">
                    <div className="space-y-1">
                      <span className="font-bold text-[#0E5C34] flex items-center gap-1 text-xs">
                        <Sparkles className="w-3.5 h-3.5 text-[#0E5C34]" /> AI Match Analysis:
                      </span>
                      <p className="text-[#334155] leading-relaxed text-xs">
                        {cand.explanation || 'Evaluated skills, semantic overlap and experience.'}
                      </p>
                    </div>

                    <div className="flex flex-wrap gap-2 text-[11px] font-semibold shrink-0">
                      <span className="px-2.5 py-1 cl-card-nested rounded text-[#334155]">
                        Skill: {cand.skillScore != null && !isNaN(cand.skillScore) ? Math.round(cand.skillScore) : 0}%
                      </span>
                      <span className="px-2.5 py-1 cl-card-nested rounded text-[#334155]">
                        Semantic: {cand.semanticScore != null && !isNaN(cand.semanticScore) ? Math.round(cand.semanticScore) : 0}%
                      </span>
                      <span className="px-2.5 py-1 cl-card-nested rounded text-[#334155]">
                        Exp: {cand.experienceScore != null && !isNaN(cand.experienceScore) ? Math.round(cand.experienceScore) : 0}%
                      </span>
                    </div>
                  </div>

                  {/* Skills & Education */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
                    <div className="cl-card-nested p-4 space-y-2">
                      <span className="font-bold text-[#0f172a] flex items-center gap-1.5">
                        <Sparkles className="w-3.5 h-3.5 text-[#0E5C34]" /> Candidate Skills
                      </span>
                      {cand.applicantSkills && cand.applicantSkills.length > 0 ? (
                        <div className="flex flex-wrap gap-1">
                          {cand.applicantSkills.map((sk, i) => (
                            <span key={i} className="cl-skill-tag">
                              {sk}
                            </span>
                          ))}
                        </div>
                      ) : (
                        <p className="text-[#94a3b8] italic">No specific skills listed.</p>
                      )}
                    </div>

                    <div className="cl-card-nested p-4 space-y-2">
                      <span className="font-bold text-[#0f172a] flex items-center gap-1.5">
                        <BookOpen className="w-3.5 h-3.5 text-[#0891b2]" /> Candidate Education
                      </span>
                      {cand.applicantEducation && cand.applicantEducation.length > 0 ? (
                        <ul className="space-y-1 text-[#334155]">
                          {cand.applicantEducation.map((edu, i) => <li key={i}>• {edu}</li>)}
                        </ul>
                      ) : (
                        <p className="text-[#94a3b8] italic">No education entries listed.</p>
                      )}
                    </div>
                  </div>

                  {/* Interview Schedule & Notes */}
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs pt-2">
                    <div>
                      <label htmlFor={`interview-${cand.applicationId}`} className="font-semibold text-[#0f172a] mb-1.5 flex items-center gap-1.5">
                        <Calendar className="w-3.5 h-3.5 text-[#1a56db] shrink-0" /> Interview Schedule
                      </label>
                      <input
                        id={`interview-${cand.applicationId}`}
                        type="datetime-local"
                        value={interviewDateState[cand.applicationId] || ''}
                        onChange={(e) => setInterviewDateState(prev => ({ ...prev, [cand.applicationId]: e.target.value }))}
                        onBlur={() => handleStatusChange(cand.applicationId, cand.status)}
                        className="cl-input py-2 text-xs"
                      />
                    </div>

                    <div>
                      <label htmlFor={`notes-${cand.applicationId}`} className="font-semibold text-[#0f172a] mb-1.5 flex items-center gap-1.5">
                        <MessageSquare className="w-3.5 h-3.5 text-[#64748b] shrink-0" /> Recruiter Notes
                      </label>
                      <div className="flex gap-2">
                        <input
                          id={`notes-${cand.applicationId}`}
                          type="text"
                          placeholder="Add private interview feedback..."
                          value={notesState[cand.applicationId] || ''}
                          onChange={(e) => setNotesState(prev => ({ ...prev, [cand.applicationId]: e.target.value }))}
                          className="cl-input py-2 text-xs flex-1"
                        />
                        <button
                          onClick={() => handleSaveNotes(cand.applicationId)}
                          className="cl-btn-secondary py-2 text-xs px-3"
                        >
                          Save
                        </button>
                      </div>
                    </div>
                  </div>

                </motion.div>
              )
            })}
          </div>
        ) : (
          <div className="cl-empty py-12">
            <div className="w-12 h-12 rounded-full bg-[#0E5C34]/15 flex items-center justify-center mx-auto mb-4 text-[#0E5C34]">
              <Users className="w-6 h-6" />
            </div>
            <h3 className="text-base font-bold text-[#1B211D]">No applicants yet</h3>
            <p className="text-sm text-[#5B655E] mt-1.5 max-w-sm mx-auto">
              Candidates who apply for this job posting will appear here automatically sorted by AI fit.
            </p>
          </div>
        )}
        </div>
      </main>

      <Footer />
    </div>
  )
}
