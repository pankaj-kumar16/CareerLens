import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="border-t border-[#D9E1DB] bg-white mt-auto">
      <div className="max-w-[76rem] mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-10 flex items-center justify-center">
        <Link
          to="/"
          className="inline-flex items-center gap-2.5 group transition-opacity hover:opacity-90"
          aria-label="CareerLens home"
        >
          <div className="w-8 h-8 rounded bg-[#11643B] text-white font-bold text-xs flex items-center justify-center shadow-sm">
            CL
          </div>
          <span className="text-lg sm:text-xl font-bold text-[#202824] tracking-tight">
            Career<span className="text-[#11643B]">Lens</span>
          </span>
        </Link>
      </div>
    </footer>
  )
}
