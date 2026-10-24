import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { profileApi } from '../services/api'
import {
  CheckCircle2, Circle, AlertCircle, User, Briefcase,
  GraduationCap, Code, Award, MapPin, Phone, Mail, Sparkles, Edit2, X
} from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'

export default function ProfilePage() {
  const [profile, setProfile] = useState(null)
  const [completeness, setCompleteness] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [isEditing, setIsEditing] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)

  const [formData, setFormData] = useState({
    fullName: '', phone: '', location: '', skills: '', education: '',
    experience: '', projects: '', certifications: '', preferredRoles: '',
    preferredLocations: '', expectedSalary: '', workMode: 'HYBRID',
  })

  const fetchData = async () => {
    try {
      setLoading(true)
      const [profData, compData] = await Promise.all([
        profileApi.getJobSeekerProfile(),
        profileApi.getCompleteness(),
      ])
      setProfile(profData)
      setCompleteness(compData)
      setFormData({
        fullName: profData.fullName || '', phone: profData.phone || '',
        location: profData.location || '', skills: profData.skills || '',
        education: profData.education || '', experience: profData.experience || '',
        projects: profData.projects || '', certifications: profData.certifications || '',
        preferredRoles: profData.preferredRoles || '', preferredLocations: profData.preferredLocations || '',
        expectedSalary: profData.expectedSalary || '', workMode: profData.workMode || 'HYBRID',
      })
    } catch (err) {
      console.error('Failed to load profile data:', err)
      setError(err?.response?.data?.message || 'Failed to load profile data.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [])

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      setSaving(true)
      setError(null)
      await profileApi.updateJobSeekerProfile(formData)
      setSuccess('Profile updated successfully!')
      setIsEditing(false)
      fetchData()
      setTimeout(() => setSuccess(null), 4000)
    } catch (err) {
      console.error('Failed to update profile:', err)
      setError(err?.response?.data?.message || 'Failed to update profile.')
    } finally {
      setSaving(false)
    }
  }

  const inputCls = 'cl-input'
  const textareaCls = 'cl-input resize-none'

  if (loading) {
    return (
      <div className="min-h-screen bg-[#f8f9fb] flex items-center justify-center">
        <div className="cl-spinner" />
      </div>
    )
  }

  return (
    <div className="cl-page-jobseeker">
      <Navbar />

      <main className="cl-main">
        <div className="cl-panel p-5 sm:p-6 space-y-6">
          {/* Page header */}
        <motion.div
          initial={{ opacity: 0, y: 14 }}
          animate={{ opacity: 1, y: 0 }}
          className="cl-page-header flex flex-col sm:flex-row sm:items-center justify-between gap-4"
        >
          <div>
            <span className="cl-eyebrow">My Account</span>
            <h1 className="cl-page-title flex items-center gap-2">
              <User className="w-6 h-6 text-[#0E5C34]" /> Candidate Profile
            </h1>
            <p className="cl-page-subtitle">Manage your details and keep your profile score high.</p>
          </div>
          <button
            onClick={() => setIsEditing(!isEditing)}
            className={isEditing ? 'cl-btn-secondary' : 'cl-btn-primary'}
          >
            {isEditing ? <><X className="w-4 h-4" /> Cancel</> : <><Edit2 className="w-4 h-4" /> Edit Profile</>}
          </button>
        </motion.div>

        {/* Alerts */}
        {error && (
          <div className="cl-alert cl-alert-error" role="alert">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}
        {success && (
          <div className="cl-alert cl-alert-success" role="status">
            <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
            <span>{success}</span>
          </div>
        )}

        {/* Completeness card */}
        {completeness && (
          <div className="cl-card p-6 sm:p-8 space-y-5">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-5">
              <div>
                <div className="flex items-center gap-2 text-[#0E5C34] text-xs font-bold uppercase tracking-wider mb-2">
                  <Sparkles className="w-4 h-4 text-[#B45309]" /> Profile Completeness
                </div>
                <div className="text-4xl sm:text-5xl font-extrabold text-[#1B211D] mono">{completeness.percentage}%</div>
                <p className="text-sm text-[#5B655E] mt-1.5">{completeness.tip}</p>
              </div>
              <div className="w-full md:w-64 space-y-2">
                <div className="h-3 bg-[#E4E2DB] rounded-full overflow-hidden">
                  <div
                    className="h-full bg-[#0E5C34] rounded-full transition-all duration-700"
                    style={{ width: `${completeness.percentage}%` }}
                  />
                </div>
                <p className="text-xs text-[#8F9992] text-right font-semibold mono">
                  {completeness.percentage === 100 ? 'All sections complete!' : `${100 - completeness.percentage}% remaining`}
                </p>
              </div>
            </div>

            {completeness.sections?.length > 0 && (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3 pt-5 border-t border-[#E4E2DB]">
                {completeness.sections.map((sec, idx) => (
                  <div
                    key={idx}
                    className={`p-3.5 rounded border flex items-start gap-3 ${
                      sec.complete
                        ? 'bg-[#EAF5EE] border-[#B9E3C6]'
                        : 'bg-[#FEF3C7] border-[#FDE68A]'
                    }`}
                  >
                    {sec.complete
                      ? <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5 text-[#0E5C34]" />
                      : <Circle className="w-4 h-4 shrink-0 mt-0.5 text-[#B45309]" />
                    }
                    <div>
                      <h4 className={`font-bold text-xs ${sec.complete ? 'text-[#0E5C34]' : 'text-[#92400E]'}`}>{sec.name}</h4>
                      <p className="text-[11px] text-[#5B655E] mt-0.5">{sec.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Edit form */}
        {isEditing ? (
          <form onSubmit={handleSubmit} className="cl-card p-6 sm:p-8 space-y-6">
            <h3 className="text-base font-bold text-[#1B211D] border-b border-[#E4E2DB] pb-4">
              Update Profile Information
            </h3>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
              {[
                { name: 'fullName', label: 'Full Name', type: 'text', placeholder: 'John Doe' },
                { name: 'phone', label: 'Phone Number', type: 'text', placeholder: '+91 9876543210' },
                { name: 'location', label: 'Location', type: 'text', placeholder: 'Mumbai, India' },
              ].map(({ name, label, type, placeholder }) => (
                <div key={name}>
                  <label className="cl-label">{label}</label>
                  <input type={type} name={name} value={formData[name]} onChange={handleChange} placeholder={placeholder} className={inputCls} />
                </div>
              ))}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
              {[
                { name: 'skills', label: 'Skills (Comma-separated)', placeholder: 'Java, Spring Boot, React, MongoDB, AWS' },
                { name: 'education', label: 'Education Details', placeholder: 'B.Tech Computer Science, IIT Bombay (2020–2024)' },
                { name: 'experience', label: 'Work Experience', placeholder: 'Software Developer at TechCorp (2 years)' },
                { name: 'projects', label: 'Key Projects', placeholder: 'CareerLens AI Job Portal, E-Commerce Platform' },
              ].map(({ name, label, placeholder }) => (
                <div key={name}>
                  <label className="cl-label">{label}</label>
                  <textarea name={name} rows="3" value={formData[name]} onChange={handleChange} placeholder={placeholder} className={textareaCls} />
                </div>
              ))}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
              {[
                { name: 'certifications', label: 'Certifications', placeholder: 'AWS Certified Solutions Architect' },
                { name: 'preferredRoles', label: 'Preferred Roles', placeholder: 'Backend Developer, Full Stack Engineer' },
                { name: 'expectedSalary', label: 'Expected Salary', placeholder: '₹15 LPA' },
              ].map(({ name, label, placeholder }) => (
                <div key={name}>
                  <label className="cl-label">{label}</label>
                  <input type="text" name={name} value={formData[name]} onChange={handleChange} placeholder={placeholder} className={inputCls} />
                </div>
              ))}
            </div>

            <div className="max-w-xs">
              <label className="cl-label">Work Mode</label>
              <select name="workMode" value={formData.workMode} onChange={handleChange} className={inputCls}>
                <option value="REMOTE">Remote</option>
                <option value="HYBRID">Hybrid</option>
                <option value="ONSITE">On-site</option>
              </select>
            </div>

            <div className="flex justify-end gap-3 pt-4 border-t border-[#E4E2DB]">
              <button type="button" onClick={() => setIsEditing(false)} className="cl-btn-secondary">
                Cancel
              </button>
              <button type="submit" disabled={saving} className="cl-btn-primary">
                {saving ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </form>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Basic Details */}
            <div className="cl-card p-6">
              <h3 className="text-xs font-bold uppercase tracking-wider text-[#5B655E] flex items-center gap-2 border-b border-[#E4E2DB] pb-3 mb-4">
                <User className="w-4 h-4 text-[#0E5C34]" /> Basic Details
              </h3>
              <div className="space-y-3 text-sm">
                <div className="flex items-center gap-3"><User className="w-4 h-4 text-[#8F9992] shrink-0" /><span className="font-bold text-[#1B211D]">{profile?.fullName || '—'}</span></div>
                <div className="flex items-center gap-3"><Phone className="w-4 h-4 text-[#8F9992] shrink-0" /><span className="text-[#3A4139] mono">{profile?.phone || 'No phone added'}</span></div>
                <div className="flex items-center gap-3"><MapPin className="w-4 h-4 text-[#8F9992] shrink-0" /><span className="text-[#3A4139]">{profile?.location || 'No location'}</span></div>
                <div className="flex items-center gap-3"><Mail className="w-4 h-4 text-[#8F9992] shrink-0" /><span className="text-[#3A4139]">{profile?.email || '—'}</span></div>
              </div>
            </div>

            {/* Skills & Preferences */}
            <div className="cl-card p-6">
              <h3 className="text-xs font-bold uppercase tracking-wider text-[#5B655E] flex items-center gap-2 border-b border-[#E4E2DB] pb-3 mb-4">
                <Code className="w-4 h-4 text-[#0E5C34]" /> Skills & Preferences
              </h3>
              <div className="space-y-3 text-sm">
                <div>
                  <p className="cl-section-label mb-1.5">Skills</p>
                  <div className="flex flex-wrap gap-1.5">
                    {profile?.skills ? profile.skills.split(',').map((s, i) => (
                      <span key={i} className="cl-skill-tag">{s.trim()}</span>
                    )) : <span className="text-[#8F9992]">—</span>}
                  </div>
                </div>
                <div>
                  <p className="cl-section-label mb-1">Preferred Roles</p>
                  <p className="text-[#3A4139]">{profile?.preferredRoles || '—'}</p>
                </div>
                <div>
                  <p className="cl-section-label mb-1">Work Mode</p>
                  <span className="cl-badge cl-badge-green">{profile?.workMode || 'HYBRID'}</span>
                </div>
              </div>
            </div>

            {/* Education */}
            <div className="cl-card p-6">
              <h3 className="text-xs font-bold uppercase tracking-wider text-[#5B655E] flex items-center gap-2 border-b border-[#E4E2DB] pb-3 mb-4">
                <GraduationCap className="w-4 h-4 text-[#0E5C34]" /> Education
              </h3>
              <p className="text-sm text-[#3A4139] leading-relaxed">{profile?.education || 'No education records added.'}</p>
            </div>

            {/* Experience */}
            <div className="cl-card p-6">
              <h3 className="text-xs font-bold uppercase tracking-wider text-[#5B655E] flex items-center gap-2 border-b border-[#E4E2DB] pb-3 mb-4">
                <Briefcase className="w-4 h-4 text-[#0E5C34]" /> Work Experience
              </h3>
              <p className="text-sm text-[#3A4139] leading-relaxed">{profile?.experience || 'No work experience listed.'}</p>
            </div>

            {/* Projects */}
            {profile?.projects && (
              <div className="cl-card p-6">
                <h3 className="text-xs font-bold uppercase tracking-wider text-[#5B655E] flex items-center gap-2 border-b border-[#E4E2DB] pb-3 mb-4">
                  <Code className="w-4 h-4 text-[#0E5C34]" /> Projects
                </h3>
                <p className="text-sm text-[#3A4139] leading-relaxed">{profile.projects}</p>
              </div>
            )}

            {/* Certifications */}
            {profile?.certifications && (
              <div className="cl-card p-6">
                <h3 className="text-xs font-bold uppercase tracking-wider text-[#5B655E] flex items-center gap-2 border-b border-[#E4E2DB] pb-3 mb-4">
                  <Award className="w-4 h-4 text-[#0E5C34]" /> Certifications
                </h3>
                <p className="text-sm text-[#3A4139] leading-relaxed">{profile.certifications}</p>
              </div>
            )}
          </div>
        )}
        </div>
      </main>

      <Footer />
    </div>
  )
}
