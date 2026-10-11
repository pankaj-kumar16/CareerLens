import api from './api'

const resumeService = {
  uploadResume: (file) => {
    const form = new FormData()
    form.append('file', file)
    return api.post('/jobseeker/resume/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  getResumeScore: () => api.get('/jobseeker/resume/score'),
  getResumeInfo: () => api.get('/jobseeker/resume'),
}

export default resumeService
