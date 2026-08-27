import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

export default {
  getTransformers(params) { return api.get('/transformers', { params }) },
  getTransformerFleetSummary(params) { return api.get('/transformers/summary', { params }) },
  getTransformerCapacityByVoltage(params) { return api.get('/transformers/capacity-by-voltage', { params }) },
  getMixByOrganisation(params) { return api.get('/transformers/mix-by-organisation', { params }) },
  getManufacturerMix(params) { return api.get('/transformers/manufacturer-mix', { params }) },
  getTopManufacturers(params) { return api.get('/transformers/top-manufacturers', { params }) },
  getDataCompleteness(params) { return api.get('/transformers/data-completeness', { params }) },
  getCompatibilityResults(params) { return api.get('/transformers/compatibility', { params }) },

  getDataQualitySummary(params) { return api.get('/quality/summary', { params }) },
  getCompletenessByCategory(params) { return api.get('/quality/completeness-by-category', { params }) },
  getQualityStatusDistribution(params) { return api.get('/quality/status-distribution', { params }) },
  getAssetCountByPhase(params) { return api.get('/quality/asset-count-by-phase', { params }) },
  getMissingKeyFields(params) { return api.get('/quality/missing-key-fields', { params }) },

  getPlantSummary(params) { return api.get('/plants/summary', { params }) },
  getCapacityByPlantType(params) { return api.get('/plants/capacity-by-type', { params }) },
  getAssetHealthDistribution(params) { return api.get('/plants/health-distribution', { params }) },
  getCriticalAssetsBySystem(params) { return api.get('/plants/critical-assets', { params }) },
  getPlantCompleteness(params) { return api.get('/plants/completeness', { params }) },

  getOrganisations() { return api.get('/organisations') },
  getOrganisationsWithData() { return api.get('/organisations/with-data') },
  getSubstations() { return api.get('/substations') },
  getManufacturers() { return api.get('/manufacturers') },
  getCities() { return api.get('/cities') },
  getWards(city) { return api.get('/wards', { params: city ? { city } : {} }) },
}
