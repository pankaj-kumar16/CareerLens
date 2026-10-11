import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import {
  Search, MapPin, IndianRupee, Briefcase, Filter, Bookmark,
  CheckCircle2, AlertCircle, Building2, X, BarChart2, Sparkles, ArrowRight, Upload
} from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { jobApi, jobSeekerApi, aiApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { Link, useSearchParams } from 'react-router-dom'

export default function JobSearchPage() {
  const { user } = useAuth()
  const [searchParams] = useSearchParams()
  const initialKeyword = searchParams.get('keyword') || ''
  const initialLocation = searchParams.get('location') || ''

  const [jobs, setJobs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Recommendations state
  const [recommendations, setRecommendations] = useState([])
  const [loadingRecs, setLoadingRecs] = useState(false)
  const [noResume, setNoResume] = useState(false)

  // Filters
  const [keyword, setKeyword] = useState(initialKeyword)
  const [location, setLocation] = useState(initialLocation)
  const [workMode, setWorkMode] = useState('')
  const [jobType, setJobType] = useState('')

  // Selected job detail panel
  const [selectedJob, setSelectedJob] = useState(null)
  const [jobMatch, setJobMatch] = useState(null)
  const [loadingMatch, setLoadingMatch] = useState(false)
  const [applying, setApplying] = useState(false)
  const [savedJobIds, setSavedJobIds] = useState(new Set())

  useEffect(() => {
    fetchJobs({ keyword: initialKeyword, location: initialLocation })
    if (user?.role === 'JOB_SEEKER') {
      fetchSavedJobIds()
      fetchRecommendations()
    }
  }, [])

  const fetchRecommendations = async () => {
    setLoadingRecs(true)
    setNoResume(false)
    try {
      // Primary: use /recommendations endpoint (pre-computed scores)
      const res = await aiApi.getRecommendations(0, 50)
      const list = res.content || res || []
      if (list.length > 0) {
        const sorted = [...list]
          .sort((a, b) => (b.matchScore || 0) - (a.matchScore || 0))
          .slice(0, 5)
        setRecommendations(sorted)
        return
      }
      // Fallback: live match via /matches endpoint
      const matches = await aiApi.getRecommendedMatches()
      if (Array.isArray(matches) && matches.length > 0) {
        const converted = matches.map(m => ({
          jobId: m.job?.id,
          title: m.job?.title,
          companyName: m.job?.companyName,
          location: m.job?.location,
          workMode: m.job?.workMode,
          salaryRange: m.job?.salaryMin
            ? `₹${Number(m.job.salaryMin).toLocaleString('en-IN')} - ₹${Number(m.job.salaryMax).toLocaleString('en-IN')}`
            : 'Competitive',
          jobType: m.job?.jobType,
          matchScore: m.match?.overallScore || 0,
          matchedSkills: m.match?.matchedSkills || [],
          missingRequiredSkills: m.match?.missingRequiredSkills || [],
          explanation: m.match?.explanation || '',
        }))
        const sorted = converted
          .sort((a, b) => b.matchScore - a.matchScore)
          .slice(0, 5)
        setRecommendations(sorted)
      } else {
        setRecommendations([])
      }
    } catch (err) {
      const msg = err.response?.data?.message || ''
      if (err.response?.status === 400 && (msg.toLowerCase().includes('upload a resume') || msg.toLowerCase().includes('please upload'))) {
        setNoResume(true)
      } else {
        setRecommendations([])
      }
    } finally {
      setLoadingRecs(false)
    }
  }

  useEffect(() => {
    if (selectedJob && user?.role === 'JOB_SEEKER') {
      fetchMatchForSelectedJob(selectedJob.id)
    } else {
      setJobMatch(null)
    }
  }, [selectedJob])

  const fetchJobs = async (customParams = null) => {
    setLoading(true)
    setError('')
    try {
      const params = {}
      const kw = customParams?.keyword !== undefined ? customParams.keyword : keyword
      const loc = customParams?.location !== undefined ? customParams.location : location
      const wm = customParams?.workMode !== undefined ? customParams.workMode : workMode
      const jt = customParams?.jobType !== undefined ? customParams.jobType : jobType
      if (kw) params.keyword = kw
      if (loc) params.location = loc
      if (wm) params.workMode = wm
      if (jt) params.jobType = jt
      const res = await jobApi.search(params)
      setJobs(res.content || [])
    } catch (err) {
      setError('Failed to load job listings. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const fetchSavedJobIds = async () => {
    try {
      const res = await jobSeekerApi.getSavedJobs()
      const ids = new Set((res || []).map(s => s.jobId))
      setSavedJobIds(ids)
    } catch (_) {}
  }

  const fetchMatchForSelectedJob = async (jobId) => {
    setLoadingMatch(true)
    try {
      const match = await aiApi.getJobMatch(jobId)
      setJobMatch(match)
    } catch (_) {
      setJobMatch(null)
    } finally {
      setLoadingMatch(false)
    }
  }

  const handleSearchSubmit = (e) => {
    e.preventDefault()
    fetchJobs()
  }

  const handleApply = async (jobId) => {
    if (!user) {
      setError('Please sign in as a job seeker to apply.')
      return
    }
    if (user.role !== 'JOB_SEEKER') {
      setError('Only job seekers can apply for positions.')
      return
    }
    setApplying(true)
    setError('')
    setSuccess('')
    try {
      await jobApi.apply(jobId)
      setSuccess('Application submitted successfully.')
      setSelectedJob(null)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit application. Please make sure you have uploaded a resume.')
    } finally {
      setApplying(false)
    }
  }

  const handleToggleSave = async (jobId) => {
    if (!user || user.role !== 'JOB_SEEKER') return
    const isSaved = savedJobIds.has(jobId)
    try {
      if (isSaved) {
        await jobSeekerApi.unsaveJob(jobId)
        const updated = new Set(savedJobIds)
        updated.delete(jobId)
        setSavedJobIds(updated)
      } else {
        await jobSeekerApi.saveJob(jobId)
        const updated = new Set(savedJobIds)
        updated.add(jobId)
        setSavedJobIds(updated)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update saved job.')
    }
  }

  const handleReset = () => {
    setKeyword('')
    setLocation('')
    setWorkMode('')
    setJobType('')
    // Reset will trigger a new fetch via fetchJobs — reset state then fetch
    setTimeout(() => fetchJobs(), 0)
  }

  return (
    <div className="cl-page-jobseeker">
      <Navbar />

      <main className="cl-main">
        <div className="cl-panel p-5 sm:p-6 space-y-6">
          {/* Page header */}
          <div className="cl-page-header">
            <h1 className="cl-page-title">Find Jobs</h1>
            <p className="cl-page-subtitle">
              Search, filter and discover your next opportunity
            </p>
          </div>

        {/* Alerts */}
        {error && (
          <div className="cl-alert cl-alert-error mb-5" role="alert">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}
        {success && (
          <div className="cl-alert cl-alert-success mb-5" role="status">
            <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
            <span>{success}</span>
          </div>
        )}

        {/* Recommended Jobs for You Section (Job Seekers) */}
        {user?.role === 'JOB_SEEKER' && (
          <div className="mb-8">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
              <div>
                <div className="flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-[#0E5C34]">
                  <Sparkles className="w-4 h-4 text-[#0E5C34]" /> AI Matched
                </div>
                <h2 className="text-xl font-bold text-[#1B211D] mt-0.5">Recommended Jobs for You</h2>
                <p className="text-xs text-[#64748b]">Top openings ranked by highest compatibility with your resume</p>
              </div>
              <Link
                to="/applicant/matches"
                className="text-xs font-semibold text-[#0E5C34] hover:underline flex items-center gap-1 self-start sm:self-auto"
              >
                View all AI matches <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            {loadingRecs ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {[1, 2, 3].map(i => (
                  <div key={i} className="cl-card p-4 space-y-3">
                    <div className="cl-skeleton h-4 w-32 rounded" />
                    <div className="cl-skeleton h-5 w-48 rounded" />
                    <div className="cl-skeleton h-4 w-24 rounded" />
                  </div>
                ))}
              </div>
            ) : noResume ? (
              <div className="cl-card p-5 bg-[#0E5C34]/5 border-[#0E5C34]/20 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-[#0E5C34]/10 flex items-center justify-center text-[#0E5C34] shrink-0">
                    <Sparkles className="w-5 h-5 text-[#0E5C34]" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold text-[#1B211D]">Get AI-Powered Job Recommendations</h3>
                    <p className="text-xs text-[#64748b] mt-0.5">Upload your resume to unlock personalized job matches and compatibility scores.</p>
                  </div>
                </div>
                <Link to="/applicant/resume" className="cl-btn-primary text-xs shrink-0">
                  <Upload className="w-3.5 h-3.5" /> Upload Resume
                </Link>
              </div>
            ) : recommendations.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {recommendations.slice(0, 5).map((rec, index) => {
                  const score = Math.round(rec.matchScore || 0)
                  return (
                    <motion.div
                      key={rec.jobId}
                      initial={{ opacity: 0, y: 8 }}
                      animate={{ opacity: 1, y: 0 }}
                      className="cl-card p-5 flex flex-col justify-between hover:border-[#0E5C34] transition-all group"
                    >
                      <div className="space-y-2 mb-4">
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex items-center gap-1.5">
                            <span className="px-2 py-0.5 rounded-full bg-[#0E5C34] text-white text-[11px] font-bold font-mono">
                              #{index + 1}
                            </span>
                            <span className="cl-badge cl-badge-green font-bold font-mono">
                              <Sparkles className="w-3 h-3 text-[#059669]" /> {score}% Match
                            </span>
                          </div>
                          <span className="cl-badge cl-badge-slate text-[10px]">{rec.workMode || 'Full-time'}</span>
                        </div>
                        <h3 className="text-base font-bold text-[#1B211D] group-hover:text-[#0E5C34] transition-colors line-clamp-1">
                          {rec.title}
                        </h3>
                        <p className="text-xs font-medium text-[#475569] flex items-center gap-1">
                          <Building2 className="w-3.5 h-3.5 text-[#94a3b8]" /> {rec.companyName}
                        </p>
                        <div className="flex items-center gap-3 text-xs text-[#64748b]">
                          <span><MapPin className="w-3 h-3 inline mr-1" />{rec.location}</span>
                          {rec.salaryRange && (
                            <span className="text-[#059669] font-medium"><IndianRupee className="w-3 h-3 inline" />{rec.salaryRange.replace(/^[\$₹]\s*/, '')}</span>
                          )}
                        </div>
                        {rec.matchedSkills?.length > 0 && (
                          <div className="flex flex-wrap gap-1 pt-1">
                            {rec.matchedSkills.slice(0, 3).map((s, idx) => (
                              <span key={idx} className="px-1.5 py-0.5 bg-[#ecfdf5] text-[#065f46] text-[10px] rounded font-medium border border-[#a7f3d0]">
                                ✓ {s}
                              </span>
                            ))}
                          </div>
                        )}
                      </div>

                      <div className="flex items-center gap-2 pt-3 border-t border-[#e4e8ef]">
                        <button
                          onClick={() => setSelectedJob({ id: rec.jobId, title: rec.title, companyName: rec.companyName, location: rec.location, workMode: rec.workMode, jobType: rec.jobType, description: rec.explanation })}
                          className="cl-btn-secondary text-xs flex-1 py-1.5"
                        >
                          Details
                        </button>
                        <button
                          onClick={() => handleApply(rec.jobId)}
                          disabled={applying}
                          className="cl-btn-primary text-xs flex-1 py-1.5"
                        >
                          Apply Now
                        </button>
                      </div>
                    </motion.div>
                  )
                })}
              </div>
            ) : (
              <div className="cl-card p-6 text-center text-[#64748b]">
                <Sparkles className="w-6 h-6 text-[#94a3b8] mx-auto mb-2" />
                <p className="text-sm font-semibold text-[#334155]">No jobs matching your resume roles right now.</p>
                <p className="text-xs text-[#64748b] mt-1">Try again when new jobs are posted on CareerLens.</p>
              </div>
            )}
          </div>
        )}

        {/* Search & filter bar */}
        <form
          onSubmit={handleSearchSubmit}
          className="cl-card p-4 sm:p-5 mb-6"
          aria-label="Job search filters"
        >
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 mb-3">
            <div className="cl-input-group">
              <label htmlFor="job-search-keyword" className="sr-only">Search by keyword</label>
              <div className="cl-input-icon-left">
                <Search className="w-4 h-4 text-[#94a3b8]" />
              </div>
              <input
                id="job-search-keyword"
                type="text"
                placeholder="Title or keyword…"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                className="cl-input cl-input-with-icon text-sm"
              />
            </div>

            <div className="cl-input-group">
              <label htmlFor="job-search-location" className="sr-only">Search by location</label>
              <div className="cl-input-icon-left">
                <MapPin className="w-4 h-4 text-[#94a3b8]" />
              </div>
              <input
                id="job-search-location"
                type="text"
                placeholder="Location or remote…"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                className="cl-input cl-input-with-icon text-sm"
              />
            </div>

            <div>
              <label htmlFor="job-work-mode" className="sr-only">Work mode</label>
              <select
                id="job-work-mode"
                value={workMode}
                onChange={(e) => setWorkMode(e.target.value)}
                className="cl-input text-sm"
              >
                <option value="">All work modes</option>
                <option value="REMOTE">Remote</option>
                <option value="HYBRID">Hybrid</option>
                <option value="ONSITE">On-site</option>
              </select>
            </div>

            <div>
              <label htmlFor="job-type" className="sr-only">Job type</label>
              <select
                id="job-type"
                value={jobType}
                onChange={(e) => setJobType(e.target.value)}
                className="cl-input text-sm"
              >
                <option value="">All job types</option>
                <option value="FULL_TIME">Full-time</option>
                <option value="PART_TIME">Part-time</option>
                <option value="INTERNSHIP">Internship</option>
                <option value="CONTRACT">Contract</option>
              </select>
            </div>
          </div>

          <div className="flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={handleReset}
              className="cl-btn-ghost text-sm"
            >
              Clear filters
            </button>
            <button type="submit" className="cl-btn-primary text-sm">
              <Filter className="w-3.5 h-3.5" />
              Search
            </button>
          </div>
        </form>

        {/* All Available Jobs Section */}
        <div className="mb-2">
          <div className="flex items-center gap-2 mb-4">
            <div className="w-8 h-8 rounded-lg bg-[#1a56db]/10 flex items-center justify-center">
              <Briefcase className="w-4 h-4 text-[#1a56db]" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-[#0f172a]">All Available Jobs</h2>
              <p className="text-xs text-[#64748b]">
                {!loading && `${jobs.length} active listing${jobs.length !== 1 ? 's' : ''}`}
              </p>
            </div>
          </div>

          {loading ? (
            <div className="space-y-3">
              {[1,2,3,4].map(i => (
                <div key={i} className="cl-card p-5">
                  <div className="cl-skeleton h-5 w-64 rounded mb-3" />
                  <div className="cl-skeleton h-4 w-96 rounded mb-2" />
                  <div className="flex gap-2 mt-3">
                    {[1,2,3].map(j => <div key={j} className="cl-skeleton h-6 w-16 rounded" />)}
                  </div>
                </div>
              ))}
            </div>
          ) : jobs.length > 0 ? (
            <div className="space-y-3">
              {jobs.map((job) => {
                const isSaved = savedJobIds.has(job.id)
                return (
                  <motion.div
                    key={job.id}
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ duration: 0.2 }}
                    className="cl-card p-5 flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
                  >
                    <div className="space-y-2.5 flex-1 min-w-0">
                      <div className="flex items-start gap-3 flex-wrap">
                        <button
                          onClick={() => setSelectedJob(job)}
                          className="text-left"
                        >
                          <h3 className="text-base font-semibold text-[#0f172a] hover:text-[#1a56db] transition-colors">
                            {job.title}
                          </h3>
                        </button>
                        <span className="cl-badge cl-badge-blue shrink-0">{job.workMode}</span>
                      </div>

                      <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-[#64748b]">
                        <span className="flex items-center gap-1.5 font-medium text-[#334155]">
                          <Building2 className="w-3.5 h-3.5 text-[#94a3b8]" />
                          {job.companyName}
                        </span>
                        {job.location && (
                          <span className="flex items-center gap-1">
                            <MapPin className="w-3.5 h-3.5" />
                            {job.location}
                          </span>
                        )}
                        {job.salaryMin && (
                          <span className="flex items-center gap-1 text-[#059669] font-medium">
                            <IndianRupee className="w-3.5 h-3.5" />
                            {job.salaryMin.toLocaleString('en-IN')}–{job.salaryMax?.toLocaleString('en-IN')}
                          </span>
                        )}
                        {job.jobType && (
                          <span className="flex items-center gap-1">
                            <Briefcase className="w-3.5 h-3.5" />
                            {job.jobType.replace('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase())}
                          </span>
                        )}
                      </div>

                      {job.requiredSkills?.length > 0 && (
                        <div className="flex flex-wrap gap-1.5">
                          {job.requiredSkills.slice(0, 5).map((skill, i) => (
                            <span key={i} className="cl-skill-tag">{skill}</span>
                          ))}
                          {job.requiredSkills.length > 5 && (
                            <span className="text-xs text-[#94a3b8] self-center">
                              +{job.requiredSkills.length - 5} more
                            </span>
                          )}
                        </div>
                      )}
                    </div>

                    {/* Actions */}
                    <div className="flex items-center gap-2 shrink-0">
                      {user?.role === 'JOB_SEEKER' && (
                        <button
                          onClick={() => handleToggleSave(job.id)}
                          className={[
                            'p-2 rounded-lg border transition-colors',
                            isSaved
                              ? 'bg-[#fffbeb] text-[#d97706] border-[#fde68a]'
                              : 'bg-white text-[#334155] border-[#D9E1DB] hover:bg-[#F1F4F2] hover:border-[#11643B]',
                          ].join(' ')}
                          title={isSaved ? 'Remove from saved' : 'Save job'}
                          aria-label={isSaved ? 'Remove from saved jobs' : 'Save job'}
                        >
                          <Bookmark className={`w-4 h-4 ${isSaved ? 'fill-current' : ''}`} />
                        </button>
                      )}

                      <button
                        onClick={() => setSelectedJob(job)}
                        className="cl-btn-secondary text-sm py-1.5 px-3"
                      >
                        View details
                      </button>

                      {user?.role === 'JOB_SEEKER' && (
                        <button
                          onClick={() => handleApply(job.id)}
                          disabled={applying}
                          className="cl-btn-primary text-sm py-1.5 px-4"
                        >
                          {applying ? 'Applying…' : 'Apply'}
                        </button>
                      )}
                    </div>
                  </motion.div>
                )
              })}
            </div>
          ) : (
            <div className="cl-empty">
              <Briefcase className="cl-empty-icon" />
              <p className="cl-empty-title">No jobs found</p>
              <p className="cl-empty-text">
                Try adjusting your search terms or removing some filters to see more results.
              </p>
            </div>
          )}
        </div>
        </div>
      </main>

      {/* Job detail modal */}
      {selectedJob && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4 overflow-y-auto bg-black/40"
          role="dialog"
          aria-modal="true"
          aria-labelledby="job-detail-title"
        >
          <div
            className="cl-modal-body bg-white border border-[#D9E1DB] rounded-lg max-w-2xl w-full shadow-xl max-h-[90vh] overflow-y-auto relative"
            style={{ margin: 'auto' }}
          >
            {/* Modal header */}
            <div className="flex items-start justify-between p-6 pb-4 border-b border-[#D9E1DB] sticky top-0 bg-white z-10">
              <div>
                <span className="cl-badge cl-badge-blue mb-2">{selectedJob.companyName}</span>
                <h2 id="job-detail-title" className="text-xl font-bold text-[#0f172a] mt-1">
                  {selectedJob.title}
                </h2>
                <div className="flex flex-wrap items-center gap-3 text-sm text-[#64748b] mt-1.5">
                  {selectedJob.location && (
                    <span className="flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5" /> {selectedJob.location}
                    </span>
                  )}
                  <span className="flex items-center gap-1">
                    <Briefcase className="w-3.5 h-3.5" />
                    {selectedJob.workMode} · {selectedJob.jobType}
                  </span>
                  {selectedJob.salaryMin && (
                    <span className="text-[#059669] font-medium">
                      ₹{selectedJob.salaryMin?.toLocaleString('en-IN')} – ₹{selectedJob.salaryMax?.toLocaleString('en-IN')}
                    </span>
                  )}
                </div>
              </div>
              <button
                onClick={() => setSelectedJob(null)}
                className="p-2 text-[#94a3b8] hover:text-[#334155] hover:bg-[#f2f4f8] rounded-lg transition-colors ml-4 shrink-0"
                aria-label="Close job details"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="p-6 space-y-6">
              {/* AI match section — only for job seekers with data */}
              {user?.role === 'JOB_SEEKER' && (
                <div className="bg-[#0E5C34]/5 border border-[#0E5C34]/20 rounded-xl p-4">
                  <div className="flex items-center gap-2 mb-3">
                    <BarChart2 className="w-4 h-4 text-[#0E5C34]" />
                    <span className="text-sm font-semibold text-[#0E5C34]">Your resume match</span>
                  </div>

                  {loadingMatch ? (
                    <p className="text-xs text-[#64748b] animate-pulse">Analyzing your resume against this role…</p>
                  ) : jobMatch ? (
                    <div className="space-y-3">
                      <div className="flex items-center gap-3">
                        <span className="text-3xl font-bold text-[#0E5C34] font-mono">
                          {Math.round(jobMatch.overallScore)}%
                        </span>
                        <div>
                          <p className="text-xs font-medium text-[#334155]">Overall match</p>
                          {jobMatch.explanation && (
                            <p className="text-xs text-[#64748b] mt-0.5">{jobMatch.explanation}</p>
                          )}
                        </div>
                      </div>

                      <div className="grid grid-cols-5 gap-2 text-center">
                        {[
                          { label: 'Skills', val: jobMatch.skillScore },
                          { label: 'Semantic', val: jobMatch.semanticScore },
                          { label: 'Experience', val: jobMatch.experienceScore },
                          { label: 'Education', val: jobMatch.educationScore },
                          { label: 'Requirements', val: jobMatch.requirementScore },
                        ].map(({ label, val }) => (
                          <div key={label} className="cl-card-nested p-2">
                            <p className="text-[10px] text-[#64748b] font-medium">{label}</p>
                            <p className="text-sm font-semibold text-[#0f172a]">{Math.round(val || 0)}%</p>
                          </div>
                        ))}
                      </div>

                      <div className="flex flex-wrap gap-2">
                        {jobMatch.matchedSkills?.length > 0 && (
                          <div>
                            <p className="text-xs font-medium text-[#059669] mb-1">Matched skills</p>
                            <div className="flex flex-wrap gap-1">
                              {jobMatch.matchedSkills.map((s, i) => (
                                <span key={i} className="px-2 py-0.5 bg-[#ecfdf5] text-[#065f46] border border-[#a7f3d0] rounded text-xs">
                                  {s}
                                </span>
                              ))}
                            </div>
                          </div>
                        )}
                        {jobMatch.missingRequiredSkills?.length > 0 && (
                          <div>
                            <p className="text-xs font-medium text-[#dc2626] mb-1">Missing required skills</p>
                            <div className="flex flex-wrap gap-1">
                              {jobMatch.missingRequiredSkills.map((s, i) => (
                                <span key={i} className="px-2 py-0.5 bg-[#fef2f2] text-[#991b1b] border border-[#fecaca] rounded text-xs">
                                  {s}
                                </span>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  ) : (
                    <p className="text-xs text-[#64748b]">
                      Upload a resume to see your compatibility score for this role.
                    </p>
                  )}
                </div>
              )}

              {/* Job description */}
              <div>
                <h3 className="text-sm font-semibold text-[#0f172a] mb-2">Description</h3>
                <p className="text-sm text-[#475569] leading-relaxed whitespace-pre-wrap">
                  {selectedJob.description}
                </p>
              </div>

              {selectedJob.responsibilities?.length > 0 && (
                <div>
                  <h3 className="text-sm font-semibold text-[#0f172a] mb-2">Responsibilities</h3>
                  <ul className="space-y-1.5">
                    {selectedJob.responsibilities.map((r, i) => (
                      <li key={i} className="text-sm text-[#475569] flex items-start gap-2">
                        <span className="text-[#1a56db] mt-1.5">·</span>
                        {r}
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {selectedJob.requirements?.length > 0 && (
                <div>
                  <h3 className="text-sm font-semibold text-[#0f172a] mb-2">Requirements</h3>
                  <ul className="space-y-1.5">
                    {selectedJob.requirements.map((r, i) => (
                      <li key={i} className="text-sm text-[#475569] flex items-start gap-2">
                        <span className="text-[#1a56db] mt-1.5">·</span>
                        {r}
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {selectedJob.requiredSkills?.length > 0 && (
                <div>
                  <h3 className="text-sm font-semibold text-[#0f172a] mb-2">Required skills</h3>
                  <div className="flex flex-wrap gap-1.5">
                    {selectedJob.requiredSkills.map((s, i) => (
                      <span key={i} className="cl-badge cl-badge-blue">{s}</span>
                    ))}
                  </div>
                </div>
              )}

              {/* Footer actions */}
              <div className="flex items-center justify-end gap-3 pt-4 border-t border-[#e4e8ef]">
                <button
                  onClick={() => setSelectedJob(null)}
                  className="cl-btn-secondary text-sm"
                >
                  Close
                </button>
                {user?.role === 'JOB_SEEKER' && (
                  <button
                    onClick={() => handleApply(selectedJob.id)}
                    disabled={applying}
                    className="cl-btn-primary text-sm"
                  >
                    {applying ? 'Applying…' : 'Apply now'}
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      <Footer />
    </div>
  )
}
