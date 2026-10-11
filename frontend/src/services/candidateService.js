import api from './api'

const candidateService = {
  getAllCandidates: (params) => api.get('/recruiter/candidates', { params }),
  getCandidateById: (id) => api.get(`/recruiter/candidates/${id}`),
  getShortlisted: () => api.get('/recruiter/shortlisted'),
  addNote: (candidateId, note) =>
    api.post(`/recruiter/candidates/${candidateId}/notes`, { note }),
}

export default candidateService
