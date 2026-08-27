import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import AssetOverview from '../views/AssetOverview.vue'
import TransformerFleet from '../views/TransformerFleet.vue'
import AssetDataQuality from '../views/AssetDataQuality.vue'
import MineOverview from '../views/MineOverview.vue'
import MineEquipment from '../views/MineEquipment.vue'
import MineProduction from '../views/MineProduction.vue'
import MineSafety from '../views/MineSafety.vue'

const routes = [
  { path: '/', name: 'dashboard', component: Dashboard, meta: { title: 'Dashboard' } },
  { path: '/asset-overview', name: 'overview', component: AssetOverview, meta: { title: 'Asset Overview' } },
  { path: '/transformer-fleet', name: 'fleet', component: TransformerFleet, meta: { title: 'Transformer Fleet' } },
  { path: '/data-quality', name: 'quality', component: AssetDataQuality, meta: { title: 'Asset Data Quality' } },
  { path: '/mine/overview', name: 'mineOverview', component: MineOverview, meta: { title: 'Mining Overview' } },
  { path: '/mine/equipment', name: 'mineEquipment', component: MineEquipment, meta: { title: 'Mine Equipment' } },
  { path: '/mine/production', name: 'mineProduction', component: MineProduction, meta: { title: 'Mine Production' } },
  { path: '/mine/safety', name: 'mineSafety', component: MineSafety, meta: { title: 'Mine Safety' } },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})
