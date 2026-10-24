import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { 
  Users, Sparkles, Filter, Download, CheckCircle2, AlertCircle, 
  Search, ShieldCheck, ChevronRight, BarChart2, Check, XCircle 
} from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { recruiterApi, aiApi } from '../services/api'
import { Link } from 'react-router-dom'

export default function RecruiterMatchesPage() {
  const [jobs, setJobs] = useState([])
  const [selectedJobId, setSelectedJobId] = useState('')
  const [candidates, setCandidates] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // Filters
  const [minScore, setMinScore] = useState(0)
  const [skillFilter, setSkillFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [candidateSearch, setCandidateSearch] = useState('')

  const fetchRecruiterJobs = async () => {
    try {
      const res = await recruiterApi.getJobs(0, 100)
      const jobsList = res?.content || (Array.isArray(res) ? res : [])
      setJobs(jobsList)
      setError('')
      if (jobsList.length > 0) {
        setSelectedJobId(jobsList[0].id)
      }
    } catch (err) {
      console.error('Failed to fetch recruiter jobs:', err)
      setError('Failed to load recruiter jobs.')
    }
  }

  const fetchRankedCandidates = async (jobId) => {
    setLoading(true)
    setError('')
    try {
      const ranked = await aiApi.getRankedCandidates(jobId)
      setCandidates(ranked || [])
    } catch (err) {
      console.error('Failed to fetch ranked candidates:', err)
      setError(err.response?.data?.message || 'Failed to fetch AI ranked candidates.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchRecruiterJobs()
  }, [])

  useEffect(() => {
    if (selectedJobId) {
      fetchRankedCandidates(selectedJobId)
    } else {
      setCandidates([])
    }
  }, [selectedJobId])

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
      setError('Failed to download resume.')
    }
  }

  const handleStatusChange = async (applicationId, newStatus) => {
    try {
      await recruiterApi.updateApplicationStatus(applicationId, { status: newStatus })
      setCandidates(candidates.map(c => c.applicationId === applicationId ? { ...c, status: newStatus } : c))
    } catch (err) {
      console.error('Failed to update candidate status:', err)
      setError(err?.response?.data?.message || 'Failed to update candidate status.')
    }
  }

  // Filter candidates locally
  const filteredCandidates = candidates.filter(c => {
    const scoreMatch = (c.overallMatchScore || 0) >= minScore
    const statusMatch = !statusFilter || c.status === statusFilter
    const nameMatch = !candidateSearch || 
      c.candidateName?.toLowerCase().includes(candidateSearch.toLowerCase()) || 
      c.candidateEmail?.toLowerCase().includes(candidateSearch.toLowerCase())
    
    const skillMatch = !skillFilter || (
      c.matchedSkills?.some(s => s.toLowerCase().includes(skillFilter.toLowerCase())) ||
      c.applicantSkills?.some(s => s.toLowerCase().includes(skillFilter.toLowerCase()))
    )

    return scoreMatch && statusMatch && nameMatch && skillMatch
  })

  return (
    <div className="cl-page-recruiter">
      <Navbar />

      <main className="cl-main">
        <div className="cl-panel p-5 sm:p-6 space-y-6">
          {/* Header */}
        <div className="cl-card p-5 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-lg bg-[#ebf0ff] text-[#1a56db] text-xs font-semibold mb-2">
              <Sparkles className="w-3.5 h-3.5 text-[#1a56db]" /> Candidate Discovery
            </div>
            <h1 className="text-2xl sm:text-3xl font-bold text-[#0f172a] tracking-tight">AI Ranked Candidate Matcher</h1>
            <p className="text-sm text-[#64748b] mt-1">
              Review candidates auto-ranked by skills match, experience, education, and semantic relevance.
            </p>
          </div>

          {/* Job Select Dropdown */}
          <div className="w-full md:w-auto">
            <label htmlFor="job-selector" className="block text-xs font-medium text-[#64748b] mb-1.5">Select Job Listing:</label>
            <select
              id="job-selector"
              value={selectedJobId}
              onChange={(e) => setSelectedJobId(e.target.value)}
              className="cl-input font-medium py-2 px-3 min-w-[240px]"
            >
              {jobs.map(j => (
                <option key={j.id} value={j.id}>{j.title} ({j.companyName || 'Company'})</option>
              ))}
            </select>
          </div>
        </div>

        {error && (
          <div className="cl-alert cl-alert-error" role="alert">
            <AlertCircle className="w-5 h-5 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Filter Controls Bar */}
        <div className="cl-card p-5 space-y-4">
          <div className="flex items-center gap-2 text-xs font-bold text-[#0f172a]">
            <Filter className="w-4 h-4 text-[#1a56db]" /> Filter Candidates
          </div>
          
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            
            {/* Candidate Search */}
            <div>
              <label htmlFor="cand-search" className="block text-xs text-[#64748b] mb-1 font-medium">Search Candidate</label>
              <div className="cl-input-group">
                <div className="cl-input-icon-left">
                  <Search className="w-3.5 h-3.5 text-[#94a3b8]" />
                </div>
                <input
                  id="cand-search"
                  type="text"
                  placeholder="Name or email..."
                  value={candidateSearch}
                  onChange={(e) => setCandidateSearch(e.target.value)}
                  className="cl-input cl-input-with-icon py-2 text-xs"
                />
              </div>
            </div>

            {/* Min Match Score */}
            <div>
              <div className="flex justify-between text-xs text-[#64748b] mb-1 font-medium">
                <span>Min Match Score:</span>
                <span className="font-bold text-[#1a56db]">{minScore}%</span>
              </div>
              <input
                type="range"
                min="0"
                max="100"
                value={minScore}
                onChange={(e) => setMinScore(Number(e.target.value))}
                className="w-full accent-[#1a56db] mt-2"
                aria-label="Minimum match score slider"
              />
            </div>

            {/* Skill Filter */}
            <div>
              <label htmlFor="skill-filter" className="block text-xs text-[#64748b] mb-1 font-medium">Filter by Skill</label>
              <input
                id="skill-filter"
                type="text"
                placeholder="e.g. Java, Docker..."
                value={skillFilter}
                onChange={(e) => setSkillFilter(e.target.value)}
                className="cl-input py-2 text-xs"
              />
            </div>

            {/* Application Status Filter */}
            <div>
              <label htmlFor="status-filter" className="block text-xs text-[#64748b] mb-1 font-medium">Application Status</label>
              <select
                id="status-filter"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="cl-input py-2 text-xs font-medium"
              >
                <option value="">All Statuses</option>
                <option value="APPLIED">Applied</option>
                <option value="UNDER_REVIEW">Under Review</option>
                <option value="SHORTLISTED">Shortlisted</option>
                <option value="INTERVIEW">Interview</option>
                <option value="SELECTED">Selected</option>
                <option value="REJECTED">Rejected</option>
              </select>
            </div>

          </div>
        </div>

        {/* Candidate List */}
        {loading ? (
          <div className="space-y-4">
            {[1, 2, 3].map((n) => (
              <div key={n} className="cl-card animate-pulse space-y-4">
                <div className="h-5 bg-slate-200/50 rounded w-1/3"></div>
                <div className="h-4 bg-slate-100/50 rounded w-1/4"></div>
              </div>
            ))}
          </div>
        ) : filteredCandidates.length > 0 ? (
          <div className="space-y-4">
            {filteredCandidates.map((cand, index) => {
              const score = Math.round(cand.overallMatchScore ?? cand.matchScore ?? 0)
              const scoreBadge = score >= 85 ? 'cl-badge-green' : score >= 65 ? 'cl-badge-blue' : 'cl-badge-amber'

              return (
                <motion.div 
                  key={cand.applicationId}
                  initial={{ opacity: 0, y: 6 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="cl-card space-y-5 transition-all"
                >
                  <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                    
                    <div className="flex items-start gap-4">
                      {/* Rank Circle */}
                      <div className="w-10 h-10 rounded-full bg-[#0E5C34]/10 border border-[#0E5C34]/20 flex items-center justify-center font-bold text-sm text-[#0E5C34] shrink-0">
                        #{index + 1}
                      </div>

                      <div>
                        <div className="flex items-center gap-3">
                          <h3 className="text-lg font-bold text-[#0f172a]">{cand.candidateName || 'Candidate'}</h3>
                          <span className={`cl-badge ${scoreBadge} font-bold`}>
                            {score}% Match
                          </span>
                        </div>
                        <p className="text-xs text-[#64748b] mt-1">
                          {cand.candidateEmail || cand.email} • Applied {new Date(cand.appliedAt).toLocaleDateString()}
                        </p>
                      </div>
                    </div>

                    {/* Actions & Status Dropdown */}
                    <div className="flex items-center gap-3 w-full md:w-auto justify-end">
                      <select
                        value={cand.status || cand.applicationStatus || 'APPLIED'}
                        onChange={(e) => handleStatusChange(cand.applicationId, e.target.value)}
                        className="cl-input py-1.5 px-3 text-xs font-semibold w-auto"
                        aria-label="Update candidate status"
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

                  {/* AI Explanation & Criteria Breakdown */}
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs pt-3 border-t border-[#e4e8ef]/60">
                    
                    {/* Matching / Missing Skills */}
                    <div className="space-y-2">
                      <span className="font-semibold text-[#059669] flex items-center gap-1">
                        <Check className="w-3.5 h-3.5" /> Top Matching Skills:
                      </span>
                      <div className="flex flex-wrap gap-1">
                        {cand.matchedSkills?.length > 0 ? (
                          cand.matchedSkills.map((s, i) => (
                            <span key={i} className="px-2 py-0.5 bg-[#ecfdf5] text-[#059669] border border-[#a7f3d0] rounded text-[10px] font-medium">
                              {s}
                            </span>
                          ))
                        ) : (
                          <span className="text-[#94a3b8] italic">No exact skill overlap</span>
                        )}
                      </div>

                      {cand.missingRequiredSkills?.length > 0 && (
                        <div className="pt-1">
                          <span className="font-semibold text-[#e11d48] flex items-center gap-1">
                            <XCircle className="w-3.5 h-3.5" /> Missing Required:
                          </span>
                          <div className="flex flex-wrap gap-1 mt-1">
                            {cand.missingRequiredSkills.map((s, i) => (
                              <span key={i} className="px-2 py-0.5 bg-[#fff1f2] text-[#e11d48] border border-[#fecdd3] rounded text-[10px] font-medium">
                                {s}
                              </span>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>

                    {/* AI Explanation summary */}
                    <div className="cl-card-nested p-3 text-[#334155] text-[11px] leading-relaxed">
                      <span className="font-bold text-[#0E5C34] block mb-1">AI Match Summary:</span>
                      {cand.explanation || 'Calculated based on skill match and semantic similarity.'}
                    </div>

                    {/* Breakdown Meters */}
                    <div className="cl-card-nested p-3 space-y-1.5">
                      <span className="font-bold text-[#0f172a] text-[11px] block">Criteria Breakdown:</span>
                      <div className="grid grid-cols-2 gap-x-3 gap-y-1.5 text-[11px]">
                        <div>Skill: <span className="font-bold text-[#0E5C34]">{cand.skillScore != null && !isNaN(cand.skillScore) ? Math.round(cand.skillScore) : 0}%</span></div>
                        <div>Semantic: <span className="font-bold text-[#0891b2]">{cand.semanticScore != null && !isNaN(cand.semanticScore) ? Math.round(cand.semanticScore) : 0}%</span></div>
                        <div>Experience: <span className="font-bold text-[#059669]">{cand.experienceScore != null && !isNaN(cand.experienceScore) ? Math.round(cand.experienceScore) : 0}%</span></div>
                        <div>Education: <span className="font-bold text-[#7c3aed]">{cand.educationScore != null && !isNaN(cand.educationScore) ? Math.round(cand.educationScore) : 0}%</span></div>
                      </div>
                    </div>

                  </div>

                </motion.div>
              )
            })}
          </div>
        ) : (
          <div className="cl-empty">
            <div className="w-14 h-14 rounded-full bg-[#0E5C34]/10 border border-[#0E5C34]/20 flex items-center justify-center mx-auto mb-4 text-[#0E5C34]">
              <Users className="w-7 h-7" />
            </div>
            <h3 className="text-base font-bold text-[#0f172a]">No candidate matches found</h3>
            <p className="text-sm text-[#64748b] mt-1.5 max-w-sm mx-auto">
              Try adjusting your score filter, searching for different keywords, or selecting another job listing.
            </p>
          </div>
        )}
        </div>
      </main>

      <Footer />
    </div>
  )
}
