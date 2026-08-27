<template>
  <section>
    <div class="page-head">
      <div>
        <h1 class="page-title">Power Plant Assets</h1>
        <div class="page-desc">Installed capacity, critical assets and fleet composition across generation plants</div>
      </div>
    </div>

    <div class="filterbar">
      <div><label>Organisation</label>
        <select v-model="filters.organisation">
          <option value="">All owners</option>
          <option v-for="o in organisations" :key="o.id" :value="o.id">{{ o.shortName || o.name }}</option>
        </select>
      </div>
      <div><label>Plant type</label>
        <select v-model="filters.plantType">
          <option value="">All plant types</option>
          <option>Hydropower</option>
          <option>Thermal</option>
          <option>Solar</option>
          <option>Wind</option>
        </select>
      </div>
      <div><label>Region / Province</label>
        <select v-model="filters.region">
          <option value="">All locations</option>
          <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
        </select>
      </div>
      <div><label>Power plant</label>
        <select v-model="filters.plant">
          <option value="">All plants</option>
          <option v-for="s in substations" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>
      <div><label>Asset class</label>
        <select v-model="filters.assetClass">
          <option value="">All asset classes</option>
          <option value="transformer">Transformer</option>
          <option value="ct">Current Transformer</option>
          <option value="vt">Voltage Transformer</option>
          <option value="circuit-breaker">Circuit Breaker</option>
          <option value="disconnector">Disconnector</option>
          <option value="surge-arrester">Surge Arrester</option>
        </select>
      </div>
      <div class="filter-actions"><button class="btn" @click="fetchData">Apply</button></div>
    </div>

    <div class="cards">
      <div class="card">
        <div class="kpi-label">Power plants</div>
        <div class="kpi" style="color:#0e7490">{{ summary.totalPlants?.toLocaleString() || '—' }}</div>
        <div class="kpi-sub">Selected generation portfolio</div>
      </div>
      <div class="card">
        <div class="kpi-label">Installed capacity</div>
        <div class="kpi" style="color:#059669">{{ summary.totalCapacity || '—' }}</div>
        <div class="kpi-sub">Gross plant capacity</div>
      </div>
      <div class="card">
        <div class="kpi-label">Generating units</div>
        <div class="kpi" style="color:#0891b2">{{ summary.totalUnits?.toLocaleString() || '—' }}</div>
        <div class="kpi-sub">Operational and planned units</div>
      </div>
      <div class="card">
        <div class="kpi-label">Critical assets</div>
        <div class="kpi bad-text">{{ summary.criticalAssets?.toLocaleString() || '—' }}</div>
        <div class="kpi-sub">Criticality A / high consequence</div>
      </div>
      <div class="card">
        <div class="kpi-label">Overdue tests</div>
        <div class="kpi warn-text">{{ summary.overdueTests?.toLocaleString() || '—' }}</div>
        <div class="kpi-sub">Past inspection or testing due date</div>
      </div>
    </div>

    <div class="grid2">
      <div class="card">
        <div class="card-head"><h3>Transformer capacity by voltage level</h3><span class="small-link">MVA</span></div>
        <div class="bar-chart">
          <div class="bar-col" v-for="item in capacityByType" :key="item.label">
            <div class="bar-value">{{ item.value?.toLocaleString(undefined, {maximumFractionDigits: 1}) }} MVA</div>
            <div class="bar-track"><div class="bar" :style="{ height: Math.max(20, item.height * 1.8) + 'px' }"></div></div>
            <div class="x">{{ item.label }}</div>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-head"><h3>Asset health distribution</h3></div>
        <div class="donut-layout">
          <div class="donut" :style="{ background: healthDonutGradient }"></div>
          <div class="legend">
            <div v-for="(item, idx) in healthDistribution" :key="idx">
              <span class="dot" :style="{ background: ['#059669','#d97706','#dc2626'][idx] }"></span>{{ item.label }} — {{ item.value }}%
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="grid-equal">
      <div class="card">
        <div class="card-head"><h3>Critical assets by system</h3></div>
        <div class="list-row" v-for="item in criticalBySystem" :key="item.label">
          <span>{{ item.label }}</span><strong>{{ item.value }}</strong>
        </div>
      </div>
      <div class="card">
        <div class="card-head"><h3>Plant master-data completeness</h3></div>
        <template v-for="item in plantCompleteness" :key="item.label">
          <div class="list-row"><span>{{ item.label }}</span><strong>{{ item.value }}%</strong></div>
          <div class="progress"><span :style="{ width: item.value + '%' }"></span></div>
        </template>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../api'

const filters = ref({ organisation: '', plantType: '', region: '', plant: '', assetClass: '' })
const summary = ref({})
const organisations = ref([])
const cities = ref([])
const substations = ref([])

const capacityByType = ref([])
const healthDistribution = ref([])
const criticalBySystem = ref([])
const plantCompleteness = ref([])

const healthDonutGradient = computed(() => {
  const colors = ['#059669', '#d97706', '#dc2626']
  let acc = 0
  return 'conic-gradient(' + healthDistribution.value.map((d, i) => {
    const start = acc; acc += d.value
    return `${colors[i]} ${start}% ${acc}%`
  }).join(',') + ')'
})

async function fetchData() {
  try {
    const [sumRes, capRes, hlthRes, critRes, compRes] = await Promise.all([
      api.getPlantSummary(filters.value).catch(() => null),
      api.getCapacityByPlantType(filters.value).catch(() => null),
      api.getAssetHealthDistribution(filters.value).catch(() => null),
      api.getCriticalAssetsBySystem(filters.value).catch(() => null),
      api.getPlantCompleteness(filters.value).catch(() => null),
    ])
    if (sumRes?.data) summary.value = sumRes.data
    if (capRes?.data) capacityByType.value = capRes.data.filter(d => d.value > 0)
    if (hlthRes?.data) healthDistribution.value = hlthRes.data
    if (critRes?.data) criticalBySystem.value = critRes.data
    if (compRes?.data) plantCompleteness.value = compRes.data
  } catch (e) { console.warn('API not available, using mock data') }
}

onMounted(async () => {
  fetchData()
  try {
    const [orgRes, cityRes, subRes] = await Promise.all([
      api.getOrganisationsWithData().catch(() => null),
      api.getCities().catch(() => null),
      api.getSubstations().catch(() => null),
    ])
    if (orgRes?.data) organisations.value = orgRes.data
    if (cityRes?.data) cities.value = cityRes.data
    if (subRes?.data) substations.value = subRes.data
  } catch (e) {}
})
</script>
