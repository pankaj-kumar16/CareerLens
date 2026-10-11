import { useState, useEffect } from 'react'
import { 
  Bell, CheckCircle2, AlertCircle, Calendar, Sparkles, 
  Send, Check, Clock, ShieldAlert 
} from 'lucide-react'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import { notificationApi } from '../services/api'
import { useNavigate } from 'react-router-dom'

export default function NotificationsPage() {
  const navigate = useNavigate()
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    fetchNotifications()
  }, [])

  const fetchNotifications = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await notificationApi.getNotifications()
      setNotifications(data || [])
      const countRes = await notificationApi.getUnreadCount()
      setUnreadCount(countRes?.unreadCount || 0)
    } catch (err) {
      setError('Failed to load notifications.')
    } finally {
      setLoading(false)
    }
  }

  const handleMarkAsRead = async (id, e) => {
    e?.stopPropagation?.()
    try {
      await notificationApi.markAsRead(id)
      setNotifications(notifications.map(n => n.id === id ? { ...n, read: true } : n))
      setUnreadCount(Math.max(0, unreadCount - 1))
    } catch (err) {
      console.error(err)
    }
  }

  const handleMarkAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead()
      setNotifications(notifications.map(n => ({ ...n, read: true })))
      setUnreadCount(0)
    } catch (err) {
      console.error(err)
    }
  }

  const handleNotificationClick = (notif) => {
    if (!notif.read) {
      handleMarkAsRead(notif.id)
    }
    
    if (notif.type === 'APPLICATION_SUBMITTED' || notif.type === 'SHORTLISTED' || notif.type === 'INTERVIEW' || notif.type === 'SELECTED' || notif.type === 'REJECTED' || notif.type === 'APPLICATION_STATUS_CHANGED') {
      navigate('/applicant/applications')
    } else if (notif.type === 'JOB_RECOMMENDATION') {
      navigate('/applicant/matches')
    }
  }

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'APPLICATION_SUBMITTED':
        return <Send className="w-5 h-5 text-[#1a56db]" />
      case 'SHORTLISTED':
        return <Sparkles className="w-5 h-5 text-[#1a56db]" />
      case 'INTERVIEW':
        return <Calendar className="w-5 h-5 text-[#d97706]" />
      case 'SELECTED':
        return <CheckCircle2 className="w-5 h-5 text-[#059669]" />
      case 'REJECTED':
        return <ShieldAlert className="w-5 h-5 text-[#dc2626]" />
      case 'JOB_RECOMMENDATION':
        return <Sparkles className="w-5 h-5 text-[#d97706]" />
      default:
        return <Bell className="w-5 h-5 text-[#1a56db]" />
    }
  }

  return (
    <div className="cl-page">
      <Navbar />

      <main className="cl-main">
        <div className="cl-panel p-5 sm:p-6 space-y-6">
          {/* Header */}
          <div className="cl-page-header flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <div>
              <div className="flex items-center gap-2 text-xs font-semibold text-[#0E5C34] mb-1">
                <Bell className="w-4 h-4" /> Notification Center
              </div>
              <h1 className="cl-page-title">Your Notifications</h1>
              <p className="cl-page-subtitle">
                Stay updated on job applications, interview schedules, and recruiter decisions.
              </p>
            </div>

            {unreadCount > 0 && (
              <button
                onClick={handleMarkAllAsRead}
                className="cl-btn-secondary text-sm"
              >
                <Check className="w-4 h-4" /> Mark All as Read
              </button>
            )}
          </div>

          {/* Notifications */}
          {error && (
            <div className="cl-alert cl-alert-error" role="alert">
              <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          {loading ? (
            <div className="py-20 flex justify-center items-center">
              <div className="cl-spinner" />
            </div>
          ) : notifications.length > 0 ? (
            <div className="space-y-3">
              {notifications.map((notif) => (
                <div
                  key={notif.id}
                  onClick={() => handleNotificationClick(notif)}
                  className={`cl-card p-4 transition-all cursor-pointer flex items-start gap-4 ${
                    !notif.read
                      ? 'bg-[#0E5C34]/15 border-[#0E5C34]/30'
                      : 'hover:border-[#B9E3C6]'
                  }`}
                >
                  <div className="w-10 h-10 rounded-xl bg-[#0E5C34]/10 border border-[#0E5C34]/20 flex items-center justify-center text-[#0E5C34] shrink-0 mt-0.5">
                    {getNotificationIcon(notif.type)}
                  </div>

                  <div className="flex-1 space-y-1 min-w-0">
                    <div className="flex items-center justify-between gap-3">
                      <h4 className={`text-sm font-bold truncate ${!notif.read ? 'text-[#1B211D]' : 'text-[#3A4139]'}`}>
                        {notif.title}
                      </h4>
                      <div className="flex items-center gap-2 shrink-0">
                        <span className="text-[11px] text-[#8F9992] flex items-center gap-1">
                          <Clock className="w-3 h-3" /> {new Date(notif.createdAt).toLocaleDateString()}
                        </span>
                        {!notif.read && (
                          <span className="w-2 h-2 rounded-full bg-[#0E5C34]" title="Unread" />
                        )}
                      </div>
                    </div>

                    <p className="text-xs text-[#5B655E] leading-relaxed">{notif.message}</p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="cl-empty">
              <Bell className="cl-empty-icon" />
              <p className="cl-empty-title">No notifications yet</p>
              <p className="cl-empty-text">Status changes and application updates will appear here.</p>
            </div>
          )}
        </div>
      </main>

      <Footer />
    </div>
  )
}
