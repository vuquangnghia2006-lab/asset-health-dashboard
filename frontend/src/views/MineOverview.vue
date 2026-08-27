<script setup>
import { ref, computed, onMounted } from 'vue'
import { mineOverview } from '../api'
import EChart from '../components/EChart.vue'
import { fmt, pct, PALETTE, barBase } from '../format'
import { exportCSV, exportExcel, exportPDF } from '../export'
import { registerExporter } from '../exportStore'

const report = ref(null)
const loading = ref(false)
const error = ref(null)

async function fetchData() {
  loading.value = true
  error.value = null
  try {
    report.value = await mineOverview()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const cards = computed(() => report.value?.cards || {})

const prodByMonthOption = computed(() => {
  const data = report.value?.productionByMonth || []
  return {
    ...barBase,
    yAxis: { ...barBase.yAxis, name: 'Tonnes' },
    xAxis: { ...barBase.xAxis, data: data.map(d => d.label), axisLabel: { fontSize: 9, rotate: 30 } },
    series: [{
      type: 'bar',
      data: data.map(d => d.value),
      itemStyle: { color: '#168aad', borderRadius: [4, 4, 0, 0] },
      barMaxWidth: 36,
    }],
  }
})

const prodByTypeOption = computed(() => {
  const data = report.value?.productionByType || []
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

const minesByStatusOption = computed(() => {
  const data = report.value?.minesByStatus || []
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

const prodMonthChart = ref(null)
const prodTypeChart = ref(null)
const statusChart = ref(null)

function buildPayload() {
  if (!report.value) return null
  const charts = [prodMonthChart.value?.getDataURL(), prodTypeChart.value?.getDataURL(), statusChart.value?.getDataURL()]
    .filter(Boolean)
    .map((d, i) => ({ title: ['Production by month', 'Production by material type', 'Mines by status'][i], ...d }))
  return {
    title: 'Mining Overview',
    charts,
    tables: [
      { name: 'Overview', headers: ['Metric', 'Value'], rows: [
        ['Total mines', cards.value.totalMines],
        ['Active mines', cards.value.activeMines],
        ['Total workers', cards.value.totalWorkers],
        ['Total equipment', cards.value.totalEquipment],
        ['Monthly production (tonnes)', cards.value.totalProductionTonnes],
        ['Safety incidents', cards.value.safetyIncidents],
      ]},
      { name: 'Production by month', headers: ['Month', 'Tonnes'], rows: report.value.productionByMonth.map(d => [d.label, d.value]) },
      { name: 'Production by material type', headers: ['Material', 'Tonnes'], rows: report.value.productionByType.map(d => [d.label, d.value]) },
      { name: 'Top mines', headers: ['Mine', 'Location', 'Type', 'Status', 'Workers', 'Production'], rows: report.value.topMines.map(d => [d.name, d.location, d.type, d.status, d.workers, d.productionTonnes]) },
    ],
  }
}

registerExporter('mineOverview', async (fmt, el) => {
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
        <h1 class="page-title">Mining Overview</h1>
        <div class="page-desc">Overview of all mining operations, production and workforce</div>
      </div>
    </div>

    <div v-if="loading" class="card">Loading...</div>
    <div v-else-if="error" class="card bad-text">Error: {{ error }}</div>
    <template v-else-if="report">
      <div class="cards" style="grid-template-columns:repeat(6,1fr)">
        <div class="card">
          <div class="kpi-label">Total mines</div>
          <div class="kpi">{{ fmt(cards.totalMines) }}</div>
          <div class="kpi-sub">All mining sites</div>
        </div>
        <div class="card">
          <div class="kpi-label">Active mines</div>
          <div class="kpi good">{{ fmt(cards.activeMines) }}</div>
          <div class="kpi-sub">Currently operating</div>
        </div>
        <div class="card">
          <div class="kpi-label">Total workers</div>
          <div class="kpi">{{ fmt(cards.totalWorkers) }}</div>
          <div class="kpi-sub">Across all active mines</div>
        </div>
        <div class="card">
          <div class="kpi-label">Equipment units</div>
          <div class="kpi">{{ fmt(cards.totalEquipment) }}</div>
          <div class="kpi-sub">Active machinery</div>
        </div>
        <div class="card">
          <div class="kpi-label">Monthly production</div>
          <div class="kpi">{{ fmt(cards.totalProductionTonnes) }} <span style="font-size:13px;color:var(--muted)">t</span></div>
          <div class="kpi-sub">Tonnes extracted this month</div>
        </div>
        <div class="card">
          <div class="kpi-label">Safety incidents</div>
          <div class="kpi bad-text">{{ fmt(cards.safetyIncidents) }}</div>
          <div class="kpi-sub">This month</div>
        </div>
      </div>

      <div class="grid3">
        <div class="card">
          <div class="card-head"><h3>Production by month</h3><span class="small-link">tonnes</span></div>
          <EChart ref="prodMonthChart" :option="prodByMonthOption" height="270px" />
        </div>
        <div class="card">
          <div class="card-head"><h3>Production by material</h3><span class="small-link">tonnes</span></div>
          <EChart ref="prodTypeChart" :option="prodByTypeOption" height="270px" />
        </div>
        <div class="card">
          <div class="card-head"><h3>Mines by status</h3><span class="small-link">count</span></div>
          <EChart ref="statusChart" :option="minesByStatusOption" height="270px" />
        </div>
      </div>

      <div class="card table-card">
        <div class="card-head"><h3>Top mines by production (30 days)</h3></div>
        <table>
          <thead>
            <tr><th>Mine</th><th>Location</th><th>Type</th><th>Status</th><th>Workers</th><th>Production (t)</th></tr>
          </thead>
          <tbody>
            <tr v-for="m in report.topMines" :key="m.id">
              <td>{{ m.name }}</td>
              <td>{{ m.location }}</td>
              <td>{{ m.type }}</td>
              <td><span :class="m.status === 'ACTIVE' ? 'pill green' : m.status === 'MAINTENANCE' ? 'pill amber' : 'pill red'">{{ m.status }}</span></td>
              <td>{{ fmt(m.workers) }}</td>
              <td>{{ fmt(m.productionTonnes) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>
