import { Link } from 'react-router-dom'

const footerLinks = [
  { to: '/applicant/jobs', label: 'Find Jobs' },
  { to: '/login', label: 'Sign in' },
  { to: '/register', label: 'Create account' },
]

export default function Footer() {
  const year = new Date().getFullYear()

  return (
    <footer className="border-t border-[#D9E1DB] bg-white mt-auto">
      <div className="max-w-[76rem] mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">

          {/* Brand */}
          <div>
            <Link to="/" className="inline-flex items-center gap-2 group">
              <div className="w-7 h-7 rounded bg-[#11643B] text-white font-bold text-xs flex items-center justify-center">
                CL
              </div>
              <span className="text-sm font-bold text-[#202824]">
                Career<span className="text-[#11643B]">Lens</span>
              </span>
            </Link>
            <p className="text-xs text-[#626B65] mt-1.5 max-w-xs">
              Resume matching and job discovery for job seekers and recruiters.
            </p>
          </div>

          {/* Links + copyright */}
          <div className="flex flex-col sm:items-end gap-3">
            <nav className="flex flex-wrap gap-x-5 gap-y-2" aria-label="Footer navigation">
              {footerLinks.map(({ to, label }) => (
                <Link
                  key={to}
                  to={to}
                  className="text-xs text-[#626B65] hover:text-[#11643B] transition-colors font-medium"
                >
                  {label}
                </Link>
              ))}
            </nav>
            <p className="text-xs text-[#626B65]">
              © {year} CareerLens. All rights reserved.
            </p>
          </div>
        </div>
      </div>
    </footer>
  )
}
