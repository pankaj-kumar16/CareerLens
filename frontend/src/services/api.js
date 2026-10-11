import axios from 'axios'

const rawBase = import.meta.env.VITE_API_BASE_URL || '/api'
const baseURL = rawBase.endsWith('/api') ? rawBase : `${rawBase.replace(/\/+$/, '')}/api`

const API = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

const getStoredToken = () => {
  return sessionStorage.getItem('careerlens_token') || localStorage.getItem('careerlens_token')
}

API.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, (error) => Promise.reject(error))

API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      sessionStorage.removeItem('careerlens_token')
      sessionStorage.removeItem('careerlens_user')
      localStorage.removeItem('careerlens_token')
      localStorage.removeItem('careerlens_user')
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  registerSendOtp: async (payload) => {
    const res = await API.post('/auth/register/send-otp', payload)
    return res.data
  },
  registerVerifyOtp: async (payload) => {
    const res = await API.post('/auth/register/verify-otp', payload)
    return res.data
  },
  loginSendOtp: async (payload) => {
    const res = await API.post('/auth/login/send-otp', payload)
    return res.data
  },
  loginVerifyOtp: async (payload) => {
    const res = await API.post('/auth/login/verify-otp', payload)
    return res.data
  },
  resendOtp: async (payload) => {
    const res = await API.post('/auth/resend-otp', payload)
    return res.data
  },
  getCurrentUser: async () => {
    const res = await API.get('/auth/me')
    return res.data
  },
}

export const companyApi = {
  list: async (page = 0, size = 10) => {
    const res = await API.get(`/companies?page=${page}&size=${size}`)
    return res.data
  },
  get: async (id) => {
    const res = await API.get(`/companies/${id}`)
    return res.data
  },
  create: async (data) => {
    const res = await API.post('/recruiter/company', data)
    return res.data
  },
  getMy: async () => {
    const res = await API.get('/recruiter/company')
    return res.data
  },
  updateMy: async (data) => {
    const res = await API.put('/recruiter/company', data)
    return res.data
  },
}

export const profileApi = {
  getJobSeekerProfile: async () => {
    const res = await API.get('/job-seeker/profile')
    return res.data
  },
  getCompleteness: async () => {
    const res = await API.get('/job-seeker/profile/completeness')
    return res.data
  },
  updateJobSeekerProfile: async (data) => {
    const res = await API.put('/job-seeker/profile', data)
    return res.data
  },
  getRecruiterProfile: async () => {
    const res = await API.get('/recruiter/profile')
    return res.data
  },
  updateRecruiterProfile: async (data) => {
    const res = await API.put('/recruiter/profile', data)
    return res.data
  },
}

export const resumeApi = {
  upload: async (formData) => {
    const res = await API.post('/job-seeker/resume', formData, {
      headers: { 'Content-Type': undefined },
    })
    return res.data
  },
  get: async () => {
    const res = await API.get('/job-seeker/resume')
    return res.data
  },
  download: async () => {
    const res = await API.get('/job-seeker/resume/download', { responseType: 'blob' })
    return res
  },
  delete: async () => {
    const res = await API.delete('/job-seeker/resume')
    return res.data
  },
  listVersions: async () => {
    const res = await API.get('/job-seeker/resumes')
    return res.data
  },
  uploadVersion: async (formData) => {
    const res = await API.post('/job-seeker/resumes', formData, {
      headers: { 'Content-Type': undefined },
    })
    return res.data
  },
  getVersionById: async (id) => {
    const res = await API.get(`/job-seeker/resumes/${id}`)
    return res.data
  },
  downloadVersion: async (id) => {
    const res = await API.get(`/job-seeker/resumes/${id}/download`, { responseType: 'blob' })
    return res
  },
  setDefault: async (id) => {
    const res = await API.put(`/job-seeker/resumes/${id}/default`)
    return res.data
  },
  deleteVersion: async (id) => {
    const res = await API.delete(`/job-seeker/resumes/${id}`)
    return res.data
  },
  updateVersion: async (id, formData) => {
    const res = await API.put(`/job-seeker/resumes/${id}`, formData, {
      headers: { 'Content-Type': undefined },
    })
    return res.data
  },
}

export const jobApi = {
  search: async (params = {}) => {
    const res = await API.get('/jobs', { params })
    return res.data
  },
  getById: async (id) => {
    const res = await API.get(`/jobs/${id}`)
    return res.data
  },
  apply: async (jobId, resumeId = null) => {
    const url = resumeId ? `/jobs/${jobId}/apply?resumeId=${resumeId}` : `/jobs/${jobId}/apply`
    const res = await API.post(url)
    return res.data
  },
}

export const jobSeekerApi = {
  getApplications: async () => {
    const res = await API.get('/job-seeker/applications')
    return res.data
  },
  getApplicationById: async (id) => {
    const res = await API.get(`/job-seeker/applications/${id}`)
    return res.data
  },
  saveJob: async (jobId) => {
    const res = await API.post(`/job-seeker/jobs/${jobId}/save`)
    return res.data
  },
  unsaveJob: async (jobId) => {
    const res = await API.delete(`/job-seeker/jobs/${jobId}/save`)
    return res.data
  },
  getSavedJobs: async () => {
    const res = await API.get('/job-seeker/saved-jobs')
    return res.data
  },
}

export const recruiterApi = {
  createJob: async (data) => {
    const res = await API.post('/recruiter/jobs', data)
    return res.data
  },
  getJobs: async (page = 0, size = 50) => {
    const res = await API.get(`/recruiter/jobs?page=${page}&size=${size}`)
    return res.data
  },
  getMyJobs: async (page = 0, size = 50) => {
    const res = await API.get(`/recruiter/jobs?page=${page}&size=${size}`)
    return res.data
  },
  getJobById: async (id) => {
    const res = await API.get(`/recruiter/jobs/${id}`)
    return res.data
  },
  updateJob: async (id, data) => {
    const res = await API.put(`/recruiter/jobs/${id}`, data)
    return res.data
  },
  deleteJob: async (id) => {
    const res = await API.delete(`/recruiter/jobs/${id}`)
    return res.data
  },
  getJobApplications: async (jobId) => {
    const res = await API.get(`/recruiter/jobs/${jobId}/applications`)
    return res.data
  },
  updateApplicationStatus: async (applicationId, statusData) => {
    const res = await API.put(`/recruiter/applications/${applicationId}/status`, statusData)
    return res.data
  },
  updateApplicationNotes: async (applicationId, notesData) => {
    const res = await API.put(`/recruiter/applications/${applicationId}/notes`, notesData)
    return res.data
  },
  getShortlisted: async () => {
    const res = await API.get('/recruiter/shortlisted')
    return res.data
  },
  downloadCandidateResume: async (applicationId) => {
    const res = await API.get(`/recruiter/applications/${applicationId}/resume/download`, { responseType: 'blob' })
    return res
  },
}

export const aiApi = {
  getResumeAnalysis: async () => {
    const res = await API.get('/job-seeker/resume/analysis')
    return res.data
  },
  getJobMatch: async (jobId) => {
    const res = await API.get(`/job-seeker/jobs/${jobId}/match`)
    return res.data
  },
  getRecommendedMatches: async () => {
    const res = await API.get('/job-seeker/matches')
    return res.data
  },
  getRecommendations: async (page = 0, size = 10) => {
    const res = await API.get(`/job-seeker/recommendations?page=${page}&size=${size}`)
    return res.data
  },
  getRankedCandidates: async (jobId) => {
    const res = await API.get(`/recruiter/jobs/${jobId}/candidates`)
    return res.data
  },
  getTopMatchesForRecruiter: async () => {
    const res = await API.get('/recruiter/matches')
    return res.data
  },
  getRecruiterAnalytics: async (days = 30) => {
    const res = await API.get(`/recruiter/analytics?days=${days}`)
    return res.data
  },
}

export const notificationApi = {
  getNotifications: async () => {
    const res = await API.get('/notifications')
    return res.data
  },
  getUnreadCount: async () => {
    const res = await API.get('/notifications/unread-count')
    return res.data
  },
  markAsRead: async (id) => {
    const res = await API.put(`/notifications/${id}/read`)
    return res.data
  },
  markAllAsRead: async () => {
    const res = await API.put('/notifications/read-all')
    return res.data
  },
}

export default API
