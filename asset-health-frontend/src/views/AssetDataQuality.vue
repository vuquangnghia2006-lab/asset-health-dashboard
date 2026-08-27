<template>
  <section>
    <div class="page-head">
      <div>
        <h1 class="page-title">Asset Data Quality</h1>
        <div class="page-desc">Completeness, key-field coverage and phase-balance consistency of asset master data</div>
      </div>
    </div>

    <div class="filterbar">
      <div><label>Organisation</label>
        <select v-model="filters.organisation">
          <option value="">All organisations</option>
          <option v-for="o in organisations" :key="o.id" :value="o.id">{{ o.shortName || o.name }}</option>
        </select>
      </div>
      <div><label>Manufacturer</label>
        <select v-model="filters.manufacturer">
          <option value="">All manufacturers</option>
          <option v-for="m in manufacturers" :key="m.id" :value="m.id">{{ m.name }}</option>
        </select>
      </div>
      <div>
        <label>Asset class</label>
        <select v-model="filters.assetClass" @change="setAssetTab(filters.assetClass)">
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
      <div><label>Quality status</label>
        <select v-model="filters.status">
          <option value="">All statuses</option>
          <option>Complete</option>
          <option>Incomplete</option>
          <option>Invalid</option>
        </select>
      </div>
      <div class="filter-actions"><button class="btn" @click="fetchData">Apply</button></div>
    </div>

    <div class="cards six">
      <div class="card"><div class="kpi-label">Overall completeness</div><div class="kpi">{{ summary.overallCompleteness || '78.4' }}%</div><div class="kpi-sub">Weighted master-data score</div></div>
      <div class="card"><div class="kpi-label">Assets with missing fields</div><div class="kpi bad-text">{{ (summary.missingFields || 8372).toLocaleString() }}</div><div class="kpi-sub">At least one key field missing</div></div>
      <div class="card"><div class="kpi-label">Missing nameplates</div><div class="kpi warn-text">{{ (summary.missingNameplates || 2641).toLocaleString() }}</div><div class="kpi-sub">No nameplate image uploaded</div></div>
      <div class="card"><div class="kpi-label">Phase-monitored assets</div><div class="kpi">{{ (summary.phaseMonitored || 9864).toLocaleString() }}</div><div class="kpi-sub">Single-phase asset records</div></div>
      <div class="card"><div class="kpi-label">Balanced groups</div><div class="kpi good">{{ summary.balancedGroups || '92.4' }}%</div><div class="kpi-sub">Equal counts for phases A, B and C</div></div>
      <div class="card"><div class="kpi-label">Phase mismatches</div><div class="kpi bad-text">{{ summary.phaseMismatches || 137 }}</div><div class="kpi-sub">Groups requiring data review</div></div>
    </div>

    <div class="grid2">
      <div class="card">
        <div class="card-head"><h3>Completeness by data category</h3><span class="small-link">View details</span></div>
        <div class="bar-chart">
          <div class="bar-col" v-for="item in completenessData" :key="item.label">
            <div class="bar-value">{{ item.value }}%</div>
            <div class="bar" :class="item.value >= 80 ? 'alt' : 'amber'" :style="{ height: (item.value * 1.65) + 'px' }"></div>
            <div class="x" v-html="item.label"></div>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-head"><h3>Quality status distribution</h3></div>
        <div class="donut-layout">
          <div class="donut-wrap">
            <svg class="donut-svg" viewBox="0 0 200 200" @mouseleave="hoveredStatus = null">
              <g v-for="(seg, idx) in statusDonutSegments" :key="idx">
                <path
                  :d="seg.path"
                  :fill="statusDonutColors[idx]"
                  :opacity="selectedStatus === null ? (hoveredStatus === idx ? 0.8 : 1) : (selectedStatus === idx ? 1 : 0.2)"
                  class="donut-segment"
                  @mouseenter="hoveredStatus = idx"
                  @click="toggleStatusFilter(statusDistribution[idx]?.label)"
                />
              </g>
              <circle cx="100" cy="100" r="55" fill="#fff" />
              <text x="100" y="92" text-anchor="middle" class="donut-center-val">{{ statusDistribution[selectedStatus ?? 0]?.value ?? '' }}%</text>
              <text x="100" y="110" text-anchor="middle" class="donut-center-label">{{ statusDistribution[selectedStatus ?? 0]?.label ?? 'All' }}</text>
            </svg>
          </div>
          <div class="legend">
            <div
              v-for="(item, idx) in statusDistribution" :key="idx"
              class="legend-item"
              :class="{ active: selectedStatus === idx, dimmed: selectedStatus !== null && selectedStatus !== idx }"
              @mouseenter="hoveredStatus = idx"
              @mouseleave="hoveredStatus = null"
              @click="toggleStatusFilter(item.label)"
            >
              <span class="dot" :style="{ background: statusDonutColors[idx] }"></span>
              <span class="legend-label">{{ item.label }}</span>
              <span class="legend-pct">{{ item.value }}%</span>
            </div>
            <div v-if="selectedStatus !== null" class="legend-clear" @click="clearStatusFilter">
              ✕ Clear filter
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card" style="margin-bottom:16px">
      <div class="card-head"><h3>Asset count by phase</h3><span class="small-link">Open mismatched groups</span></div>
      <div class="phase-grid">
        <div class="phase-head">Asset class</div><div class="phase-head">Phase A</div><div class="phase-head">Phase B</div><div class="phase-head">Phase C</div><div class="phase-head">Status</div>
        <template v-for="row in phaseData" :key="row.assetClass">
          <div style="font-weight:600;color:#0f172a">{{ row.assetClass }}</div>
          <div class="phase-a">{{ row.phaseA?.toLocaleString() }}</div>
          <div class="phase-b">{{ row.phaseB?.toLocaleString() }}</div>
          <div class="phase-c">{{ row.phaseC?.toLocaleString() }}</div>
          <div><span class="pill" :class="row.phaseClass">{{ row.statusText }}</span></div>
        </template>
      </div>
    </div>

    <div class="card">
      <div class="card-head"><h3>Missing key fields</h3><span class="small-link">Open filtered assets</span></div>
      <div class="asset-tabs">
        <button v-for="tab in assetTabs" :key="tab.value" class="asset-tab" :class="{ active: filters.assetClass === tab.value }" @click="setAssetTab(tab.value)">{{ tab.label }}</button>
      </div>
      <div class="table-note">Each asset-type tab includes the common key fields plus its asset-specific key fields.</div>
      <table>
        <thead><tr><th>Field</th><th>Category</th><th>Missing count</th><th>Missing percent</th></tr></thead>
        <tbody>
          <tr v-for="(row, idx) in missingFields" :key="idx">
            <td style="font-weight:600;color:#0f172a">{{ row.field }}</td>
            <td>{{ row.category }}</td>
            <td style="font-weight:700">{{ row.count }}</td>
            <td><span class="pill" :class="parseFloat(row.percent) > 25 ? 'red' : parseFloat(row.percent) > 10 ? 'amber' : 'green'">{{ row.percent }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../api'

const filters = ref({ organisation: '', manufacturer: '', assetClass: 'all', voltage: '', status: '' })
const summary = ref({})
const organisations = ref([])
const manufacturers = ref([])

const completenessData = ref([
  { label: 'Properties', value: 88 },
  { label: 'Nameplate', value: 72 },
  { label: 'Basic<br>Specification', value: 69 },
  { label: 'Ratings', value: 81 },
  { label: 'Others', value: 63 },
])

const statusDistribution = ref([
  { label: 'Complete', value: 61 },
  { label: 'Incomplete', value: 27 },
  { label: 'Invalid', value: 12 },
])

const statusDonutColors = ['#35a66f', '#e5a13a', '#d85763']
const selectedStatus = ref(null)
const hoveredStatus = ref(null)

const statusDonutSegments = computed(() => {
  const cx = 100, cy = 100, r = 78, inner = 55
  const data = statusDistribution.value
  if (!data.length) return []
  let acc = 0
  return data.map((d) => {
    const start = acc
    acc += d.value
    const startAngle = (start / 100) * 2 * Math.PI - Math.PI / 2
    const endAngle = (acc / 100) * 2 * Math.PI - Math.PI / 2
    const largeArc = (acc - start) > 50 ? 1 : 0
    const x1 = cx + r * Math.cos(startAngle)
    const y1 = cy + r * Math.sin(startAngle)
    const x2 = cx + r * Math.cos(endAngle)
    const y2 = cy + r * Math.sin(endAngle)
    const ix1 = cx + inner * Math.cos(startAngle)
    const iy1 = cy + inner * Math.sin(startAngle)
    const ix2 = cx + inner * Math.cos(endAngle)
    const iy2 = cy + inner * Math.sin(endAngle)
    const path = `M ${x1} ${y1} A ${r} ${r} 0 ${largeArc} 1 ${x2} ${y2} L ${ix2} ${iy2} A ${inner} ${inner} 0 ${largeArc} 0 ${ix1} ${iy1} Z`
    return { path, pct: d.value }
  })
})

function toggleStatusFilter(label) {
  const idx = statusDistribution.value.findIndex(d => d.label === label)
  if (idx < 0) return
  if (selectedStatus.value === idx) {
    selectedStatus.value = null
    filters.value.status = ''
  } else {
    selectedStatus.value = idx
    filters.value.status = label
  }
}

function clearStatusFilter() {
  selectedStatus.value = null
  filters.value.status = ''
}

const phaseData = ref([
  { assetClass: 'Current Transformer', phaseA: 1206, phaseB: 1206, phaseC: 1204, phaseClass: 'amber', statusText: '−2 C' },
  { assetClass: 'Voltage Transformer', phaseA: 874, phaseB: 874, phaseC: 874, phaseClass: 'green', statusText: 'Balanced' },
  { assetClass: 'Surge Arrester', phaseA: 1113, phaseB: 1111, phaseC: 1108, phaseClass: 'red', statusText: 'Mismatch' },
  { assetClass: 'Bushing', phaseA: 642, phaseB: 642, phaseC: 642, phaseClass: 'green', statusText: 'Balanced' },
])

const assetTabs = [
  { label: 'All assets', value: 'all' },
  { label: 'Transformers', value: 'transformer' },
  { label: 'Circuit Breaker', value: 'circuit-breaker' },
  { label: 'CT', value: 'ct' },
  { label: 'VT', value: 'vt' },
  { label: 'Disconnector', value: 'disconnector' },
]

const commonKeyFields = [
  { field: 'Serial No.', category: 'Properties', count: '8,280', percent: '55.4%' },
  { field: 'Asset ID', category: 'Properties', count: '1,164', percent: '7.8%' },
  { field: 'Operating date', category: 'Properties', count: '3,220', percent: '21.5%' },
  { field: 'Nameplate', category: 'Properties', count: '2,641', percent: '17.7%' },
]

const assetSpecificFields = {
  all: [],
  transformer: [
    { field: 'Vector Group', category: 'Basic Specs/Transformers', count: '1,124', percent: '17.5%' },
    { field: 'Voltage Ratio', category: 'Basic Specs/Transformers', count: '986', percent: '15.4%' },
    { field: 'Rated Power', category: 'Basic Specs/Transformers', count: '742', percent: '11.6%' },
    { field: 'Uk%', category: 'Basic Specs/Transformers', count: '1,486', percent: '23.1%' },
    { field: 'Number of Taps', category: 'Basic Specs/Transformers', count: '1,735', percent: '27.0%' },
    { field: 'Tap voltage', category: 'Basic Specs/Transformers', count: '1,944', percent: '30.3%' },
  ],
  'circuit-breaker': [
    { field: 'Rated voltage L-L', category: 'Basic Specs/Circuit Breaker', count: '612', percent: '14.8%' },
    { field: 'Rated current', category: 'Basic Specs/Circuit Breaker', count: '488', percent: '11.8%' },
    { field: 'Rated short-circuit breaking current', category: 'Basic Specs/Circuit Breaker', count: '975', percent: '23.6%' },
    { field: 'Interrupting duty cycle', category: 'Basic Specs/Circuit Breaker', count: '1,428', percent: '34.5%' },
  ],
  ct: [
    { field: 'Primary windings', category: 'Basic Specs/CT', count: '1,264', percent: '19.7%' },
    { field: 'Standard', category: 'Basic Specs/CT', count: '2,018', percent: '31.5%' },
    { field: 'Tap configuration', category: 'Basic Specs/CT', count: '1,547', percent: '24.1%' },
  ],
  vt: [
    { field: 'Standard', category: 'Basic Specs/VT', count: '1,136', percent: '22.8%' },
    { field: 'Upr', category: 'Basic Specs/VT', count: '1,874', percent: '37.6%' },
    { field: 'Windings configuration', category: 'Basic Specs/VT', count: '982', percent: '19.7%' },
  ],
  disconnector: [
    { field: 'Rated voltage', category: 'Basic Specs/Disconnector', count: '746', percent: '12.9%' },
    { field: 'Rated current', category: 'Basic Specs/Disconnector', count: '1,089', percent: '18.9%' },
  ],
}

const missingFields = ref([...commonKeyFields])

async function setAssetTab(value) {
  filters.value.assetClass = value
  const assetType = value === 'all' ? '' : value
  try {
    const res = await api.getMissingKeyFields({ assetType })
    if (res?.data) missingFields.value = [...commonKeyFields, ...res.data]
    else missingFields.value = [...commonKeyFields, ...assetSpecificFields[value]]
  } catch {
    missingFields.value = [...commonKeyFields, ...assetSpecificFields[value]]
  }
}

async function fetchData() {
  const qualityParams = {
    organisation: filters.value.organisation || undefined,
    manufacturer: filters.value.manufacturer || undefined,
    voltage: filters.value.voltage || undefined,
  }
  try {
    const assetType = filters.value.assetClass === 'all' ? '' : filters.value.assetClass
    const [sumRes, compRes, distRes, phaseRes, fieldsRes] = await Promise.all([
      api.getDataQualitySummary(qualityParams).catch(() => null),
      api.getCompletenessByCategory(qualityParams).catch(() => null),
      api.getQualityStatusDistribution(qualityParams).catch(() => null),
      api.getAssetCountByPhase().catch(() => null),
      api.getMissingKeyFields({ assetType }).catch(() => null),
    ])
    if (sumRes?.data) summary.value = sumRes.data
    if (compRes?.data) completenessData.value = compRes.data
    if (distRes?.data) statusDistribution.value = distRes.data
    if (phaseRes?.data) phaseData.value = phaseRes.data
    if (fieldsRes?.data) missingFields.value = [...commonKeyFields, ...fieldsRes.data]
  } catch (e) { console.warn('API not available, using mock data') }
}

onMounted(async () => {
  fetchData()
  try {
    const [orgRes, mfgRes] = await Promise.all([
      api.getOrganisationsWithData().catch(() => null),
      api.getManufacturers().catch(() => null),
    ])
    if (orgRes?.data) organisations.value = orgRes.data
    if (mfgRes?.data) manufacturers.value = mfgRes.data
  } catch (e) {}
})
</script>

<style scoped>
.donut-wrap { position: relative; flex-shrink: 0; }
.donut-svg { width: 180px; height: 180px; cursor: pointer; }
.donut-segment { transition: opacity .2s; cursor: pointer; }
.donut-center-val { font-size: 22px; font-weight: 800; fill: #1f2937; }
.donut-center-label { font-size: 11px; fill: #6b7280; }
.legend-item {
  display: flex; align-items: center; gap: 8px; padding: 5px 8px;
  border-radius: 6px; cursor: pointer; transition: all .15s;
}
.legend-item:hover { background: #f0f4f8; }
.legend-item.active { background: #e8f6fa; }
.legend-item.dimmed { opacity: 0.4; }
.legend-label { flex: 1; color: #4b5563; font-weight: 500; font-size: 11px; }
.legend-pct { font-weight: 700; color: #1f2937; font-size: 11px; min-width: 36px; text-align: right; }
.legend-clear {
  font-size: 10px; color: #d95763; cursor: pointer; padding: 4px 8px;
  font-weight: 600; margin-top: 4px;
}
.legend-clear:hover { text-decoration: underline; }
</style>
