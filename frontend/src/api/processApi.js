import axios from 'axios'

const BASE_URL = 'http://localhost:8080/api'

export const getProcessDefinitions = () => axios.get(`${BASE_URL}/process-definitions`)
export const getProcessDefinition = (id) => axios.get(`${BASE_URL}/process-definitions/${id}`)
export const saveProcessDefinition = (data) => axios.post(`${BASE_URL}/process-definitions`, data)
export const deleteProcessDefinition = (id) => axios.delete(`${BASE_URL}/process-definitions/${id}`)

export const startProcessInstance = (processDefinitionId) => axios.post(`${BASE_URL}/process-instances`, { processDefinitionId })
export const getProcessInstance = (id) => axios.get(`${BASE_URL}/process-instances/${id}`)
export const getProcessInstances = () => axios.get(`${BASE_URL}/process-instances`)
