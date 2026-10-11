import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { 
  Sparkles, Briefcase, MapPin, IndianRupee, CheckCircle2, 
  XCircle, AlertCircle, ChevronRight, BarChart2, ShieldCheck, ArrowRight, Bookmark, Lightbulb
} from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { jobApi, aiApi, jobSeekerApi } from '../services/api'
import { Link } from 'react-router-dom'

export default function JobSeekerMatchesPage() {
  const [recommendations, setRecommendations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selectedMatch, setSelectedMatch] = useState(null)
  const [applyingJobId, setApplyingJobId] = useState(null)
  const [applySuccess, setApplySuccess] = useState('')
  const [savedJobIds, setSavedJobIds] = useState(new Set())

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    setLoading(true)
    setError('')
    try {
      const [recsRes, savedRes] = await Promise.all([
        aiApi.getRecommendations(0, 20),
        jobSeekerApi.getSavedJobs()
      ])

      const recList = recsRes.content || recsRes || []
      setRecommendations(recList)
      
      const ids = new Set((savedRes || []).map(s => s.jobId))
      setSavedJobIds(ids)

      if (recList.length > 0) {
        const first = recList[0]
        setSelectedMatch({
          job: {
            id: first.jobId,
            title: first.title,
            companyName: first.companyName,
            location: first.location,
            workMode: first.workMode,
            salaryRange: first.salaryRange,
            jobType: first.jobType
          },
          match: {
            overallScore: first.matchScore,
            matchedSkills: first.matchedSkills,
            missingRequiredSkills: first.missingRequiredSkills,
            missingPreferredSkills: first.missingPreferredSkills,
            explanation: first.explanation,
            analysisMethod: first.analysisMethod,
            skillScore: first.matchScore * 0.9,
            semanticScore: first.matchScore * 0.95,
            experienceScore: first.matchScore * 0.85,
            educationScore: 90,
            requirementScore: 85
          }
        })
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load recommendations. Please make sure you have uploaded a resume.')
    } finally {
      setLoading(false)
    }
  }

  const handleApply = async (jobId) => {
    setApplyingJobId(jobId)
    setApplySuccess('')
    setError('')
    try {
      await jobApi.apply(jobId)
      setApplySuccess('Successfully applied to job!')
      setTimeout(() => setApplySuccess(''), 4000)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to apply for job.')
    } finally {
      setApplyingJobId(null)
    }
  }

  const handleToggleSave = async (jobId, e) => {
    e.stopPropagation()
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
      setError('Failed to update saved job status.')
    }
  }

  return (
    <div className="cl-page-jobseeker">
      <Navbar />

      <main className="cl-main">
        <div className="cl-panel p-5 sm:p-6 space-y-6">
          {/* Header */}
        <div className="cl-page-header flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <div className="flex items-center gap-2 text-xs font-semibold text-[#0E5C34] mb-1">
              <Sparkles className="w-4 h-4 text-[#0E5C34]" /> Personalized AI Job Recommendations
            </div>
            <h1 className="cl-page-title">Recommended For You</h1>
            <p className="cl-page-subtitle">
              Job recommendations ranked by actual resume match scores, skills overlap, and experience.
            </p>
          </div>
        </div>

        {/* Notifications */}
        {error && (
          <div className="cl-alert cl-alert-error flex items-center justify-between">
            <div className="flex items-center gap-3">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
            <Link to="/applicant/resume" className="text-xs font-semibold underline text-[#991b1b]">
              Upload / Check Resume
            </Link>
          </div>
        )}

        {applySuccess && (
          <div className="cl-alert cl-alert-success flex items-center gap-3">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>{applySuccess}</span>
          </div>
        )}

        {loading ? (
          <div className="py-20 flex flex-col justify-center items-center gap-3">
            <div className="cl-spinner" />
            <p className="text-xs text-[#64748b]">Evaluating personalized job recommendations...</p>
          </div>
        ) : recommendations.length === 0 ? (
          <div className="cl-empty">
            <Briefcase className="cl-empty-icon" />
            <h3 className="cl-empty-title">No AI Job Recommendations Found</h3>
            <p className="cl-empty-text">
              Make sure your resume is uploaded with valid technical skills and work experience to unlock custom job recommendations.
            </p>
            <Link 
              to="/applicant/resume" 
              className="mt-4 cl-btn-primary text-sm"
            >
              Update Resume <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            
            {/* Recommendations List */}
            <div className="lg:col-span-2 space-y-4">
              {recommendations.map((rec) => {
                const score = Math.round(rec.matchScore || 0)
                const isSelected = selectedMatch?.job?.id === rec.jobId
                const isSaved = savedJobIds.has(rec.jobId)

                return (
                  <div 
                    key={rec.jobId}
                    onClick={() => setSelectedMatch({
                      job: {
                        id: rec.jobId,
                        title: rec.title,
                        companyName: rec.companyName,
                        location: rec.location,
                        workMode: rec.workMode,
                        salaryRange: rec.salaryRange,
                        jobType: rec.jobType
                      },
                      match: {
                        overallScore: rec.matchScore,
                        matchedSkills: rec.matchedSkills,
                        missingRequiredSkills: rec.missingRequiredSkills,
                        missingPreferredSkills: rec.missingPreferredSkills,
                        explanation: rec.explanation,
                        analysisMethod: rec.analysisMethod,
                        skillScore: rec.matchScore * 0.9,
                        semanticScore: rec.matchScore * 0.95,
                        experienceScore: rec.matchScore * 0.85,
                        educationScore: 90,
                        requirementScore: 85
                      }
                    })}
                    className={`cl-card p-5 transition-all cursor-pointer space-y-3 ${
                      isSelected 
                        ? 'border-[#0E5C34] ring-2 ring-[#0E5C34]/20 bg-[#0E5C34]/5' 
                        : ''
                    }`}
                  >
                    <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                      <div>
                        <div className="flex items-center gap-2">
                          <h3 className="text-base font-bold text-[#0f172a] hover:text-[#0E5C34] transition-colors">
                            {rec.title}
                          </h3>
                          <span className="text-xs text-[#64748b]">at {rec.companyName}</span>
                        </div>
                        <div className="flex flex-wrap items-center gap-3 text-xs text-[#64748b] mt-1.5">
                          <span className="flex items-center gap-1"><MapPin className="w-3.5 h-3.5" />{rec.location}</span>
                          <span>•</span>
                          <span className="flex items-center gap-1"><Briefcase className="w-3.5 h-3.5" />{rec.jobType || 'Full-time'}</span>
                          {rec.salaryRange && (
                            <>
                              <span>•</span>
                              <span className="flex items-center gap-1 text-[#059669] font-medium"><IndianRupee className="w-3.5 h-3.5" />{rec.salaryRange.replace(/^[\$₹]\s*/, '')}</span>
                            </>
                          )}
                        </div>
                      </div>

                      {/* Overall Score Badge & Save */}
                      <div className="flex items-center gap-3 self-end sm:self-center">
                        <button
                          onClick={(e) => handleToggleSave(rec.jobId, e)}
                          className={`p-2 rounded border transition-colors ${
                            isSaved 
                              ? 'bg-[#fffbeb] text-[#d97706] border-[#fde68a]' 
                              : 'bg-white text-[#202824] border-[#D9E1DB] hover:bg-[#F1F4F2] hover:border-[#11643B]'
                          }`}
                          title={isSaved ? 'Unsave Job' : 'Save Job'}
                        >
                          <Bookmark className={`w-4 h-4 ${isSaved ? 'fill-current' : ''}`} />
                        </button>

                        <div className="px-3.5 py-1.5 rounded border flex flex-col items-center bg-[#0E5C34]/10 text-[#0E5C34] border-[#0E5C34]/20 font-mono">
                          <span className="text-lg font-bold">{score}%</span>
                          <span className="text-[9px] uppercase font-bold tracking-wider opacity-80">Match</span>
                        </div>
                      </div>
                    </div>

                    {/* Explanation */}
                    {rec.explanation && (
                      <div className="cl-card-nested p-3 text-xs text-[#0E5C34] flex items-start gap-2">
                        <Lightbulb className="w-4 h-4 text-[#0E5C34] shrink-0 mt-0.5" />
                        <div>
                          <span className="font-bold block text-[11px] text-[#0E5C34]">Why this job?</span>
                          <p className="text-[11px] leading-relaxed text-[#1B211D]">{rec.explanation}</p>
                        </div>
                      </div>
                    )}

                    {/* Skill Tags */}
                    <div className="pt-2 border-t border-[#e4e8ef] flex flex-wrap items-center justify-between gap-3">
                      <div className="flex flex-wrap gap-1.5 items-center">
                        <span className="text-[11px] text-[#64748b] mr-1">Top Skills:</span>
                        {rec.matchedSkills?.slice(0, 4).map((s, idx) => (
                          <span key={idx} className="px-2 py-0.5 bg-[#ecfdf5] border border-[#a7f3d0] text-[#065f46] text-[11px] rounded font-medium">
                            ✓ {s}
                          </span>
                        ))}
                        {rec.missingRequiredSkills?.length > 0 && (
                          <span className="px-2 py-0.5 bg-[#fef2f2] border border-[#fecaca] text-[#991b1b] text-[11px] rounded font-medium">
                            Missing: {rec.missingRequiredSkills[0]}
                          </span>
                        )}
                      </div>

                      <span className="text-xs font-semibold text-[#0E5C34] flex items-center gap-1">
                        Details <ChevronRight className="w-4 h-4" />
                      </span>
                    </div>
                  </div>
                )
              })}
            </div>

            {/* Selected Match Detailed Breakdown */}
            <div className="lg:col-span-1">
              {selectedMatch ? (
                <div className="sticky top-24 cl-card p-6 space-y-6">
                  
                  {/* Top Match Header */}
                  <div>
                    <div className="flex justify-between items-start gap-2">
                      <div>
                        <h3 className="text-lg font-bold text-[#0f172a]">{selectedMatch.job.title}</h3>
                        <p className="text-xs text-[#64748b] mt-0.5">{selectedMatch.job.companyName}</p>
                      </div>
                      <span className="cl-badge cl-badge-blue shrink-0">
                        <ShieldCheck className="w-3 h-3" /> {selectedMatch.match.analysisMethod || 'HYBRID_AI'}
                      </span>
                    </div>

                    <div className="mt-4 p-4 rounded-xl bg-[#f8f9fb] border border-[#e4e8ef] flex items-center justify-between">
                      <div>
                        <span className="text-xs text-[#64748b] font-medium">Match Score</span>
                        <p className="text-[11px] text-[#94a3b8] mt-0.5 italic max-w-[160px]">
                          Evaluated against resume factors
                        </p>
                      </div>
                      <div className="text-3xl font-bold text-[#1a56db]">
                        {Math.round(selectedMatch.match.overallScore)}%
                      </div>
                    </div>
                  </div>

                  {/* AI Explanation */}
                  {selectedMatch.match.explanation && (
                    <div className="cl-card-nested p-3.5 text-[#0E5C34] text-xs leading-relaxed">
                      <span className="font-bold block mb-1">AI Recommendation Reason:</span>
                      {selectedMatch.match.explanation}
                    </div>
                  )}

                  {/* Skill Gap Analysis */}
                  <div className="space-y-3">
                    <h4 className="text-xs font-bold text-[#64748b] uppercase tracking-wider">Skill Alignment</h4>
                    
                    {/* Matched */}
                    <div>
                      <span className="text-[11px] font-semibold text-[#059669] flex items-center gap-1 mb-1.5">
                        <CheckCircle2 className="w-3.5 h-3.5" /> Matched Skills
                      </span>
                      <div className="flex flex-wrap gap-1">
                        {selectedMatch.match.matchedSkills?.length > 0 ? (
                          selectedMatch.match.matchedSkills.map((s, idx) => (
                            <span key={idx} className="px-2 py-0.5 bg-[#ecfdf5] text-[#065f46] border border-[#a7f3d0] text-[11px] rounded font-medium">
                              {s}
                            </span>
                          ))
                        ) : (
                          <span className="text-[11px] text-[#94a3b8] italic">None matched</span>
                        )}
                      </div>
                    </div>

                    {/* Missing Required */}
                    <div>
                      <span className="text-[11px] font-semibold text-[#dc2626] flex items-center gap-1 mb-1.5">
                        <XCircle className="w-3.5 h-3.5" /> Missing Required Skills
                      </span>
                      <div className="flex flex-wrap gap-1">
                        {selectedMatch.match.missingRequiredSkills?.length > 0 ? (
                          selectedMatch.match.missingRequiredSkills.map((s, idx) => (
                            <span key={idx} className="px-2 py-0.5 bg-[#fef2f2] text-[#991b1b] border border-[#fecaca] text-[11px] rounded font-medium">
                              {s}
                            </span>
                          ))
                        ) : (
                          <span className="text-[11px] text-[#64748b]">All required skills present!</span>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Apply Button */}
                  <button
                    onClick={() => handleApply(selectedMatch.job.id)}
                    disabled={applyingJobId === selectedMatch.job.id}
                    className="w-full cl-btn-primary py-2.5 text-sm flex items-center justify-center gap-2"
                  >
                    {applyingJobId === selectedMatch.job.id ? (
                      <span>Applying...</span>
                    ) : (
                      <>
                        <span>Apply For Position</span>
                        <ArrowRight className="w-4 h-4" />
                      </>
                    )}
                  </button>

                </div>
              ) : (
                <div className="cl-card p-8 text-center text-[#64748b] text-xs">
                  Click on any recommendation card to inspect match details.
                </div>
              )}
            </div>

          </div>
        )}
        </div>
      </main>

      <Footer />
    </div>
  )
}
