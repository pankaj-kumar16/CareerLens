import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Briefcase, CheckCircle2, AlertCircle, ArrowLeft, Building2 } from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { recruiterApi } from '../services/api'

export default function JobCreateEditPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditing = Boolean(id)

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    location: '',
    workMode: 'REMOTE',
    jobType: 'FULL_TIME',
    salaryMin: '',
    salaryMax: '',
    minimumExperience: '',
    maximumExperience: '',
    education: '',
    requiredSkills: '',
    preferredSkills: '',
    responsibilities: '',
    requirements: '',
    active: true,
  })

  const [loading, setLoading] = useState(false)
  const [fetching, setFetching] = useState(isEditing)
  const [error, setError] = useState('')

  useEffect(() => {
    if (isEditing) {
      fetchJobDetails()
    }
  }, [id])

  const fetchJobDetails = async () => {
    try {
      const data = await recruiterApi.getJobById(id)
      setFormData({
        title: data.title || '',
        description: data.description || '',
        location: data.location || '',
        workMode: data.workMode || 'REMOTE',
        jobType: data.jobType || 'FULL_TIME',
        salaryMin: data.salaryMin || '',
        salaryMax: data.salaryMax || '',
        minimumExperience: data.minimumExperience || '',
        maximumExperience: data.maximumExperience || '',
        education: data.education || '',
        requiredSkills: (data.requiredSkills || []).join(', '),
        preferredSkills: (data.preferredSkills || []).join(', '),
        responsibilities: (data.responsibilities || []).join('\n'),
        requirements: (data.requirements || []).join('\n'),
        active: data.active ?? true,
      })
    } catch (err) {
      setError('Failed to fetch job details.')
    } finally {
      setFetching(false)
    }
  }

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    const payload = {
      title: formData.title,
      description: formData.description,
      location: formData.location,
      workMode: formData.workMode,
      jobType: formData.jobType,
      salaryMin: formData.salaryMin ? parseFloat(formData.salaryMin) : null,
      salaryMax: formData.salaryMax ? parseFloat(formData.salaryMax) : null,
      minimumExperience: formData.minimumExperience ? parseInt(formData.minimumExperience) : null,
      maximumExperience: formData.maximumExperience ? parseInt(formData.maximumExperience) : null,
      education: formData.education,
      requiredSkills: formData.requiredSkills ? formData.requiredSkills.split(',').map(s => s.trim()).filter(Boolean) : [],
      preferredSkills: formData.preferredSkills ? formData.preferredSkills.split(',').map(s => s.trim()).filter(Boolean) : [],
      responsibilities: formData.responsibilities ? formData.responsibilities.split('\n').map(s => s.trim()).filter(Boolean) : [],
      requirements: formData.requirements ? formData.requirements.split('\n').map(s => s.trim()).filter(Boolean) : [],
      active: formData.active,
    }

    try {
      if (isEditing) {
        await recruiterApi.updateJob(id, payload)
      } else {
        await recruiterApi.createJob(payload)
      }
      navigate('/recruiter/jobs')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save job posting.')
    } finally {
      setLoading(false)
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
          <ArrowLeft className="w-4 h-4" /> Back to Job Listings
        </button>

        <div className="cl-card p-5">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-lg bg-[#ebf0ff] text-[#1a56db] text-xs font-semibold mb-2">
            <Briefcase className="w-3.5 h-3.5" /> Job Posting Editor
          </div>
          <h1 className="text-2xl sm:text-3xl font-bold text-[#0f172a] tracking-tight">
            {isEditing ? 'Edit Job Listing' : 'Create New Job Listing'}
          </h1>
          <p className="text-sm text-[#64748b] mt-1">
            Provide clear job details, required skills, and compensation parameters for accurate AI candidate matching.
          </p>
        </div>

        {error && (
          <div className="cl-alert cl-alert-error" role="alert">
            <AlertCircle className="w-5 h-5 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {fetching ? (
          <div className="cl-card animate-pulse space-y-6">
            <div className="h-6 bg-slate-200/50 rounded w-1/3"></div>
            <div className="h-10 bg-slate-100/50 rounded w-full"></div>
            <div className="h-10 bg-slate-100/50 rounded w-full"></div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="cl-card space-y-6">
            
            {/* Title & Location */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div>
                <label htmlFor="job-title" className="cl-label">Job Title *</label>
                <input
                  id="job-title"
                  type="text"
                  name="title"
                  required
                  value={formData.title}
                  onChange={handleChange}
                  placeholder="e.g. Senior Full Stack Engineer"
                  className="cl-input"
                />
              </div>

              <div>
                <label htmlFor="job-location" className="cl-label">Location *</label>
                <input
                  id="job-location"
                  type="text"
                  name="location"
                  required
                  value={formData.location}
                  onChange={handleChange}
                  placeholder="e.g. San Francisco, CA or Remote"
                  className="cl-input"
                />
              </div>
            </div>

            {/* Work Mode & Job Type */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div>
                <label htmlFor="work-mode" className="cl-label">Work Mode *</label>
                <select
                  id="work-mode"
                  name="workMode"
                  value={formData.workMode}
                  onChange={handleChange}
                  className="cl-input font-medium"
                >
                  <option value="REMOTE">Remote</option>
                  <option value="HYBRID">Hybrid</option>
                  <option value="ONSITE">Onsite</option>
                </select>
              </div>

              <div>
                <label htmlFor="job-type" className="cl-label">Employment Type *</label>
                <select
                  id="job-type"
                  name="jobType"
                  value={formData.jobType}
                  onChange={handleChange}
                  className="cl-input font-medium"
                >
                  <option value="FULL_TIME">Full Time</option>
                  <option value="PART_TIME">Part Time</option>
                  <option value="INTERNSHIP">Internship</option>
                  <option value="CONTRACT">Contract</option>
                </select>
              </div>
            </div>

            {/* Salary Range */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div>
                <label htmlFor="salary-min" className="cl-label">Minimum Annual Salary (₹)</label>
                <input
                  id="salary-min"
                  type="number"
                  name="salaryMin"
                  value={formData.salaryMin}
                  onChange={handleChange}
                  placeholder="e.g. 1000000"
                  className="cl-input"
                />
              </div>

              <div>
                <label htmlFor="salary-max" className="cl-label">Maximum Annual Salary (₹)</label>
                <input
                  id="salary-max"
                  type="number"
                  name="salaryMax"
                  value={formData.salaryMax}
                  onChange={handleChange}
                  placeholder="e.g. 1400000"
                  className="cl-input"
                />
              </div>
            </div>

            {/* Experience & Education */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
              <div>
                <label htmlFor="min-exp" className="cl-label">Min Experience (Years)</label>
                <input
                  id="min-exp"
                  type="number"
                  name="minimumExperience"
                  value={formData.minimumExperience}
                  onChange={handleChange}
                  placeholder="e.g. 2"
                  className="cl-input"
                />
              </div>

              <div>
                <label htmlFor="max-exp" className="cl-label">Max Experience (Years)</label>
                <input
                  id="max-exp"
                  type="number"
                  name="maximumExperience"
                  value={formData.maximumExperience}
                  onChange={handleChange}
                  placeholder="e.g. 5"
                  className="cl-input"
                />
              </div>

              <div>
                <label htmlFor="education-req" className="cl-label">Education Requirement</label>
                <input
                  id="education-req"
                  type="text"
                  name="education"
                  value={formData.education}
                  onChange={handleChange}
                  placeholder="e.g. B.S. in Computer Science"
                  className="cl-input"
                />
              </div>
            </div>

            {/* Required & Preferred Skills */}
            <div>
              <label htmlFor="req-skills" className="cl-label">Required Skills (Comma separated)</label>
              <input
                id="req-skills"
                type="text"
                name="requiredSkills"
                value={formData.requiredSkills}
                onChange={handleChange}
                placeholder="Java, Spring Boot, React, PostgreSQL"
                className="cl-input"
              />
            </div>

            <div>
              <label htmlFor="pref-skills" className="cl-label">Preferred Skills (Comma separated)</label>
              <input
                id="pref-skills"
                type="text"
                name="preferredSkills"
                value={formData.preferredSkills}
                onChange={handleChange}
                placeholder="AWS, Docker, Microservices"
                className="cl-input"
              />
            </div>

            {/* Description */}
            <div>
              <label htmlFor="job-desc" className="cl-label">Job Description *</label>
              <textarea
                id="job-desc"
                name="description"
                required
                rows={5}
                value={formData.description}
                onChange={handleChange}
                placeholder="Provide a comprehensive summary of the team, objectives, and overall role..."
                className="cl-textarea"
              />
            </div>

            {/* Responsibilities & Requirements */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div>
                <label htmlFor="responsibilities" className="cl-label">Key Responsibilities (One per line)</label>
                <textarea
                  id="responsibilities"
                  name="responsibilities"
                  rows={4}
                  value={formData.responsibilities}
                  onChange={handleChange}
                  placeholder="Architect scalable REST APIs&#10;Lead code reviews and technical mentoring"
                  className="cl-textarea"
                />
              </div>

              <div>
                <label htmlFor="requirements" className="cl-label">Requirements (One per line)</label>
                <textarea
                  id="requirements"
                  name="requirements"
                  rows={4}
                  value={formData.requirements}
                  onChange={handleChange}
                  placeholder="3+ years of professional backend development&#10;Proficiency in relational databases"
                  className="cl-textarea"
                />
              </div>
            </div>

            {/* Active Checkbox */}
            <div className="flex items-center gap-2.5 pt-2">
              <input
                type="checkbox"
                id="job-active"
                name="active"
                checked={formData.active}
                onChange={handleChange}
                className="w-4 h-4 rounded border-[#cbd5e1] text-[#1a56db] focus:ring-[#1a56db]"
              />
              <label htmlFor="job-active" className="text-xs font-semibold text-[#0f172a] cursor-pointer">
                Job active and accepting candidate applications
              </label>
            </div>

            {/* Action Buttons */}
            <div className="flex justify-end gap-3 pt-4 border-t border-[#e4e8ef]">
              <button
                type="button"
                onClick={() => navigate('/recruiter/jobs')}
                className="cl-btn-secondary text-sm"
              >
                Cancel
              </button>

              <button
                type="submit"
                disabled={loading}
                className="cl-btn-primary text-sm"
              >
                {loading ? 'Saving Job...' : isEditing ? 'Update Job Listing' : 'Publish Job Listing'}
              </button>
            </div>

          </form>
        )}
        </div>
      </main>

      <Footer />
    </div>
  )
}
