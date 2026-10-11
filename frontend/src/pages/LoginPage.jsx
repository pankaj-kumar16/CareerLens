import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Mail, ArrowRight, AlertCircle, UserCheck, Briefcase, CheckCircle2, ShieldCheck } from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import VerifyOtpModal from '../components/VerifyOtpModal'
import { useAuth } from '../context/AuthContext'
import { THEME_MODE } from '../config/themeConfig'

export default function LoginPage() {
  const [formData, setFormData] = useState({
    email: '',
    role: 'JOB_SEEKER',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showOtpModal, setShowOtpModal] = useState(false)

  const { loginSendOtp, loginVerifyOtp } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = location.state?.from?.pathname

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
    setError('')
  }

  const handleRoleSelect = (role) => {
    setFormData({ ...formData, role })
    setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!formData.email) {
      setError('Please enter your email address.')
      return
    }
    setLoading(true)
    setError('')
    try {
      await loginSendOtp(formData)
      setShowOtpModal(true)
    } catch (err) {
      const serverMsg = err.response?.data?.message || err.message
      setError(serverMsg || 'Failed to send OTP. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleOtpVerified = (res) => {
    setShowOtpModal(false)
    const targetRole = res.data?.user?.role || formData.role
    const dest = from || (targetRole === 'RECRUITER' ? '/recruiter/dashboard' : '/seeker/dashboard')
    navigate(dest, { replace: true })
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
              CAREERLENS • SMARTER CAREER MATCHING
            </div>
            
            <h1 className={`text-3xl sm:text-4xl lg:text-5xl font-extrabold leading-tight mb-4 tracking-tight ${
              THEME_MODE === 'glass' ? 'text-white' : 'text-[#202824]'
            }`}>
              Find the right opportunity.<br />
              Build your future.
            </h1>
            
            <p className={`text-sm sm:text-base leading-relaxed max-w-xl mb-8 ${
              THEME_MODE === 'glass' ? 'text-gray-200' : 'text-[#626B65]'
            }`}>
              Discover relevant jobs, showcase your skills, and connect with opportunities that match your career goals.
            </p>

            {/* Value Propositions */}
            <div className={`pt-6 border-t grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs font-semibold ${
              THEME_MODE === 'glass' ? 'border-white/20 text-white' : 'border-[#D9E1DB] text-[#202824]'
            }`}>
              <div className="flex items-center gap-2">
                <CheckCircle2 className={`w-4 h-4 shrink-0 ${THEME_MODE === 'glass' ? 'text-emerald-400' : 'text-[#11643B]'}`} />
                <span>AI-powered resume matching</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className={`w-4 h-4 shrink-0 ${THEME_MODE === 'glass' ? 'text-emerald-400' : 'text-[#11643B]'}`} />
                <span>Personalized job recommendations</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className={`w-4 h-4 shrink-0 ${THEME_MODE === 'glass' ? 'text-emerald-400' : 'text-[#11643B]'}`} />
                <span>One place to track applications</span>
              </div>
            </div>
          </motion.div>
        </div>

        {/* Right Login Panel */}
        <div className={
          THEME_MODE === 'glass'
            ? 'relative z-10 w-full lg:w-[28%] min-w-[340px] auth-form-panel p-6 sm:p-10 flex flex-col justify-center shrink-0'
            : 'w-full lg:w-[32%] xl:w-[28%] min-w-[340px] bg-white border-l border-[#D9E1DB] p-6 sm:p-10 flex flex-col justify-center shrink-0'
        }>
          <motion.div
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.35 }}
            className="w-full"
          >
            {/* Form Header */}
            <div className="mb-6">
              <div className={`w-10 h-10 rounded-[4px] border flex items-center justify-center mb-3 shadow-sm ${
                THEME_MODE === 'glass'
                  ? 'bg-[#0E5C34]/10 border-[#0E5C34]/20 text-[#0E5C34]'
                  : 'bg-[#E8F0E9] border-[#D9E1DB] text-[#11643B]'
              }`}>
                <ShieldCheck className="w-5 h-5" />
              </div>
              <h2 className="text-2xl font-bold">Welcome back</h2>
              <p className={`text-xs mt-1 ${THEME_MODE === 'glass' ? 'text-[#5B655E]' : 'text-[#626B65]'}`}>
                Sign in to continue your career journey.
              </p>
            </div>

            {/* Role selector */}
            <fieldset className="mb-6">
              <legend className="text-xs font-semibold text-[#202824] mb-2.5">Sign in as</legend>
              <div className="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  id="login-role-seeker"
                  onClick={() => handleRoleSelect('JOB_SEEKER')}
                  aria-pressed={isSeeker}
                  className={[
                    'p-3 rounded-[4px] border text-left transition-all flex items-center gap-2.5',
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
                    <div className={`text-[11px] ${isSeeker ? 'text-[#E8F0E9]' : 'text-[#626B65]'}`}>Looking for work</div>
                  </div>
                </button>

                <button
                  type="button"
                  id="login-role-recruiter"
                  onClick={() => handleRoleSelect('RECRUITER')}
                  aria-pressed={isRecruiter}
                  className={[
                    'p-3 rounded-[4px] border text-left transition-all flex items-center gap-2.5',
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
                    <div className={`text-[11px] ${isRecruiter ? 'text-[#E8F0E9]' : 'text-[#626B65]'}`}>Hiring talent</div>
                  </div>
                </button>
              </div>
            </fieldset>

            {error && (
              <div className="cl-alert cl-alert-error mb-5" role="alert">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span className="text-xs font-medium">{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              <div>
                <label htmlFor="login-email" className="text-xs font-semibold text-[#202824] mb-1.5 block">Email address</label>
                <div className="cl-input-group">
                  <div className="cl-input-icon-left">
                    <Mail className="w-4 h-4" />
                  </div>
                  <input
                    type="email"
                    name="email"
                    id="login-email"
                    required
                    autoComplete="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="you@example.com"
                    className="cl-input cl-input-with-icon text-sm bg-white text-[#202824] border-[#D9E1DB] focus:border-[#11643B]"
                  />
                </div>
              </div>

              <button
                type="submit"
                id="login-submit"
                disabled={loading}
                className="cl-btn-primary w-full justify-center py-2.5 text-sm font-semibold shadow-sm bg-[#11643B] hover:bg-[#0B4628] text-white"
              >
                {loading ? 'Sending code…' : (
                  <>
                    Continue with email
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>

            <div className="mt-6 pt-5 border-t border-[#D9E1DB] text-center text-xs text-[#626B65]">
              Don't have an account?{' '}
              <Link to="/register" className="cl-link font-bold text-[#11643B] hover:underline">
                Create one now
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
        type="LOGIN"
        onSuccess={handleOtpVerified}
        verifyApi={(payload) => loginVerifyOtp({ ...payload, role: formData.role })}
      />
    </div>
  )
}

