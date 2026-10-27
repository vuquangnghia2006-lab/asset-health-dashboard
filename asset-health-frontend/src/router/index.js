import { createRouter, createWebHistory } from 'vue-router'
import TransformerFleet from '../views/TransformerFleet.vue'
import AssetDataQuality from '../views/AssetDataQuality.vue'
import AssetCapacityOverview from '../views/AssetCapacityOverview.vue'

const routes = [
  { path: '/', redirect: '/overview' },
  { path: '/overview', name: 'Asset & Capacity Overview', component: AssetCapacityOverview },
  { path: '/transformer', name: 'Transformer Fleet', component: TransformerFleet },
  { path: '/quality', name: 'Asset Data Quality', component: AssetDataQuality },
  { path: '/:pathMatch(.*)*', redirect: '/overview' },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})
