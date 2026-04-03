import request from '@/utils/request'

// Auth API
export const login = (data: any): Promise<any> => request.post('/auth/login', data)
export const register = (data: any): Promise<any> => request.post('/auth/register', data)
export const getUserInfo = (): Promise<any> => request.get('/auth/me')

// Template API
export const getTemplates = (): Promise<any> => request.get('/templates')
export const getTemplateById = (id: string | number): Promise<any> => request.get(`/templates/${id}`)
export const createTemplate = (data: any): Promise<any> => request.post('/templates', data)
export const updateTemplate = (id: string | number, data: any): Promise<any> => request.put(`/templates/${id}`, data)
export const deleteTemplate = (id: string | number): Promise<any> => request.delete(`/templates/${id}`)

// LLM API
export const getLlmOptions = (): Promise<any> => request.get('/llm-configs/options')
export const getDefaultLlm = (): Promise<any> => request.get('/llm-configs/default')

// Session API
export const getSessions = (): Promise<any> => request.get('/sessions')
export const getSessionById = (id: string | number): Promise<any> => request.get(`/sessions/${id}`)
export const createSession = (data: any): Promise<any> => request.post('/sessions', data)
export const deleteSession = (id: string | number): Promise<any> => request.delete(`/sessions/${id}`)
export const getSessionMessages = (id: string | number): Promise<any> => request.get(`/sessions/${id}/messages`)

// Lesson Record API
export const generateLesson = (data: any): Promise<any> => request.post('/lesson-records/generate', data)
export const getLessonRecordsBySession = (sessionId: string | number): Promise<any> => request.get(`/lesson-records/session/${sessionId}`)
export const updateLessonRecord = (id: string | number, data: any): Promise<any> => request.put(`/lesson-records/${id}`, data)
