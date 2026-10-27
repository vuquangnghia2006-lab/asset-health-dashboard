<template>
  <section class="overview">
    <div class="ov-header">Asset &amp; Capacity Overview</div>

    <!-- FILTERS -->
    <div class="ov-filters">
      <select v-model="filters.organisation" @change="fetchData">
        <option value="">Organisation</option>
        <option v-for="o in organisations" :key="o.id" :value="o.id">{{ o.shortName || o.name }}</option>
      </select>
      <select v-model="filters.city" @change="onCityChange">
        <option value="">City/Province</option>
        <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
      </select>
      <select v-model="filters.ward" @change="fetchData">
        <option value="">Ward</option>
        <option v-for="w in wards" :key="w" :value="w">{{ w }}</option>
      </select>
      <select v-model="filters.voltage" @change="fetchData">
        <option value="">Voltage Level</option>
        <option value="500">500 kV</option>
        <option value="220">220 kV</option>
        <option value="110">110 kV</option>
        <option value="35">35 kV</option>
        <option value="22">22 kV</option>
      </select>
      <select v-model="filters.assetClass" @change="fetchData">
        <option value="all">Asset Class</option>
        <option value="transformer">Transformer</option>
        <option value="circuit-breaker">Circuit Breaker</option>
        <option value="ct">Current Transformer</option>
        <option value="vt">Voltage Transformer</option>
        <option value="disconnector">Disconnector</option>
      </select>
      <select v-model="filters.status" @change="fetchData">
        <option value="">Status</option>
        <option value="2">In Operation</option>
        <option value="0">Out of Operation</option>
      </select>
    </div>

    <!-- KPI CARDS -->
    <div class="ov-cards">
      <div class="ov-card">
        <div class="ov-card-title">Total Assets</div>
        <div class="ov-card-value">{{ formatInt(summary.totalAssets) }}</div>
      </div>
      <div class="ov-card">
        <div class="ov-card-title">Total Substations</div>
        <div class="ov-card-value">{{ formatInt(summary.totalSubstations) }}</div>
      </div>
      <div class="ov-card">
        <div class="ov-card-title">Total Transformers</div>
        <div class="ov-card-value">{{ formatInt(summary.totalTransformers) }}</div>
      </div>
      <div class="ov-card">
        <div class="ov-card-title">Transformer Capacity</div>
        <div class="ov-card-value">{{ formatMva(summary.totalCapacityMVA) }} <span class="ov-unit">MVA</span></div>
      </div>
    </div>

    <!-- CHARTS ROW -->
    <div class="ov-row">
      <div class="ov-panel">
        <div class="ov-panel-title">Transformer Capacity by Voltage Level</div>
        <div class="ov-barchart" v-if="capacityData.length">
          <div class="ov-yaxis">
            <div class="ov-ylabel">MVA</div>
            <div class="ov-ticks">
              <span v-for="t in yTicks" :key="t">{{ formatTick(t) }}</span>
            </div>
          </div>
          <div class="ov-plot">
            <div class="ov-grid">
              <div v-for="t in yTicks" :key="t"></div>
            </div>
            <div class="ov-bars">
              <div class="ov-barcol" v-for="item in capacityData" :key="item.label">
                <div class="ov-barval">{{ formatMvaVal(item.capacityMVA) }}</div>
                <div class="ov-bar" :style="{ height: barHeight(item), background: barColor(item.label) }"></div>
                <div class="ov-xlabel">{{ item.label }}</div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="ov-empty">No data available</div>
      </div>

      <div class="ov-panel">
        <div class="ov-panel-title">Asset Mix by Organisation</div>
        <div class="ov-treemap" v-if="orgMixData.length">
          <div
            v-for="(org, idx) in treemapBlocks" :key="org.name"
            class="ov-tm-block"
            :class="{ 'ov-tm-col': org.stacked }"
            :style="{ flexGrow: org.count, background: treemapColor(idx) }"
            @click="selectOrg(org, idx)"
          >
            <template v-if="!org.stacked">
              <div class="ov-tm-name">{{ org.name }}</div>
              <div class="ov-tm-pct">{{ org.pct }}%</div>
            </template>
            <template v-else>
              <div
                v-for="(sub, sidx) in org.items" :key="sub.name"
                class="ov-tm-sub"
                :style="{ flexGrow: sub.count, background: treemapColor(idx + sidx + 1) }"
                @click.stop="selectOrg(sub, idx + sidx + 1)"
              >
                <div class="ov-tm-name">{{ sub.name }}</div>
                <div class="ov-tm-pct">{{ sub.pct }}%</div>
              </div>
            </template>
          </div>
        </div>
        <div v-else class="ov-empty">No data available</div>

        <!-- DETAIL OVERLAY -->
        <div class="ov-overlay" v-if="selectedOrg" @click.self="selectedOrg = null">
          <div class="ov-detail">
            <div class="ov-detail-head">
              <h3>{{ selectedOrg.name }}</h3>
              <button class="ov-close" @click="selectedOrg = null">&times;</button>
            </div>
            <div class="ov-detail-stats">
              <div class="ov-stat">
                <div class="ov-stat-val">{{ selectedOrg.count?.toLocaleString() }}</div>
                <div class="ov-stat-label">Transformers</div>
              </div>
              <div class="ov-stat">
                <div class="ov-stat-val">{{ selectedOrg.pct }}%</div>
                <div class="ov-stat-label">Share</div>
              </div>
            </div>
            <div class="ov-detail-bar">
              <div class="ov-bar-label">Proportion of total fleet</div>
              <div class="ov-bar-track">
                <div class="ov-bar-fill" :style="{ width: selectedOrg.pct + '%' }"></div>
              </div>
              <div class="ov-bar-pct">{{ selectedOrg.pct }}%</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- TABLES ROW -->
    <div class="ov-row">
      <div class="ov-panel">
        <div class="ov-panel-title">Top Manufacturers</div>
        <table class="ov-table" v-if="topMfrs.length">
          <tr v-for="(m, idx) in topMfrs" :key="m.label">
            <td class="ov-rank">{{ idx + 1 }}</td>
            <td>{{ m.label }}</td>
            <td class="ov-pct">{{ m.value }}%</td>
          </tr>
        </table>
        <div v-else class="ov-empty">No data available</div>
      </div>
      <div class="ov-panel">
        <div class="ov-panel-title">Data Completeness</div>
        <table class="ov-table" v-if="completenessData.length">
          <tr v-for="d in completenessData" :key="d.fieldName">
            <td>{{ completenessLabel(d.fieldName) }}</td>
            <td class="ov-pct">{{ d.missingPct }}%</td>
          </tr>
        </table>
        <div v-else class="ov-empty">No data available</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../api'

const filters = ref({ organisation: '', city: '', ward: '', voltage: '', assetClass: 'all', status: '' })
const summary = ref({})
const capacityData = ref([])
const orgMixData = ref([])
const topMfrs = ref([])
const completenessData = ref([])
const organisations = ref([])
const cities = ref([])
const wards = ref([])
const selectedOrg = ref(null)

const treemapPalette = ['#2f6db3', '#4da663', '#f0a13b', '#7d80bd', '#1597a7', '#e05c5c']

function formatInt(v) {
  return v == null ? '—' : Number(v).toLocaleString()
}
function formatMva(v) {
  if (v == null) return '—'
  return Number(v).toLocaleString(undefined, { maximumFractionDigits: 0 })
}
function formatMvaVal(v) {
  if (v == null) return '—'
  return Number(v).toLocaleString(undefined, { maximumFractionDigits: 0 })
}
function formatTick(v) {
  return Number(v).toLocaleString()
}
function completenessLabel(fieldName) {
  const map = {
    'Manufacturer': 'Missing Manufacturer',
    'Manufacturing Year': 'Missing Year',
    'Serial Number': 'Missing Serial Number',
    'Country of Origin': 'Missing Country of Origin'
  }
  return map[fieldName] || fieldName
}
function barColor(label) {
  if (label === '500kV') return '#2f6db3'
  if (label === '220kV') return '#4da663'
  if (label === '110kV') return '#f0a13b'
  return '#7d80bd'
}
function treemapColor(idx) {
  return treemapPalette[idx % treemapPalette.length]
}
function selectOrg(org) {
  selectedOrg.value = org
}

// Y axis: nice ceiling of max bucket capacity
const yMax = computed(() => {
  const max = Math.max(0, ...capacityData.value.map(i => i.capacityMVA || 0))
  if (max <= 0) return 1000
  const mag = Math.pow(10, Math.floor(Math.log10(max)))
  const n = max / mag
  const nice = n <= 1 ? 1 : n <= 2 ? 2 : n <= 5 ? 5 : 10
  return nice * mag
})
const yTicks = computed(() => {
  const m = yMax.value
  return [m, m * 0.75, m * 0.5, m * 0.25, 0]
})
function barHeight(item) {
  if (!yMax.value) return '4px'
  const pct = (item.capacityMVA || 0) / yMax.value * 100
  return Math.max(2, pct) + '%'
}

// Treemap layout: first block | second block | stacked rest
const treemapBlocks = computed(() => {
  const items = orgMixData.value.filter(i => (i.pct || 0) > 0)
  if (items.length <= 2) return items.map(o => ({ ...o, stacked: false }))
  return [
    { ...items[0], stacked: false },
    { ...items[1], stacked: false },
    { name: '__stack__', stacked: true, count: items.slice(2).reduce((s, o) => s + (o.count || 0), 0), items: items.slice(2) }
  ]
})

async function onCityChange() {
  filters.value.ward = ''
  try {
    const res = await api.getWards(filters.value.city || undefined)
    wards.value = res?.data || []
  } catch (e) { wards.value = [] }
  fetchData()
}

async function fetchData() {
  const p = {
    organisation: filters.value.organisation || undefined,
    city: filters.value.city || undefined,
    ward: filters.value.ward || undefined,
    voltage: filters.value.voltage || undefined,
    assetClass: filters.value.assetClass || 'all',
    status: filters.value.status || undefined
  }
  selectedOrg.value = null
  try {
    const [sumRes, capRes, orgRes, mfrRes, dcRes] = await Promise.all([
      api.getOverviewSummary(p).catch(() => null),
      api.getOverviewCapacityByVoltage(p).catch(() => null),
      api.getOverviewMixByOrganisation(p).catch(() => null),
      api.getOverviewTopManufacturers(p).catch(() => null),
      api.getOverviewDataCompleteness(p).catch(() => null)
    ])
    if (sumRes?.data) summary.value = sumRes.data
    if (capRes?.data) capacityData.value = capRes.data
    if (orgRes?.data) orgMixData.value = orgRes.data.filter(i => (i.pct || 0) > 0)
    if (mfrRes?.data) topMfrs.value = mfrRes.data
    if (dcRes?.data) completenessData.value = dcRes.data
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
* { box-sizing: border-box; }
.overview { width: 100%; }

/* Header */
.ov-header {
  background: #163b70; color: #fff;
  padding: 14px 22px; font-size: 20px; font-weight: 700;
  border-radius: 6px 6px 0 0;
}

/* Filters */
.ov-filters {
  display: grid; grid-template-columns: repeat(6, 1fr);
  gap: 12px; padding: 14px; background: #fff;
  border: 1px solid #e3e9f0; border-top: none;
}
.ov-filters select {
  height: 36px; border: 1px solid #d0d9e6; border-radius: 5px;
  padding: 0 10px; color: #5b6b7f; background: #fff; font-size: 13px; width: 100%;
}

/* KPI cards */
.ov-cards {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px;
  margin: 14px 0;
}
.ov-card {
  background: #fff; border: 1px solid #e3e9f0; border-radius: 8px;
  padding: 16px 20px;
}
.ov-card-title { font-size: 13px; color: #5b6b7f; font-weight: 600; }
.ov-card-value { font-size: 38px; font-weight: 800; color: #163b70; margin-top: 6px; }
.ov-unit { font-size: 15px; font-weight: 600; color: #5b6b7f; }

/* Rows & panels */
.ov-row {
  display: grid; grid-template-columns: 1.15fr 1fr; gap: 14px;
  margin-bottom: 14px;
}
.ov-panel {
  background: #fff; border: 1px solid #e3e9f0; border-radius: 8px;
  padding: 16px 18px; min-width: 0;
}
.ov-panel-title { font-size: 14px; font-weight: 700; color: #1f2937; margin-bottom: 14px; }
.ov-empty { padding: 36px; text-align: center; color: #9ca3af; font-size: 13px; }

/* Bar chart with y-axis */
.ov-barchart { display: flex; height: 240px; }
.ov-yaxis { display: flex; flex-direction: column; width: 46px; flex-shrink: 0; }
.ov-ylabel { font-size: 11px; color: #6b7280; height: 18px; }
.ov-ticks {
  flex: 1; display: flex; flex-direction: column; justify-content: space-between;
  font-size: 11px; color: #6b7280; text-align: right; padding-right: 8px; padding-bottom: 22px;
}
.ov-ticks span { line-height: 1; transform: translateY(-50%); }
.ov-ticks span:last-child { transform: translateY(0); }
.ov-plot { flex: 1; position: relative; min-width: 0; }
.ov-grid {
  position: absolute; inset: 18px 0 22px 0;
  display: flex; flex-direction: column; justify-content: space-between;
}
.ov-grid div { border-top: 1px solid #eef1f5; }
.ov-bars {
  position: absolute; inset: 18px 0 22px 0;
  display: flex; align-items: flex-end; justify-content: space-around;
}
.ov-barcol {
  flex: 1; max-width: 110px; height: 100%;
  display: flex; flex-direction: column; align-items: center; justify-content: flex-end;
}
.ov-barval { font-size: 12px; font-weight: 700; color: #374151; margin-bottom: 4px; }
.ov-bar { width: 62%; min-height: 3px; border-radius: 3px 3px 0 0; }
.ov-xlabel { font-size: 12px; color: #4b5563; margin-top: 6px; height: 16px; }

/* Treemap */
.ov-treemap { display: flex; gap: 3px; height: 240px; }
.ov-tm-block {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: #fff; font-weight: 700; cursor: pointer; min-width: 0; padding: 8px;
  transition: filter .15s;
}
.ov-tm-block:hover { filter: brightness(1.12); }
.ov-tm-col { padding: 0; }
.ov-tm-sub {
  width: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 6px; min-height: 0; overflow: hidden;
}
.ov-tm-sub + .ov-tm-sub { margin-top: 3px; }
.ov-tm-name { font-size: 13px; text-align: center; line-height: 1.25; }
.ov-tm-pct { font-size: 13px; margin-top: 2px; }

/* Overlay detail */
.ov-overlay {
  position: fixed; inset: 0; background: rgba(15, 30, 50, .45); z-index: 100;
  display: flex; align-items: center; justify-content: center;
}
.ov-detail {
  background: #fff; border-radius: 12px; padding: 26px 30px;
  min-width: 380px; max-width: 480px; box-shadow: 0 8px 32px rgba(0,0,0,.2);
}
.ov-detail-head { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 18px; }
.ov-detail-head h3 { margin: 0; font-size: 18px; color: #163b70; }
.ov-close { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; line-height: 1; }
.ov-close:hover { color: #333; }
.ov-detail-stats { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 18px; }
.ov-stat { background: #f2f6fb; border-radius: 8px; padding: 14px; text-align: center; }
.ov-stat-val { font-size: 22px; font-weight: 800; color: #163b70; }
.ov-stat-label { font-size: 11px; color: #6b7280; margin-top: 4px; }
.ov-bar-label { font-size: 12px; color: #6b7280; margin-bottom: 6px; }
.ov-bar-track { height: 10px; background: #e8eef4; border-radius: 99px; overflow: hidden; }
.ov-bar-fill { height: 100%; background: linear-gradient(90deg, #2f6db3, #5ea3e8); border-radius: 99px; }
.ov-bar-pct { font-size: 12px; font-weight: 700; color: #163b70; text-align: right; margin-top: 4px; }

/* Tables */
.ov-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.ov-table td { padding: 9px 6px; border-bottom: 1px solid #eef1f5; color: #374151; }
.ov-table tr:last-child td { border-bottom: none; }
.ov-rank { color: #6b7280; width: 26px; }
.ov-pct { text-align: right; color: #2363bd; font-weight: 700; white-space: nowrap; }

@media (max-width: 1100px) {
  .ov-filters { grid-template-columns: repeat(3, 1fr); }
  .ov-cards { grid-template-columns: repeat(2, 1fr); }
  .ov-row { grid-template-columns: 1fr; }
}
</style>
