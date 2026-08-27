<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { assetOverview, mineOverview } from '../api'
import { store, toParams } from '../store'
import FilterBar from '../components/FilterBar.vue'
import EChart from '../components/EChart.vue'
import { fmt, pct, PALETTE, barBase } from '../format'
import { exportCSV, exportExcel, exportPDF } from '../export'
import { registerExporter } from '../exportStore'

const powerReport = ref(null)
const mineReport = ref(null)
const loading = ref(false)
const error = ref(null)
const router = useRouter()

async function fetchData() {
  loading.value = true
  error.value = null
  try {
    const [p, m] = await Promise.all([
      assetOverview(toParams(store.filters)),
      mineOverview(),
    ])
    powerReport.value = p
    mineReport.value = m
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const pCards = computed(() => powerReport.value?.cards || {})
const mCards = computed(() => mineReport.value?.cards || {})

const capacityOption = computed(() => {
  const data = powerReport.value?.capacityByVoltage || []
  return {
    ...barBase,
    yAxis: { ...barBase.yAxis, name: 'MVA' },
    xAxis: { ...barBase.xAxis, data: data.map(d => d.label) },
    series: [{
      type: 'bar',
      data: data.map(d => d.value),
      itemStyle: { color: '#168aad', borderRadius: [4, 4, 0, 0] },
      barMaxWidth: 42,
    }],
  }
})

const prodByMonthOption = computed(() => {
  const data = mineReport.value?.productionByMonth || []
  return {
    ...barBase,
    yAxis: { ...barBase.yAxis, name: 'Tonnes' },
    xAxis: { ...barBase.xAxis, data: data.map(d => d.label), axisLabel: { fontSize: 9, rotate: 30 } },
    series: [{
      type: 'bar',
      data: data.map(d => d.value),
      itemStyle: { color: '#29a36a', borderRadius: [4, 4, 0, 0] },
      barMaxWidth: 36,
    }],
  }
})

const prodByTypeOption = computed(() => {
  const data = mineReport.value?.productionByType || []
  return {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: 6, top: 'middle', textStyle: { fontSize: 10 } },
    series: [{
      type: 'pie',
      radius: ['42%', '70%'],
      center: ['38%', '50%'],
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      data: data.map((d, i) => ({ name: d.label, value: d.value, itemStyle: { color: PALETTE[i % PALETTE.length] } })),
    }],
  }
})

const mixOption = computed(() => {
  const data = powerReport.value?.assetMixByOrg || []
  return {
    tooltip: { formatter: p => `${p.name}<br/>${fmt(p.value)} assets` },
    series: [{
      type: 'treemap',
      roam: false,
      nodeClick: false,
      breadcrumb: { show: false },
      label: { fontSize: 10, color: '#fff' },
      upperLabel: { show: false },
      itemStyle: { borderColor: '#fff', borderWidth: 2, gapWidth: 2 },
      levels: [{ itemStyle: { borderColor: '#fff', borderWidth: 2, gapWidth: 2 } }],
      data: data.map((d, i) => ({
        name: d.label, value: d.value, id: d.id,
        itemStyle: { color: PALETTE[i % PALETTE.length] },
      })),
    }],
  }
})

const minesByStatusOption = computed(() => {
  const data = mineReport.value?.minesByStatus || []
  return {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: 6, top: 'middle', textStyle: { fontSize: 10 } },
    series: [{
      type: 'pie',
      radius: ['42%', '70%'],
      center: ['38%', '50%'],
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      data: data.map((d, i) => ({ name: d.label, value: d.value, itemStyle: { color: PALETTE[i % PALETTE.length] } })),
    }],
  }
})

const capChart = ref(null)
const mixChart = ref(null)
const prodMonthChart = ref(null)
const prodTypeChart = ref(null)
const statusChart = ref(null)

function buildPayload() {
  if (!powerReport.value && !mineReport.value) return null
  const charts = [
    capChart.value?.getDataURL(),
    mixChart.value?.getDataURL(),
    prodMonthChart.value?.getDataURL(),
    prodTypeChart.value?.getDataURL(),
    statusChart.value?.getDataURL(),
  ].filter(Boolean).map((d, i) => ({
    title: ['Capacity by voltage', 'Asset mix by org', 'Mine production by month', 'Mine production by material', 'Mines by status'][i],
    ...d,
  }))

  const tables = []
  if (powerReport.value) {
    const pc = powerReport.value.cards
    tables.push({
      name: 'Electric Power Overview', headers: ['Metric', 'Value'], rows: [
        ['Total assets', pc.totalAssets], ['Substations', pc.totalSubstations],
        ['Transformers', pc.totalTransformers], ['Capacity (MVA)', pc.transformerCapacityMva],
      ],
    })
  }
  if (mineReport.value) {
    const mc = mineReport.value.cards
    tables.push({
      name: 'Mining Overview', headers: ['Metric', 'Value'], rows: [
        ['Total mines', mc.totalMines], ['Active mines', mc.activeMines],
        ['Workers', mc.totalWorkers], ['Equipment', mc.totalEquipment],
        ['Monthly production (t)', mc.totalProductionTonnes], ['Safety incidents', mc.safetyIncidents],
      ],
    })
  }
  return { title: 'Unified Dashboard', charts, tables }
}

registerExporter('dashboard', async (fmt, el) => {
  const p = buildPayload()
  if (!p) throw new Error('Data not loaded')
  if (fmt === 'csv') exportCSV(p)
  else if (fmt === 'xlsx') exportExcel(p)
  else await exportPDF(el, p)
})
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">Dashboard</h1>
        <div class="page-desc">Electric power assets & mining operations — unified view</div>
      </div>
    </div>

    <FilterBar />

    <div v-if="loading" class="card">Loading...</div>
    <div v-else-if="error" class="card bad-text">Error: {{ error }}</div>
    <template v-else>
      <div class="section-header">
        <span class="section-icon" style="background:linear-gradient(135deg,#168aad,#42b8cc)">EP</span>
        <span class="section-title">Electric Power Assets</span>
        <router-link to="/asset-overview" class="small-link">View details &rarr;</router-link>
      </div>

      <div class="cards" style="grid-template-columns:repeat(4,1fr)">
        <div class="card">
          <div class="kpi-label">Total assets</div>
          <div class="kpi">{{ fmt(pCards.totalAssets) }}</div>
          <div class="kpi-sub">All asset classes</div>
        </div>
        <div class="card">
          <div class="kpi-label">Substations</div>
          <div class="kpi">{{ fmt(pCards.totalSubstations) }}</div>
          <div class="kpi-sub">Owning assets</div>
        </div>
        <div class="card">
          <div class="kpi-label">Transformers</div>
          <div class="kpi">{{ fmt(pCards.totalTransformers) }}</div>
          <div class="kpi-sub">Power transformers</div>
        </div>
        <div class="card">
          <div class="kpi-label">Capacity</div>
          <div class="kpi">{{ fmt(pCards.transformerCapacityMva) }} <span style="font-size:13px;color:var(--muted)">MVA</span></div>
          <div class="kpi-sub">Total rated power</div>
        </div>
      </div>

      <div class="grid2">
        <div class="card">
          <div class="card-head"><h3>Transformer capacity by voltage</h3><span class="small-link">MVA</span></div>
          <EChart ref="capChart" :option="capacityOption" height="250px" />
        </div>
        <div class="card">
          <div class="card-head"><h3>Asset mix by organisation</h3><span class="small-link">count</span></div>
          <EChart ref="mixChart" :option="mixOption" height="250px" />
        </div>
      </div>

      <div class="section-header" style="margin-top:8px">
        <span class="section-icon" style="background:linear-gradient(135deg,#29a36a,#58b68a)">MN</span>
        <span class="section-title">Mining Operations</span>
        <router-link to="/mine/overview" class="small-link">View details &rarr;</router-link>
      </div>

      <div class="cards" style="grid-template-columns:repeat(6,1fr)">
        <div class="card">
          <div class="kpi-label">Total mines</div>
          <div class="kpi">{{ fmt(mCards.totalMines) }}</div>
          <div class="kpi-sub">All sites</div>
        </div>
        <div class="card">
          <div class="kpi-label">Active mines</div>
          <div class="kpi good">{{ fmt(mCards.activeMines) }}</div>
          <div class="kpi-sub">Operating</div>
        </div>
        <div class="card">
          <div class="kpi-label">Workers</div>
          <div class="kpi">{{ fmt(mCards.totalWorkers) }}</div>
          <div class="kpi-sub">Across all mines</div>
        </div>
        <div class="card">
          <div class="kpi-label">Equipment</div>
          <div class="kpi">{{ fmt(mCards.totalEquipment) }}</div>
          <div class="kpi-sub">Machinery units</div>
        </div>
        <div class="card">
          <div class="kpi-label">Monthly production</div>
          <div class="kpi">{{ fmt(mCards.totalProductionTonnes) }} <span style="font-size:13px;color:var(--muted)">t</span></div>
          <div class="kpi-sub">Tonnes extracted</div>
        </div>
        <div class="card">
          <div class="kpi-label">Safety incidents</div>
          <div class="kpi bad-text">{{ fmt(mCards.safetyIncidents) }}</div>
          <div class="kpi-sub">This month</div>
        </div>
      </div>

      <div class="grid3">
        <div class="card">
          <div class="card-head"><h3>Mine production by month</h3><span class="small-link">tonnes</span></div>
          <EChart ref="prodMonthChart" :option="prodByMonthOption" height="250px" />
        </div>
        <div class="card">
          <div class="card-head"><h3>Production by material</h3><span class="small-link">tonnes</span></div>
          <EChart ref="prodTypeChart" :option="prodByTypeOption" height="250px" />
        </div>
        <div class="card">
          <div class="card-head"><h3>Mines by status</h3><span class="small-link">count</span></div>
          <EChart ref="statusChart" :option="minesByStatusOption" height="250px" />
        </div>
      </div>

      <div class="grid-equal">
        <div class="card table-card">
          <div class="card-head"><h3>Top manufacturers (power)</h3></div>
          <table>
            <thead><tr><th>Manufacturer</th><th>Assets</th><th>Share</th></tr></thead>
            <tbody>
              <tr v-for="m in (powerReport?.topManufacturers || [])" :key="m.name">
                <td>{{ m.name }}</td><td>{{ fmt(m.count) }}</td><td>{{ pct(m.pct) }}</td>
              </tr>
              <tr v-if="!powerReport?.topManufacturers?.length">
                <td colspan="3" style="text-align:center;color:var(--muted)">No data</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="card table-card">
          <div class="card-head"><h3>Top mines by production</h3></div>
          <table>
            <thead><tr><th>Mine</th><th>Location</th><th>Status</th><th>Production (t)</th></tr></thead>
            <tbody>
              <tr v-for="m in (mineReport?.topMines || [])" :key="m.id">
                <td>{{ m.name }}</td>
                <td>{{ m.location }}</td>
                <td><span :class="m.status === 'ACTIVE' ? 'pill green' : m.status === 'MAINTENANCE' ? 'pill amber' : 'pill red'">{{ m.status }}</span></td>
                <td>{{ fmt(m.productionTonnes) }}</td>
              </tr>
              <tr v-if="!mineReport?.topMines?.length">
                <td colspan="4" style="text-align:center;color:var(--muted)">No data</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  margin-top: 4px;
}
.section-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 800;
  font-size: 11px;
  flex-shrink: 0;
}
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
}
</style>
