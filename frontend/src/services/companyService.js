import api from './api'

const companyService = {
  getCompanies: (params) => api.get('/companies', { params }),
  getCompanyById: (id) => api.get(`/companies/${id}`),
  getCompanyJobs: (id) => api.get(`/companies/${id}/jobs`),
  getRecruiterCompany: () => api.get('/recruiter/company'),
  updateCompany: (data) => api.put('/recruiter/company', data),
}

export default companyService
