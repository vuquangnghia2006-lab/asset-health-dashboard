<template>
  <section>
    <div class="page-head">
      <div>
        <h1 class="page-title">Transformer Fleet</h1>
        <div class="page-desc">Fleet inventory, capacity, manufacturer profile and parallel-operation screening</div>
      </div>
      <button class="btn secondary" type="button" @click="exportPage">Export / Print</button>
    </div>

    <div class="filterbar fleet-filters">
      <div>
        <label>Organisation</label>
        <select v-model="filters.organisation" @change="onOrganisationChange">
          <option value="">All organisations</option>
          <option v-for="o in organisations" :key="o.id" :value="o.id">{{ o.shortName || o.name }}</option>
        </select>
      </div>
      <div>
        <label>Region / Province</label>
        <select v-model="filters.region" @change="onRegionChange">
          <option value="">All regions</option>
          <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
        </select>
      </div>
      <div>
        <label>Substation</label>
        <select v-model="filters.substation">
          <option value="">All substations</option>
          <option v-for="s in substations" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>
      <div>
        <label>Voltage level</label>
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
        <label>Manufacturer</label>
        <select v-model="filters.manufacturer">
          <option value="">All manufacturers</option>
          <option v-for="m in manufacturers" :key="m.id" :value="m.id">{{ m.name }}</option>
        </select>
      </div>
      <div>
        <label>Status</label>
        <select v-model="filters.status">
          <option value="">All statuses</option>
          <option value="2">In Operation</option>
          <option value="0">Out of Operation</option>
        </select>
      </div>
      <div class="filter-actions">
        <button class="btn" type="button" @click="fetchData">Apply</button>
      </div>
    </div>

    <div class="cards fleet-cards">
      <div class="card">
        <div class="kpi-label">Total transformers</div>
        <div class="kpi">{{ formatInteger(summary.totalTransformers) }}</div>
        <div class="kpi-sub">Across selected fleet</div>
      </div>
      <div class="card">
        <div class="kpi-label">Total rated capacity</div>
        <div class="kpi">{{ formatMva(summary.totalCapacityMVA) }}</div>
        <div class="kpi-sub">Based on nameplate rated power</div>
      </div>
      <div class="card">
        <div class="kpi-label">Nameplate</div>
        <div class="kpi">{{ formatPercent(summary.nameplatePct) }}</div>
        <div class="kpi-sub">Transformers with uploaded nameplate</div>
      </div>
      <div class="card">
        <div class="kpi-label">Average fleet age</div>
        <div class="kpi">{{ formatAge(summary.averageFleetAge) }}</div>
        <div class="kpi-sub">From commissioning year</div>
      </div>
    </div>

    <div class="grid2">
      <div class="card">
        <h3>Transformer capacity by voltage level</h3>
        <div class="bar-chart" v-if="capacityData.length">
          <div class="bar-col" v-for="item in capacityData" :key="item.label">
            <div class="bar-value">{{ formatMvaValue(item.capacityMVA) }}</div>
            <div class="bar" :style="{ height: Math.max(2, item.heightPct || 0) + '%' }"></div>
            <div class="x">{{ item.label }}</div>
          </div>
        </div>
        <div v-else class="kpi-sub">No data available</div>
      </div>
      <div class="card">
        <h3>Fleet mix by manufacturer</h3>
        <div class="donut-layout" v-if="topMfrs.length">
          <div class="donut" :style="{ background: donutBackground }"></div>
          <div class="legend">
            <div v-for="(item, idx) in topMfrs" :key="item.label">
              <span class="dot" :style="{ background: palette[idx % palette.length] }"></span>
              {{ item.label }} — {{ item.pct }}%
            </div>
          </div>
        </div>
        <div v-else class="kpi-sub">No data available</div>
      </div>
    </div>

    <div class="compat">
      <div class="compat-head">
        <h3>Parallel operation compatibility</h3>
        <button class="btn" type="button" @click="recalculate">Recalculate</button>
      </div>
      <div class="compat-body">
        <div class="criteria">
          <label class="criterion">
            <input type="checkbox" v-model="compat.requireRatio">
            <strong>Transformation ratio</strong>
            <p>Require matching transformation ratio.</p>
          </label>
          <label class="criterion">
            <input type="checkbox" v-model="compat.requireVectorGroup">
            <strong>Vector group</strong>
            <p>Require identical vector group.</p>
          </label>
          <label class="criterion">
            <input type="checkbox" v-model="compat.requireTapConfiguration">
            <strong>Tap configuration</strong>
            <p>Require compatible tap range and configuration.</p>
          </label>
          <label class="criterion">
            <input type="checkbox" v-model="compat.requireImpedance">
            <strong>Impedance Uk%</strong>
            <p>Compare relative Uk% difference against the selected threshold.</p>
          </label>
        </div>
        <div class="compat-controls">
          <div>
            <label>Maximum Uk% difference</label>
            <input type="number" v-model.number="compat.ukLimit" min="0" max="100" step="0.1">
          </div>
          <div class="checks">
            <label><input type="checkbox" v-model="compat.sameSiteOnly"> Same substation only</label>
          </div>
          <div></div>
        </div>
      </div>
    </div>

    <div class="card">
      <h3>Compatibility results</h3>
      <div class="table-note" v-if="compatResults.length">Review means the pair is near the Uk% threshold or required data is missing.</div>
      <div class="compat-table-wrap" v-if="compatResults.length">
        <table>
          <thead>
            <tr>
              <th>Transformer A</th>
              <th>Transformer B</th>
              <th>Substation</th>
              <th>Ratio</th>
              <th>Vector group</th>
              <th>Tap configuration</th>
              <th>Uk% difference</th>
              <th>Result</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in compatResults" :key="idx">
              <td>{{ row.transformerA }}</td>
              <td>{{ row.transformerB }}</td>
              <td>{{ row.site }}</td>
              <td>{{ row.ratio }}</td>
              <td>{{ row.vectorGroup }}</td>
              <td>{{ row.tapRange }}</td>
              <td>{{ row.ukDiff == null ? '—' : row.ukDiff + '%' }}</td>
              <td><span class="pill" :class="resultClass(row.result)">{{ row.result }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="kpi-sub">No compatible pairs found for the selected criteria.</div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '../api'

const filters = ref({ organisation: '', region: '', substation: '', voltage: '', manufacturer: '', status: '' })
const summary = ref({})
const capacityData = ref([])
const topMfrs = ref([])
const organisations = ref([])
const manufacturers = ref([])
const cities = ref([])
const substations = ref([])
const compat = ref({
  requireRatio: true,
  requireVectorGroup: true,
  requireTapConfiguration: true,
  requireImpedance: true,
  sameSiteOnly: true,
  ukLimit: 10
})
const compatResults = ref([])
const palette = ['#1f9bb8', '#47b36a', '#e99c27', '#7676ba', '#c3cad1', '#e05c5c', '#8d6e63']

const donutBackground = computed(() => {
  let used = 0
  const stops = topMfrs.value.map((item, idx) => {
    const start = used
    used += Number(item.pct) || 0
    const color = palette[idx % palette.length]
    return `${color} ${start}% ${used}%`
  })
  return `conic-gradient(${stops.join(', ')})`
})

function formatInteger(value) {
  return value == null ? '—' : Number(value).toLocaleString()
}

function formatMva(value) {
  return value == null ? '—' : `${Number(value).toLocaleString(undefined, { maximumFractionDigits: 0 })} MVA`
}

function formatMvaValue(value) {
  return value == null ? '—' : Number(value).toLocaleString(undefined, { maximumFractionDigits: 0 })
}

function formatPercent(value) {
  return value == null ? '—' : `${Number(value).toLocaleString(undefined, { maximumFractionDigits: 1 })}%`
}

function formatAge(value) {
  return value == null ? '—' : `${Number(value).toLocaleString(undefined, { maximumFractionDigits: 1 })} yr`
}

function resultClass(result) {
  if (result === 'Compatible') return 'green'
  if (result === 'Review') return 'amber'
  if (result === 'Not compatible') return 'red'
  return 'blue'
}

function dashboardParams() {
  return {
    organisation: filters.value.organisation || undefined,
    city: filters.value.region || undefined,
    substation: filters.value.substation || undefined,
    voltage: filters.value.voltage || undefined,
    manufacturer: filters.value.manufacturer || undefined,
    status: filters.value.status || undefined
  }
}

function compatibilityParams() {
  return { ...dashboardParams(), ...compat.value }
}

async function loadSubstations() {
  try {
    const res = await api.getSubstationsWithTransformers({
      organisation: filters.value.organisation || undefined,
      city: filters.value.region || undefined
    })
    substations.value = res?.data || []
    if (filters.value.substation && !substations.value.some(s => String(s.id) === String(filters.value.substation))) {
      filters.value.substation = ''
    }
  } catch (e) {
    substations.value = []
  }
}

async function onOrganisationChange() {
  filters.value.substation = ''
  await loadSubstations()
}

async function onRegionChange() {
  filters.value.substation = ''
  await loadSubstations()
}

async function fetchData() {
  const params = dashboardParams()
  try {
    const [sumRes, capRes, mfrRes, compatRes] = await Promise.all([
      api.getTransformerFleetSummary(params).catch(() => null),
      api.getTransformerCapacityByVoltage(params).catch(() => null),
      api.getTopManufacturers(params).catch(() => null),
      api.getCompatibilityResults(compatibilityParams()).catch(() => null)
    ])
    if (sumRes?.data) summary.value = sumRes.data
    if (capRes?.data) capacityData.value = capRes.data.filter(i => (i.heightPct || 0) >= 0)
    if (mfrRes?.data) topMfrs.value = mfrRes.data
    if (compatRes?.data) compatResults.value = compatRes.data
  } catch (e) {
    console.warn('API not available')
  }
}

async function recalculate() {
  try {
    const res = await api.getCompatibilityResults(compatibilityParams())
    compatResults.value = res?.data || []
  } catch (e) {
    compatResults.value = []
  }
}

function exportPage() {
  window.print()
}

onMounted(async () => {
  fetchData()
  try {
    const [orgRes, mfgRes, cityRes] = await Promise.all([
      api.getOrganisationsWithData().catch(() => null),
      api.getManufacturers().catch(() => null),
      api.getCities().catch(() => null)
    ])
    if (orgRes?.data) organisations.value = orgRes.data
    if (mfgRes?.data) manufacturers.value = mfgRes.data
    if (cityRes?.data) cities.value = cityRes.data
    await loadSubstations()
  } catch (e) {}
})
</script>

<style scoped>
.fleet-filters {
  grid-template-columns: repeat(6, minmax(120px, 1fr)) auto;
}
.fleet-cards {
  grid-template-columns: repeat(4, 1fr);
}
.criterion {
  cursor: pointer;
}
.criterion input {
  margin-right: 6px;
}
.criterion strong {
  display: inline;
}
.compat-table-wrap {
  overflow-x: auto;
}
@media (max-width: 1200px) {
  .fleet-filters {
    grid-template-columns: repeat(3, 1fr);
  }
  .fleet-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 768px) {
  .fleet-filters,
  .fleet-cards {
    grid-template-columns: 1fr;
  }
}
</style>
