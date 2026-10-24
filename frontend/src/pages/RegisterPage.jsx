import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { User, Mail, Phone, UserCheck, Briefcase, AlertCircle, ArrowRight, ShieldCheck, CheckCircle2 } from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import VerifyOtpModal from '../components/VerifyOtpModal'
import { useAuth } from '../context/AuthContext'
import { THEME_MODE } from '../config/themeConfig'

export default function RegisterPage() {
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    role: 'JOB_SEEKER',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showOtpModal, setShowOtpModal] = useState(false)

  const { registerSendOtp, registerVerifyOtp } = useAuth()
  const navigate = useNavigate()

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
    setError('')
  }

  const handleRoleSelect = (role) => setFormData({ ...formData, role })

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!formData.fullName || !formData.email) {
      setError('Please fill in all required fields.')
      return
    }
    setLoading(true)
    setError('')
    try {
      await registerSendOtp(formData)
      setShowOtpModal(true)
    } catch (err) {
      const serverMsg = err.response?.data?.message || err.message
      setError(serverMsg || 'Registration failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleOtpVerified = (res) => {
    setShowOtpModal(false)
    const targetRole = res?.data?.user?.role || formData.role
    navigate(targetRole === 'RECRUITER' ? '/recruiter/jobs' : '/applicant/jobs', { replace: true })
  }

  const isSeeker = formData.role === 'JOB_SEEKER'
  const isRecruiter = formData.role === 'RECRUITER'

  return (
    <div className={`min-h-screen flex flex-col ${THEME_MODE === 'glass' ? 'bg-[#F6F5F1] text-[#1B211D]' : 'bg-[#F1F4F2] text-[#202824]'}`}>
      <Navbar />

      {/* ── Split-Screen Layout (Left Hero / Right Panel) ───────────── */}
      <main className="flex-1 relative flex flex-col lg:flex-row overflow-hidden min-h-[calc(100vh-65px)]">
        {THEME_MODE === 'glass' && (
          <>
            {/* Full-width Split Background Image — Natural Photograph */}
            <div
              className="absolute inset-0 bg-cover bg-center bg-no-repeat pointer-events-none"
              style={{ backgroundImage: "url('/images/login_laptop_bg.png')" }}
            />
            {/* Subtle full-section dark overlay on left hero section for white text readability */}
            <div className="absolute inset-0 lg:w-[72%] bg-black/45 pointer-events-none" />
          </>
        )}
        
        {/* Left Hero Content */}
        <div className={
          THEME_MODE === 'glass'
            ? 'relative z-10 w-full lg:w-[72%] p-8 sm:p-14 lg:p-20 flex flex-col justify-end text-white min-h-[380px] lg:min-h-full'
            : 'w-full lg:w-[68%] xl:w-[72%] p-8 sm:p-14 lg:p-20 flex flex-col justify-end bg-[#E8F0E9] border-r border-[#D9E1DB] text-[#202824] min-h-[380px] lg:min-h-full'
        }>
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
            className="max-w-2xl"
          >
            <div className={`inline-flex items-center gap-2 text-xs font-semibold uppercase tracking-wider mb-3 font-mono ${
              THEME_MODE === 'glass' ? 'text-emerald-400' : 'text-[#11643B]'
            }`}>
              <span className={`w-2.5 h-2.5 rounded-full ${THEME_MODE === 'glass' ? 'bg-emerald-400' : 'bg-[#11643B]'}`} />
              INTELLIGENT RECRUITMENT PORTAL
            </div>
            
            <h1 className={`text-3xl sm:text-4xl lg:text-5xl font-extrabold leading-tight mb-4 tracking-tight ${
              THEME_MODE === 'glass' ? 'text-white' : 'text-[#202824]'
            }`}>
              Join CareerLens &amp; unlock<br />
              AI job matching.
            </h1>
            
            <p className={`text-sm sm:text-base leading-relaxed max-w-xl mb-8 ${
              THEME_MODE === 'glass' ? 'text-gray-200' : 'text-[#626B65]'
            }`}>
              Create your account to start evaluating role compatibility, building targeted resumes, and applying to top positions.
            </p>

            {/* Value Propositions */}
            <div className={`pt-6 border-t grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs font-semibold ${
              THEME_MODE === 'glass' ? 'border-white/20 text-white' : 'border-[#D9E1DB] text-[#202824]'
            }`}>
              <div className="flex items-center gap-2">
                <CheckCircle2 className={`w-4 h-4 shrink-0 ${THEME_MODE === 'glass' ? 'text-emerald-400' : 'text-[#11643B]'}`} />
                <span>Automated skill parsing</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className={`w-4 h-4 shrink-0 ${THEME_MODE === 'glass' ? 'text-emerald-400' : 'text-[#11643B]'}`} />
                <span>AI-ranked match score</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className={`w-4 h-4 shrink-0 ${THEME_MODE === 'glass' ? 'text-emerald-400' : 'text-[#11643B]'}`} />
                <span>Free account setup</span>
              </div>
            </div>
          </motion.div>
        </div>

        {/* Right Register Panel */}
        <div className={
          THEME_MODE === 'glass'
            ? 'relative z-10 w-full lg:w-[28%] min-w-[360px] auth-form-panel p-6 sm:p-10 flex flex-col justify-center shrink-0'
            : 'w-full lg:w-[32%] xl:w-[28%] min-w-[360px] bg-white border-l border-[#D9E1DB] p-6 sm:p-10 flex flex-col justify-center shrink-0'
        }>
          <motion.div
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.35 }}
            className="w-full"
          >
            {/* Form Header */}
            <div className="mb-5">
              <div className={`w-10 h-10 rounded-[4px] border flex items-center justify-center mb-3 shadow-sm ${
                THEME_MODE === 'glass'
                  ? 'bg-[#0E5C34]/10 border-[#0E5C34]/20 text-[#0E5C34]'
                  : 'bg-[#E8F0E9] border-[#D9E1DB] text-[#11643B]'
              }`}>
                <ShieldCheck className="w-5 h-5" />
              </div>
              <h2 className="text-2xl font-bold">Create your account</h2>
              <p className={`text-xs mt-1 ${THEME_MODE === 'glass' ? 'text-[#5B655E]' : 'text-[#626B65]'}`}>
                Fill in your details below to get started with CareerLens.
              </p>
            </div>

            {/* Role selector */}
            <fieldset className="mb-4">
              <legend className="text-xs font-semibold text-[#202824] mb-2">I'm joining as a</legend>
              <div className="grid grid-cols-2 gap-2.5">
                <button
                  type="button"
                  id="role-seeker"
                  onClick={() => handleRoleSelect('JOB_SEEKER')}
                  aria-pressed={isSeeker}
                  className={[
                    'p-2.5 rounded-[4px] border text-left transition-all flex items-center gap-2',
                    isSeeker
                      ? 'bg-[#11643B] border-[#11643B] text-white shadow-sm'
                      : 'bg-white border-[#D9E1DB] text-[#202824] hover:border-[#11643B]/40',
                  ].join(' ')}
                >
                  <UserCheck className={`w-4 h-4 shrink-0 ${isSeeker ? 'text-white' : 'text-[#11643B]'}`} />
                  <div>
                    <div className={`text-xs font-bold ${isSeeker ? 'text-white' : 'text-[#202824]'}`}>
                      Job Seeker
                    </div>
                    <div className={`text-[10px] ${isSeeker ? 'text-[#E8F0E9]' : 'text-[#626B65]'}`}>Looking for work</div>
                  </div>
                </button>

                <button
                  type="button"
                  id="role-recruiter"
                  onClick={() => handleRoleSelect('RECRUITER')}
                  aria-pressed={isRecruiter}
                  className={[
                    'p-2.5 rounded-[4px] border text-left transition-all flex items-center gap-2',
                    isRecruiter
                      ? 'bg-[#11643B] border-[#11643B] text-white shadow-sm'
                      : 'bg-white border-[#D9E1DB] text-[#202824] hover:border-[#11643B]/40',
                  ].join(' ')}
                >
                  <Briefcase className={`w-4 h-4 shrink-0 ${isRecruiter ? 'text-white' : 'text-[#11643B]'}`} />
                  <div>
                    <div className={`text-xs font-bold ${isRecruiter ? 'text-white' : 'text-[#202824]'}`}>
                      Recruiter
                    </div>
                    <div className={`text-[10px] ${isRecruiter ? 'text-[#E8F0E9]' : 'text-[#626B65]'}`}>Hiring talent</div>
                  </div>
                </button>
              </div>
            </fieldset>

            {error && (
              <div className="cl-alert cl-alert-error mb-4" role="alert">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span className="text-xs font-medium">{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-3" noValidate>
              <div>
                <label htmlFor="register-name" className="text-xs font-semibold text-[#202824] mb-1 block">Full name</label>
                <div className="cl-input-group">
                  <div className="cl-input-icon-left">
                    <User className="w-4 h-4" />
                  </div>
                  <input
                    type="text"
                    name="fullName"
                    id="register-name"
                    required
                    autoComplete="name"
                    value={formData.fullName}
                    onChange={handleChange}
                    placeholder="Your full name"
                    className="cl-input cl-input-with-icon text-sm bg-white text-[#202824] border-[#D9E1DB] focus:border-[#11643B]"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="register-email" className="text-xs font-semibold text-[#202824] mb-1 block">Email address</label>
                <div className="cl-input-group">
                  <div className="cl-input-icon-left">
                    <Mail className="w-4 h-4" />
                  </div>
                  <input
                    type="email"
                    name="email"
                    id="register-email"
                    required
                    autoComplete="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="you@example.com"
                    className="cl-input cl-input-with-icon text-sm bg-white text-[#202824] border-[#D9E1DB] focus:border-[#11643B]"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="register-phone" className="text-xs font-semibold text-[#202824] mb-1 block">
                  Phone number{' '}
                  <span className="text-[#626B65] font-normal normal-case tracking-normal">
                    (optional)
                  </span>
                </label>
                <div className="cl-input-group">
                  <div className="cl-input-icon-left">
                    <Phone className="w-4 h-4" />
                  </div>
                  <input
                    type="tel"
                    name="phone"
                    id="register-phone"
                    autoComplete="tel"
                    value={formData.phone}
                    onChange={handleChange}
                    placeholder="+91 9876543210"
                    className="cl-input cl-input-with-icon text-sm bg-white text-[#202824] border-[#D9E1DB] focus:border-[#11643B]"
                  />
                </div>
              </div>

              <button
                type="submit"
                id="register-submit"
                disabled={loading}
                className="cl-btn-primary w-full justify-center py-2.5 text-sm font-semibold shadow-sm bg-[#11643B] hover:bg-[#0B4628] text-white mt-1"
              >
                {loading ? 'Sending code…' : (
                  <>
                    Continue
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>

            <div className="mt-5 pt-4 border-t border-[#D9E1DB] text-center text-xs text-[#626B65]">
              Already have an account?{' '}
              <Link to="/login" className="cl-link font-bold text-[#11643B] hover:underline">
                Sign in
              </Link>
            </div>
          </motion.div>
        </div>
      </main>

      <Footer />

      <VerifyOtpModal
        isOpen={showOtpModal}
        onClose={() => setShowOtpModal(false)}
        email={formData.email}
        type="REGISTER"
        onSuccess={handleOtpVerified}
        verifyApi={(payload) => registerVerifyOtp({ ...payload, role: formData.role, fullName: formData.fullName, phone: formData.phone })}
      />
    </div>
  )
}

