import { useState, useEffect, useRef } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { KeyRound, AlertCircle, CheckCircle2, RefreshCw, X } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { authApi } from '../services/api'

export default function VerifyOtpModal({ isOpen, onClose, email, type = 'REGISTER', onSuccess, verifyApi }) {
  const [otp, setOtp] = useState(['', '', '', '', '', ''])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [resending, setResending] = useState(false)
  const [timer, setTimer] = useState(60)
  const [resendSuccess, setResendSuccess] = useState(false)

  const inputRefs = useRef([])
  const resolvedVerifyApi = verifyApi || useAuth().verifyOtp

  useEffect(() => {
    let interval = null
    if (isOpen && timer > 0) {
      interval = setInterval(() => setTimer((prev) => prev - 1), 1000)
    }
    return () => clearInterval(interval)
  }, [isOpen, timer])

  if (!isOpen) return null

  const handleChange = (index, value) => {
    if (!/^\d*$/.test(value)) return
    const newOtp = [...otp]
    newOtp[index] = value.slice(-1)
    setOtp(newOtp)
    setError('')
    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus()
    }
  }

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus()
    }
  }

  const handlePaste = (e) => {
    e.preventDefault()
    const pastedData = e.clipboardData.getData('text').trim().slice(0, 6)
    if (/^\d{6}$/.test(pastedData)) {
      setOtp(pastedData.split(''))
      inputRefs.current[5]?.focus()
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const fullCode = otp.join('')
    if (fullCode.length !== 6) {
      setError('Please enter all 6 digits of the code.')
      return
    }
    setLoading(true)
    setError('')
    try {
      const res = await resolvedVerifyApi({ email, otp: fullCode, code: fullCode, type })
      if (res.success) {
        onSuccess?.(res)
      } else {
        setError(res.message || 'Verification failed.')
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Verification failed. Please check the code.')
    } finally {
      setLoading(false)
    }
  }

  const handleResend = async () => {
    if (timer > 0 || resending) return
    setResending(true)
    setError('')
    setResendSuccess(false)
    try {
      const res = await authApi.resendOtp({ email, type })
      if (res.success) {
        setTimer(60)
        setResendSuccess(true)
        setTimeout(() => setResendSuccess(false), 3000)
      } else {
        setError(res.message || 'Failed to resend code.')
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to resend code.')
    } finally {
      setResending(false)
    }
  }

  return (
    <AnimatePresence>
      <div
        className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        role="dialog"
        aria-modal="true"
        aria-labelledby="otp-dialog-title"
      >
        <motion.div
          initial={{ opacity: 0, scale: 0.97, y: 8 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.97 }}
          transition={{ duration: 0.2 }}
          className="relative w-full max-w-sm bg-white border border-[#D9E1DB] rounded-lg shadow-xl p-7"
        >
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-[#8F9992] hover:text-[#202824] p-1.5 rounded-lg hover:bg-[#E8F0E9] transition-colors"
            aria-label="Close dialog"
          >
            <X className="w-4 h-4" />
          </button>

          <div className="flex flex-col items-center text-center mb-6">
            <div className="w-12 h-12 rounded-xl bg-[#E8F0E9] border border-[#D9E1DB] flex items-center justify-center text-[#11643B] mb-4">
              <KeyRound className="w-5 h-5" />
            </div>
            <h3 id="otp-dialog-title" className="text-lg font-bold text-[#202824]">
              Check your email
            </h3>
            <p className="text-xs text-[#626B65] mt-1.5">
              We sent a 6-digit verification code to{' '}
              <span className="font-bold text-[#202824]">{email}</span>
            </p>
          </div>

          {error && (
            <div className="cl-alert cl-alert-error mb-4" role="alert">
              <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
              <span className="text-xs font-medium">{error}</span>
            </div>
          )}

          {resendSuccess && (
            <div className="cl-alert cl-alert-success mb-4" role="status">
              <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
              <span className="text-xs font-medium">New code sent successfully.</span>
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div
              className="flex justify-between gap-2 mb-5"
              onPaste={handlePaste}
              aria-label="One-time code input"
            >
              {otp.map((digit, idx) => (
                <input
                  key={idx}
                  ref={(el) => (inputRefs.current[idx] = el)}
                  type="text"
                  inputMode="numeric"
                  maxLength={1}
                  value={digit}
                  onChange={(e) => handleChange(idx, e.target.value)}
                  onKeyDown={(e) => handleKeyDown(idx, e)}
                  aria-label={`Digit ${idx + 1} of 6`}
                  className={[
                    'w-10 h-12 sm:w-12 sm:h-14 text-center text-xl font-bold mono p-0 shrink-0 leading-none',
                    'border rounded-md outline-none transition-all',
                    'text-[#202824] bg-[#F1F4F2]',
                    digit
                      ? 'border-[#11643B] bg-[#E8F0E9] text-[#11643B]'
                      : 'border-[#D9E1DB] focus:border-[#11643B] focus:bg-white focus:shadow-[0_0_0_3px_rgba(17,100,59,0.15)]',
                  ].join(' ')}
                />
              ))}
            </div>

            <button
              type="submit"
              disabled={loading || otp.join('').length !== 6}
              className="cl-btn-primary w-full justify-center py-2.5 text-sm font-semibold shadow-sm bg-[#11643B] hover:bg-[#0B4628]"
            >
              {loading ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" />
                  Verifying…
                </>
              ) : (
                'Verify and continue'
              )}
            </button>
          </form>

          <div className="mt-5 pt-4 border-t border-[#D9E1DB] flex items-center justify-between text-xs text-[#626B65]">
            <span>Didn't receive code?</span>
            <button
              type="button"
              onClick={handleResend}
              disabled={timer > 0 || resending}
              className="text-[#11643B] font-bold hover:underline disabled:text-[#8F9992] disabled:no-underline disabled:cursor-not-allowed transition-colors mono"
            >
              {resending ? 'Sending…' : timer > 0 ? `Resend in ${timer}s` : 'Resend code'}
            </button>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  )
}
