<script setup>
import { ref, computed, onMounted } from 'vue'
import { mineEquipment } from '../api'
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
    report.value = await mineEquipment()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const cards = computed(() => report.value || {})

const byTypeOption = computed(() => {
  const data = report.value?.equipmentByType || []
  return {
    ...barBase,
    yAxis: { ...barBase.yAxis, name: 'Units' },
    xAxis: { ...barBase.xAxis, data: data.map(d => d.label), axisLabel: { fontSize: 9, rotate: 15 } },
    series: [{
      type: 'bar',
      data: data.map(d => d.value),
      itemStyle: { color: '#29a36a', borderRadius: [4, 4, 0, 0] },
      barMaxWidth: 42,
    }],
  }
})

const utilOption = computed(() => {
  const data = report.value?.utilizationByType || []
  return {
    ...barBase,
    yAxis: { ...barBase.yAxis, name: '%', max: 100 },
    xAxis: { ...barBase.xAxis, data: data.map(d => d.label), axisLabel: { fontSize: 9, rotate: 15 } },
    series: [{
      type: 'bar',
      data: data.map(d => d.value),
      itemStyle: {
        color: p => p.value >= 80 ? '#29a36a' : p.value >= 50 ? '#e49b2f' : '#d95763',
        borderRadius: [4, 4, 0, 0],
      },
      barMaxWidth: 42,
    }],
  }
})

const typeChart = ref(null)
const utilChart = ref(null)

function buildPayload() {
  if (!report.value) return null
  const charts = [typeChart.value?.getDataURL(), utilChart.value?.getDataURL()]
    .filter(Boolean)
    .map((d, i) => ({ title: ['Equipment by type', 'Utilization by type'][i], ...d }))
  return {
    title: 'Mine Equipment',
    charts,
    tables: [
      { name: 'Summary', headers: ['Metric', 'Value'], rows: [
        ['Total equipment', report.value.totalEquipment],
        ['Operational', report.value.operationalCount],
        ['Under maintenance', report.value.maintenanceCount],
        ['Idle', report.value.idleCount],
      ]},
      { name: 'Equipment list', headers: ['Name', 'Type', 'Mine', 'Status', 'Utilization %', 'Operating hours', 'Last maintenance'], rows:
        report.value.equipmentList.map(e => [e.name, e.type, e.mineName, e.status, e.utilizationPct, e.operatingHours, e.lastMaintenance]) },
    ],
  }
}

registerExporter('mineEquipment', async (fmt, el) => {
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
        <h1 class="page-title">Mine Equipment</h1>
        <div class="page-desc">Equipment inventory, utilization and maintenance status</div>
      </div>
    </div>

    <div v-if="loading" class="card">Loading...</div>
    <div v-else-if="error" class="card bad-text">Error: {{ error }}</div>
    <template v-else-if="report">
      <div class="cards" style="grid-template-columns:repeat(4,1fr)">
        <div class="card">
          <div class="kpi-label">Total equipment</div>
          <div class="kpi">{{ fmt(cards.totalEquipment) }}</div>
          <div class="kpi-sub">All machinery</div>
        </div>
        <div class="card">
          <div class="kpi-label">Operational</div>
          <div class="kpi good">{{ fmt(cards.operationalCount) }}</div>
          <div class="kpi-sub">Ready for work</div>
        </div>
        <div class="card">
          <div class="kpi-label">Under maintenance</div>
          <div class="kpi warn-text">{{ fmt(cards.maintenanceCount) }}</div>
          <div class="kpi-sub">Being serviced</div>
        </div>
        <div class="card">
          <div class="kpi-label">Idle</div>
          <div class="kpi bad-text">{{ fmt(cards.idleCount) }}</div>
          <div class="kpi-sub">Not in use</div>
        </div>
      </div>

      <div class="grid-equal">
        <div class="card">
          <div class="card-head"><h3>Equipment by type</h3><span class="small-link">units</span></div>
          <EChart ref="typeChart" :option="byTypeOption" height="270px" />
        </div>
        <div class="card">
          <div class="card-head"><h3>Utilization by type</h3><span class="small-link">%</span></div>
          <EChart ref="utilChart" :option="utilOption" height="270px" />
        </div>
      </div>

      <div class="card table-card">
        <div class="card-head"><h3>Equipment inventory</h3></div>
        <div class="table-scroll">
          <table>
            <thead>
              <tr><th>Name</th><th>Type</th><th>Mine</th><th>Status</th><th>Utilization</th><th>Hours</th><th>Last maintenance</th></tr>
            </thead>
            <tbody>
              <tr v-for="e in report.equipmentList" :key="e.id">
                <td>{{ e.name }}</td>
                <td>{{ e.type }}</td>
                <td>{{ e.mineName }}</td>
                <td><span :class="e.status === 'OPERATIONAL' ? 'pill green' : e.status === 'MAINTENANCE' ? 'pill amber' : 'pill red'">{{ e.status }}</span></td>
                <td :class="e.utilizationPct >= 80 ? 'good' : e.utilizationPct >= 50 ? 'warn-text' : 'bad-text'">{{ pct(e.utilizationPct) }}</td>
                <td>{{ fmt(e.operatingHours) }}</td>
                <td>{{ e.lastMaintenance }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
