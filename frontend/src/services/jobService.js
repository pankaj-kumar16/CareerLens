import api from './api'

const jobService = {
  getJobs: (params) => api.get('/jobs', { params }),
  getJobById: (id) => api.get(`/jobs/${id}`),
  getRecommendedJobs: () => api.get('/jobseeker/jobs/recommended'),
  getSavedJobs: () => api.get('/jobseeker/saved-jobs'),
  saveJob: (jobId) => api.post(`/jobseeker/saved-jobs/${jobId}`),
  unsaveJob: (jobId) => api.delete(`/jobseeker/saved-jobs/${jobId}`),
  applyToJob: (jobId) => api.post(`/jobseeker/jobs/${jobId}/apply`),
}

export default jobService
