import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import {
  FileText, UploadCloud, Download, Trash2, CheckCircle2,
  AlertCircle, BarChart3, Star, Plus, X, ShieldCheck, Briefcase
} from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { resumeApi, aiApi } from '../services/api'

export default function ResumePage() {
  const [resumes, setResumes] = useState([])
  const [activeResumeId, setActiveResumeId] = useState(null)
  const [analysis, setAnalysis] = useState(null)
  const [loading, setLoading] = useState(true)
  const [analyzing, setAnalyzing] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [deletingId, setDeletingId] = useState(null)
  const [settingDefaultId, setSettingDefaultId] = useState(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const getCandidateRole = (res) => {
    if (!res) return 'Software Engineer'
    const textStr = ((res.title || '') + ' ' + (res.originalFileName || '') + ' ' + (res.summary || '') + ' ' + (res.extractedText || '')).toLowerCase().replace(/[-_]/g, ' ')

    // 1. Explicit Full-Stack headline / text match
    if (textStr.includes('full stack') || textStr.includes('fullstack') || textStr.includes('mern stack') || textStr.includes('mean stack')) {
      return 'Full Stack Developer'
    }

    // 2. Explicit DevOps / Data role match
    if (textStr.includes('devops') || textStr.includes('devsecops') || textStr.includes('cloud engineer')) {
      return 'DevOps Engineer'
    }
    if (textStr.includes('data analyst') || textStr.includes('business analyst')) return 'Data Analyst'
    if (textStr.includes('data scientist') || textStr.includes('machine learning')) return 'Data Scientist'
    if (textStr.includes('data engineer')) return 'Data Engineer'

    // 3. Dual Skill Competency check: Both Frontend & Backend skills present -> Full Stack Developer!
    const skillList = (res.skills || []).map(s => String(s).toLowerCase())
    const hasFront = skillList.some(s => s.includes('react') || s.includes('html') || s.includes('css') || s.includes('angular') || s.includes('vue') || s.includes('javascript') || s.includes('bootstrap'))
    const hasBack = skillList.some(s => s.includes('node') || s.includes('express') || s.includes('java') || s.includes('spring') || s.includes('python') || s.includes('mongo') || s.includes('sql') || s.includes('api'))

    if (hasFront && hasBack) {
      return 'Full Stack Developer'
    }

    if (res.targetRole && res.targetRole !== 'Software Professional' && res.targetRole !== 'Software Engineer') {
      return res.targetRole
    }

    if (hasFront) return 'Frontend Developer'
    if (hasBack) return 'Backend Developer'

    return 'Software Engineer'
  }

  // Upload modal
  const [showUploadModal, setShowUploadModal] = useState(false)
  const [titleInput, setTitleInput] = useState('')
  const [selectedFile, setSelectedFile] = useState(null)

  useEffect(() => { fetchResumes() }, [])

  const fetchResumes = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await resumeApi.listVersions()
      setResumes(data || [])
      if (data?.length > 0) {
        const def = data.find(r => r.isDefault) || data[0]
        setActiveResumeId(def.id)
        fetchAnalysis()
      } else {
        setAnalysis(null)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load resume versions.')
    } finally {
      setLoading(false)
    }
  }

  const fetchAnalysis = async () => {
    setAnalyzing(true)
    try {
      const data = await aiApi.getResumeAnalysis()
      setAnalysis(data)
    } catch (_) {
      setAnalysis(null)
    } finally {
      setAnalyzing(false)
    }
  }

  const handleSelectResume = (r) => {
    setActiveResumeId(r.id)
    fetchAnalysis()
  }

  const handleUploadSubmit = async (e) => {
    e.preventDefault()
    if (!selectedFile) {
      setError('Please select a PDF or DOCX file.')
      return
    }
    setError('')
    setSuccess('')
    setUploading(true)
    const formData = new FormData()
    formData.append('file', selectedFile)
    if (titleInput.trim()) formData.append('title', titleInput.trim())
    try {
      await resumeApi.uploadVersion(formData)
      setSuccess('Resume uploaded successfully.')
      setShowUploadModal(false)
      setTitleInput('')
      setSelectedFile(null)
      fetchResumes()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload resume.')
    } finally {
      setUploading(false)
    }
  }

  const handleSetDefault = async (id) => {
    setSettingDefaultId(id)
    setError('')
    try {
      await resumeApi.setDefault(id)
      setSuccess('Default resume updated.')
      fetchResumes()
    } catch (_) {
      setError('Failed to update default resume.')
    } finally {
      setSettingDefaultId(null)
    }
  }

  const handleDownload = async (id, fileName) => {
    try {
      const res = await resumeApi.downloadVersion(id)
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', fileName || 'resume.pdf')
      document.body.appendChild(link)
      link.click()
      link.remove()
    } catch (_) {
      setError('Failed to download resume.')
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this resume version? This cannot be undone.')) return
    setDeletingId(id)
    setError('')
    try {
      await resumeApi.deleteVersion(id)
      setSuccess('Resume version deleted.')
      fetchResumes()
    } catch (_) {
      setError('Failed to delete resume version.')
    } finally {
      setDeletingId(null)
    }
  }

  const activeResume = resumes.find(r => r.id === activeResumeId) || resumes[0]

  const scoreLabel = (score) => {
    if (score >= 80) return 'Strong'
    if (score >= 60) return 'Good'
    return 'Needs work'
  }

  const scoreColor = (score) => {
    if (score >= 80) return 'text-[#059669]'
    if (score >= 60) return 'text-[#d97706]'
    return 'text-[#dc2626]'
  }

  return (
    <div className="cl-page-jobseeker">
      <Navbar />

      <main className="cl-main">
        <div className="cl-panel p-5 sm:p-6 space-y-6">
          {/* Header */}
        <div className="cl-page-header flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="cl-page-title">Resumes</h1>
            <p className="cl-page-subtitle">
              Manage multiple resume versions and see how they score.
            </p>
          </div>
          <button
            onClick={() => setShowUploadModal(true)}
            className="cl-btn-primary text-sm shrink-0"
          >
            <Plus className="w-4 h-4" />
            Add resume
          </button>
        </div>

        {/* Alerts */}
        {error && (
          <div className="cl-alert cl-alert-error mb-5" role="alert">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <span className="text-sm">{error}</span>
          </div>
        )}
        {success && (
          <div className="cl-alert cl-alert-success mb-5" role="status">
            <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
            <span className="text-sm">{success}</span>
          </div>
        )}

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
            {[1,2].map(i => (
              <div key={i} className="cl-card p-5">
                <div className="cl-skeleton h-5 w-40 rounded mb-3" />
                <div className="cl-skeleton h-4 w-56 rounded mb-2" />
                <div className="cl-skeleton h-3 w-32 rounded" />
              </div>
            ))}
          </div>
        ) : resumes.length > 0 ? (
          <div className="space-y-8">
            {/* Resume version cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {resumes.map((res) => {
                const isActive = res.id === activeResumeId
                return (
                  <div
                    key={res.id}
                    onClick={() => handleSelectResume(res)}
                    className={[
                      'cl-card cursor-pointer transition-all flex flex-col justify-between',
                      isActive
                        ? '!bg-white !border-[#11643B] shadow-sm ring-2 ring-[#11643B]/20'
                        : 'hover:border-[#11643B]/40',
                    ].join(' ')}
                  >
                    <div className="p-5">
                      <div className="flex items-start justify-between gap-2 mb-3">
                        <div className="flex items-center gap-2 min-w-0">
                          <FileText className={`w-4 h-4 shrink-0 ${isActive ? 'text-[#0E5C34]' : 'text-[#94a3b8]'}`} />
                          <h3 className="text-sm font-semibold text-[#0f172a] truncate">
                            {res.title || res.originalFileName}
                          </h3>
                        </div>
                        {res.isDefault && (
                          <span className="cl-badge cl-badge-amber shrink-0">
                            <Star className="w-3 h-3 fill-current" />
                            Default
                          </span>
                        )}
                      </div>

                      <p className="text-xs text-[#94a3b8] truncate">{res.originalFileName}</p>
                      <p className="text-xs text-[#94a3b8] mt-0.5 font-mono">
                        {new Date(res.uploadedAt).toLocaleDateString()} ·{' '}
                        {(res.fileSize / 1024 / 1024).toFixed(2)} MB
                      </p>

                      {res.skills?.length > 0 && (
                        <div className="flex flex-wrap gap-1 mt-3">
                          {res.skills.slice(0, 4).map((skill, i) => (
                            <span key={i} className="cl-skill-tag">{skill}</span>
                          ))}
                          {res.skills.length > 4 && (
                            <span className="text-xs text-[#94a3b8] self-center font-mono">
                              +{res.skills.length - 4}
                            </span>
                          )}
                        </div>
                      )}

                      {/* Candidate Target Role under skills */}
                      <div className="mt-3 pt-2.5 border-t border-[#f1f4f9] flex items-center justify-between text-xs">
                        <span className="text-[#64748b] font-medium flex items-center gap-1.5">
                          <Briefcase className="w-3.5 h-3.5 text-[#0E5C34]" /> Candidate Role:
                        </span>
                        <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-[#0E5C34]/10 text-[#0E5C34] border border-[#0E5C34]/20">
                          {getCandidateRole(res)}
                        </span>
                      </div>
                    </div>

                    <div className="px-5 pb-4 pt-3 border-t border-[#f1f4f9] flex items-center gap-3">
                      <button
                        onClick={(e) => { e.stopPropagation(); handleDownload(res.id, res.originalFileName) }}
                        className="cl-btn-ghost text-xs py-1 px-2 text-[#64748b]"
                        title="Download"
                      >
                        <Download className="w-3.5 h-3.5" />
                        Download
                      </button>

                      {!res.isDefault && (
                        <button
                          onClick={(e) => { e.stopPropagation(); handleSetDefault(res.id) }}
                          disabled={settingDefaultId === res.id}
                          className="text-xs font-medium text-[#d97706] hover:text-[#b45309] flex items-center gap-1 transition-colors"
                        >
                          <Star className="w-3.5 h-3.5" />
                          Set default
                        </button>
                      )}

                      <button
                        onClick={(e) => { e.stopPropagation(); handleDelete(res.id) }}
                        disabled={deletingId === res.id}
                        className="ml-auto text-[#dc2626] hover:text-[#b91c1c] p-1 rounded transition-colors"
                        aria-label="Delete resume version"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                )
              })}

              {/* Upload button card */}
              <button
                onClick={() => setShowUploadModal(true)}
                className="border-2 border-dashed border-[#e4e8ef] rounded-xl p-5 flex flex-col items-center justify-center gap-2 text-[#94a3b8] hover:border-[#1a56db] hover:text-[#1a56db] hover:bg-[#f0f4ff] transition-all min-h-[120px]"
              >
                <UploadCloud className="w-6 h-6" />
                <span className="text-sm font-medium">Add version</span>
              </button>
            </div>

            {/* Active resume detail & analysis */}
            {activeResume && (
              <div className="space-y-5">
                <div className="flex items-center justify-between">
                  <h2 className="text-base font-semibold text-[#0f172a]">
                    {activeResume.title || activeResume.originalFileName}
                    {activeResume.isDefault && (
                      <span className="cl-badge cl-badge-amber ml-2">Default</span>
                    )}
                  </h2>
                  <button
                    onClick={() => handleDownload(activeResume.id, activeResume.originalFileName)}
                    className="cl-btn-secondary text-sm"
                  >
                    <Download className="w-4 h-4" />
                    Download
                  </button>
                </div>

                {/* ATS Analysis */}
                {analyzing ? (
                  <div className="cl-card p-8 flex flex-col items-center gap-3">
                    <div className="cl-spinner" />
                    <p className="text-sm text-[#64748b]">Calculating resume score…</p>
                  </div>
                ) : analysis ? (
                  <div className="cl-card p-6">
                    <div className="flex items-center justify-between border-b border-[#f1f4f9] pb-4 mb-5">
                      <div>
                        <div className="flex items-center gap-2 mb-0.5">
                          <BarChart3 className="w-4 h-4 text-[#0E5C34]" />
                          <span className="text-sm font-semibold text-[#0f172a]">Resume Score</span>
                        </div>
                        <p className="text-xs text-[#64748b]">
                          Based on skills, experience, education, keywords, and structure.
                        </p>
                      </div>
                      {analysis.analysisMethod && (
                        <span className="cl-badge cl-badge-green text-xs font-mono">
                          <ShieldCheck className="w-3 h-3" />
                          {analysis.analysisMethod}
                        </span>
                      )}
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-[160px_1fr] gap-6 items-start">
                      <div className="flex flex-col items-center justify-center bg-[#f8f9fb] rounded-xl p-5 text-center">
                        <p className="text-xs text-[#94a3b8] mb-1">Overall score</p>
                        <p className={`text-4xl font-bold font-mono ${scoreColor(analysis.overallScore || 0)}`}>
                          {Math.round(analysis.overallScore || 0)}
                          <span className="text-base text-[#94a3b8] font-normal">/100</span>
                        </p>
                        <span className={`text-xs font-medium mt-1 ${scoreColor(analysis.overallScore || 0)}`}>
                          {scoreLabel(analysis.overallScore || 0)}
                        </span>
                      </div>

                      <div className="space-y-3">
                        {[
                          { name: 'Skills', val: analysis.skillScore },
                          { name: 'Experience', val: analysis.experienceScore },
                          { name: 'Education', val: analysis.educationScore },
                          { name: 'Keywords', val: analysis.keywordScore },
                          { name: 'Structure', val: analysis.structureScore },
                        ].map(({ name, val }) => (
                          <div key={name} className="space-y-1">
                            <div className="flex justify-between text-xs">
                              <span className="font-medium text-[#334155]">{name}</span>
                              <span className="text-[#64748b]">{Math.round(val || 0)} / 100</span>
                            </div>
                            <div className="cl-progress-track">
                              <div
                                className="cl-progress-fill"
                                style={{ width: `${Math.round(val || 0)}%` }}
                              />
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                ) : null}

                {/* Extracted data */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  <div className="cl-card p-5">
                    <h3 className="text-sm font-semibold text-[#0f172a] mb-3">
                      Extracted skills ({activeResume.skills?.length || 0})
                    </h3>
                    {activeResume.skills?.length > 0 ? (
                      <div className="flex flex-wrap gap-1.5">
                        {activeResume.skills.map((skill, i) => (
                          <span key={i} className="cl-badge cl-badge-blue">{skill}</span>
                        ))}
                      </div>
                    ) : (
                      <p className="text-sm text-[#94a3b8]">No skills detected automatically.</p>
                    )}
                  </div>

                  <div className="cl-card p-5">
                    <h3 className="text-sm font-semibold text-[#0f172a] mb-3">Extracted education</h3>
                    {activeResume.education?.length > 0 ? (
                      <ul className="space-y-1.5">
                        {activeResume.education.map((edu, i) => (
                          <li key={i} className="text-sm text-[#475569] flex items-start gap-2">
                            <span className="text-[#1a56db] mt-1">·</span>
                            {edu}
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <p className="text-sm text-[#94a3b8]">No education detected automatically.</p>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        ) : (
          /* Empty state */
          <div className="cl-empty">
            <UploadCloud className="cl-empty-icon" />
            <p className="cl-empty-title">No resumes uploaded yet</p>
            <p className="cl-empty-text">
              Upload a tailored resume to start applying for jobs and seeing your match score.
            </p>
            <button
              onClick={() => setShowUploadModal(true)}
              className="cl-btn-primary text-sm mt-4"
            >
              <Plus className="w-4 h-4" />
              Upload your first resume
            </button>
          </div>
        )}
        </div>
      </main>

      {/* Upload modal */}
      {showUploadModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
          role="dialog"
          aria-modal="true"
          aria-labelledby="upload-modal-title"
        >
          <div className="cl-modal-body bg-white border border-[#D9E1DB] rounded-lg p-6 max-w-md w-full shadow-lg">
            <div className="flex items-center justify-between border-b border-[#e4e8ef] pb-4 mb-5">
              <h3 id="upload-modal-title" className="text-base font-semibold text-[#0f172a] flex items-center gap-2">
                <FileText className="w-4 h-4 text-[#1a56db]" />
                Upload resume version
              </h3>
              <button
                onClick={() => setShowUploadModal(false)}
                className="p-1.5 rounded-lg hover:bg-[#f2f4f8] text-[#94a3b8] hover:text-[#334155] transition-colors"
                aria-label="Close upload dialog"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {error && (
              <div className="cl-alert cl-alert-error mb-4" role="alert">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span className="text-sm">{error}</span>
              </div>
            )}

            <form onSubmit={handleUploadSubmit} className="space-y-4">
              <div>
                <label htmlFor="resume-title" className="cl-label">
                  Version name <span className="text-[#94a3b8] font-normal">(optional)</span>
                </label>
                <input
                  id="resume-title"
                  type="text"
                  value={titleInput}
                  onChange={(e) => setTitleInput(e.target.value)}
                  placeholder="e.g. Java Backend Developer"
                  className="cl-input text-sm"
                />
                <p className="text-xs text-[#94a3b8] mt-1">Leave blank to use the file name.</p>
              </div>

              <div>
                <label htmlFor="resume-file" className="cl-label">File (PDF or DOCX)</label>
                <input
                  id="resume-file"
                  type="file"
                  accept=".pdf,.docx,.doc"
                  onChange={(e) => setSelectedFile(e.target.files[0])}
                  required
                  className="block w-full text-sm text-[#64748b] file:mr-3 file:py-2 file:px-3 file:rounded-lg file:border file:border-[#e4e8ef] file:text-xs file:font-medium file:bg-[#f8f9fb] file:text-[#334155] hover:file:bg-[#f2f4f8] cursor-pointer"
                />
                {selectedFile && (
                  <p className="text-xs text-[#64748b] mt-1">
                    Selected: {selectedFile.name} ({(selectedFile.size / 1024 / 1024).toFixed(2)} MB)
                  </p>
                )}
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-[#e4e8ef]">
                <button
                  type="button"
                  onClick={() => setShowUploadModal(false)}
                  className="cl-btn-secondary text-sm"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={uploading}
                  className="cl-btn-primary text-sm"
                >
                  {uploading ? 'Uploading…' : 'Upload resume'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <Footer />
    </div>
  )
}
