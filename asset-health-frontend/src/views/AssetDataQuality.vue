<template>
  <section>
    <div class="page-head">
      <div>
        <h1 class="page-title">Asset Data Quality</h1>
        <div class="page-desc">Completeness, key-field coverage and phase-balance consistency of asset master data</div>
      </div>
    </div>

    <div class="filterbar quality-filters">
      <div><label>Organisation</label>
        <select v-model="filters.organisation">
          <option value="">All organisations</option>
          <option v-for="o in organisations" :key="o.id" :value="o.id">{{ o.shortName || o.name }}</option>
        </select>
      </div>
      <div><label>Region / Province</label>
        <select v-model="filters.region">
          <option value="">All locations</option>
          <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
        </select>
      </div>
      <div>
        <label>Asset class</label>
        <select v-model="filters.assetClass">
          <option value="all">All assets</option>
          <option value="transformer">Transformer</option>
          <option value="circuit-breaker">Circuit Breaker</option>
          <option value="ct">CT</option>
          <option value="vt">VT</option>
          <option value="disconnector">Disconnector</option>
        </select>
      </div>
      <div><label>Voltage level</label>
        <select v-model="filters.voltage">
          <option value="">All voltage levels</option>
          <option value="500">500 kV</option>
          <option value="220">220 kV</option>
          <option value="110">110 kV</option>
          <option value="35">35 kV</option>
          <option value="22">22 kV</option>
        </select>
      </div>
      <div>
        <label>Data category</label>
        <select v-model="filters.category">
          <option value="all">All categories</option>
          <option value="properties">Properties</option>
          <option value="nameplate">Nameplate</option>
          <option value="basic-specification">Basic Specification</option>
          <option value="ratings">Ratings</option>
          <option value="others">Others</option>
        </select>
      </div>
      <div><label>Quality status</label>
        <select v-model="filters.qualityStatus">
          <option value="">All statuses</option>
          <option value="complete">Complete</option>
          <option value="incomplete">Incomplete</option>
          <option value="invalid">Invalid</option>
        </select>
      </div>
      <div class="filter-actions"><button class="btn" @click="fetchData">Apply</button></div>
    </div>

    <div class="cards six">
      <div class="card"><div class="kpi-label">Overall completeness</div><div class="kpi">{{ formatPercent(summary.overallCompleteness) }}</div><div class="kpi-sub">Weighted master-data score</div></div>
      <div class="card"><div class="kpi-label">Assets with missing fields</div><div class="kpi bad-text">{{ formatInteger(summary.missingFields) }}</div><div class="kpi-sub">At least one key field missing</div></div>
      <div class="card"><div class="kpi-label">Missing nameplates</div><div class="kpi warn-text">{{ formatInteger(summary.missingNameplates) }}</div><div class="kpi-sub">No nameplate image uploaded</div></div>
      <div class="card"><div class="kpi-label">Phase-monitored assets</div><div class="kpi">{{ formatInteger(summary.phaseMonitored) }}</div><div class="kpi-sub">Single-phase asset records</div></div>
      <div class="card"><div class="kpi-label">Balanced groups</div><div class="kpi good">{{ formatPercent(summary.balancedGroups) }}</div><div class="kpi-sub">Equal counts for phases A, B and C</div></div>
      <div class="card"><div class="kpi-label">Phase mismatches</div><div class="kpi bad-text">{{ formatInteger(summary.phaseMismatches) }}</div><div class="kpi-sub">Groups requiring data review</div></div>
    </div>

    <div class="grid2">
      <div class="card">
        <h3>Completeness by data category</h3>
        <div class="bar-chart" v-if="completenessData.length">
          <div class="bar-col" v-for="item in completenessData" :key="item.label">
            <div class="bar-value">{{ item.value }}%</div>
            <div class="bar" :class="[item.value >= 80 ? 'alt' : 'amber', { dim: isDimmedCategory(item.label) }]" :style="{ height: (item.value / 100 * 165) + 'px' }"></div>
            <div class="x" v-html="categoryLabel(item.label)"></div>
          </div>
        </div>
        <div v-else class="kpi-sub">No data available</div>
      </div>
      <div class="card">
        <h3>Quality status distribution</h3>
        <div class="donut-layout" v-if="statusTotal > 0">
          <div class="donut" :style="{ background: donutBackground }"></div>
          <div class="legend">
            <div v-for="(item, idx) in statusDistribution" :key="item.label">
              <span class="dot" :style="{ background: statusDonutColors[idx % statusDonutColors.length] }"></span>{{ item.label }} — {{ item.value }}%
            </div>
          </div>
        </div>
        <div v-else class="kpi-sub">No data available</div>
      </div>
    </div>

    <div class="card" style="margin-bottom:13px">
      <div class="card-head"><h3>Asset count by phase</h3><span class="small-link">Open mismatched groups</span></div>
      <div class="phase-grid" v-if="phaseData.length">
        <div class="phase-head">Asset class</div><div class="phase-head">Phase A</div><div class="phase-head">Phase B</div><div class="phase-head">Phase C</div><div class="phase-head">Status</div>
        <template v-for="row in phaseData" :key="row.assetClass">
          <div style="font-weight:600;color:#0f172a">{{ row.assetClass }}</div>
          <div class="phase-a">{{ formatInteger(row.phaseA) }}</div>
          <div class="phase-b">{{ formatInteger(row.phaseB) }}</div>
          <div class="phase-c">{{ formatInteger(row.phaseC) }}</div>
          <div><span class="pill" :class="row.phaseClass">{{ row.statusText }}</span></div>
        </template>
      </div>
      <div v-else class="kpi-sub">No phase-monitored records for the selected filters.</div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../api'

const filters = ref({ organisation: '', region: '', assetClass: 'all', voltage: '', category: 'all', qualityStatus: '' })
const summary = ref({})
const organisations = ref([])
const cities = ref([])
const completenessData = ref([])
const statusDistribution = ref([])
const phaseData = ref([])
const statusDonutColors = ['#35a66f', '#e5a13a', '#d85763']

const statusTotal = computed(() => statusDistribution.value.reduce((sum, item) => sum + (Number(item.value) || 0), 0))
const donutBackground = computed(() => {
  const total = statusTotal.value || 1
  let used = 0
  const stops = statusDistribution.value.map((item, idx) => {
    const start = used
    used += (Number(item.value) || 0) / total * 100
    return `${statusDonutColors[idx % statusDonutColors.length]} ${start}% ${used}%`
  })
  return `conic-gradient(${stops.join(', ')})`
})

function formatInteger(value) {
  return value == null ? '—' : Number(value).toLocaleString()
}

function formatPercent(value) {
  return value == null ? '—' : `${Number(value).toLocaleString(undefined, { maximumFractionDigits: 1 })}%`
}

function categoryKey(label) {
  const value = String(label || '').toLowerCase()
  if (value === 'properties') return 'properties'
  if (value === 'nameplate') return 'nameplate'
  if (value === 'basic specification') return 'basic-specification'
  if (value === 'ratings') return 'ratings'
  return 'others'
}

function categoryLabel(label) {
  return label === 'Basic Specification' ? 'Basic<br>Specification' : label
}

function isDimmedCategory(label) {
  return filters.value.category !== 'all' && categoryKey(label) !== filters.value.category
}

function qualityParams() {
  return {
    organisation: filters.value.organisation || undefined,
    region: filters.value.region || undefined,
    assetClass: filters.value.assetClass,
    voltage: filters.value.voltage || undefined,
    category: filters.value.category,
    qualityStatus: filters.value.qualityStatus || undefined
  }
}

async function fetchData() {
  const params = qualityParams()
  try {
    const [sumRes, compRes, distRes, phaseRes] = await Promise.all([
      api.getDataQualitySummary(params).catch(() => null),
      api.getCompletenessByCategory(params).catch(() => null),
      api.getQualityStatusDistribution({
        organisation: params.organisation,
        region: params.region,
        assetClass: params.assetClass,
        voltage: params.voltage
      }).catch(() => null),
      api.getAssetCountByPhase({
        organisation: params.organisation,
        region: params.region,
        assetClass: params.assetClass,
        voltage: params.voltage,
        qualityStatus: params.qualityStatus
      }).catch(() => null)
    ])
    if (sumRes?.data) summary.value = sumRes.data
    if (compRes?.data) completenessData.value = compRes.data
    if (distRes?.data) statusDistribution.value = distRes.data
    if (phaseRes?.data) phaseData.value = phaseRes.data
  } catch (e) { console.warn('API not available') }
}

onMounted(async () => {
  fetchData()
  try {
    const [orgRes, cityRes] = await Promise.all([
      api.getOrganisationsWithData().catch(() => null),
      api.getCities().catch(() => null)
    ])
    if (orgRes?.data) organisations.value = orgRes.data
    if (cityRes?.data) cities.value = cityRes.data
  } catch (e) {}
})
</script>

<style scoped>
.quality-filters {
  grid-template-columns: repeat(6, minmax(130px, 1fr)) auto;
}
.bar.dim {
  opacity: .35;
}
@media (max-width: 1200px) {
  .quality-filters {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 768px) {
  .quality-filters {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
