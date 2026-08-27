import { createRouter, createWebHistory } from 'vue-router'
import TransformerFleet from '../views/TransformerFleet.vue'
import AssetDataQuality from '../views/AssetDataQuality.vue'
import PowerPlantAssets from '../views/PowerPlantAssets.vue'

const routes = [
  { path: '/', redirect: '/transformer' },
  { path: '/transformer', name: 'Transformer Fleet', component: TransformerFleet },
  { path: '/quality', name: 'Asset Data Quality', component: AssetDataQuality },
  { path: '/plant', name: 'Power Plant Assets', component: PowerPlantAssets },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})
