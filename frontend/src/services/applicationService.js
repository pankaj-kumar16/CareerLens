import api from './api'

const applicationService = {
  getMyApplications: () => api.get('/jobseeker/applications'),
  getApplicationById: (id) => api.get(`/jobseeker/applications/${id}`),
  getJobApplicants: (jobId) => api.get(`/recruiter/jobs/${jobId}/applicants`),
  updateApplicationStatus: (appId, status) =>
    api.patch(`/recruiter/applications/${appId}/status`, { status }),
  shortlistCandidate: (appId) => api.post(`/recruiter/applications/${appId}/shortlist`),
}

export default applicationService
