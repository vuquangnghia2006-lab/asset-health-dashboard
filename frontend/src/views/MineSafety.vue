<script setup>
import { ref, computed, onMounted } from 'vue'
import { mineSafety } from '../api'
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
    report.value = await mineSafety()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const byMonthOption = computed(() => {
  const data = report.value?.incidentsByMonth || []
  return {
    ...barBase,
    yAxis: { ...barBase.yAxis, name: 'Incidents' },
    xAxis: { ...barBase.xAxis, data: data.map(d => d.label), axisLabel: { fontSize: 9, rotate: 30 } },
    series: [{
      type: 'bar',
      data: data.map(d => d.value),
      itemStyle: { color: '#d95763', borderRadius: [4, 4, 0, 0] },
      barMaxWidth: 36,
    }],
  }
})

const bySeverityOption = computed(() => {
  const data = report.value?.incidentsBySeverity || []
  const colors = { CRITICAL: '#d95763', MAJOR: '#e49b2f', MINOR: '#168aad' }
  return {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: 6, top: 'middle', textStyle: { fontSize: 10 } },
    series: [{
      type: 'pie',
      radius: ['42%', '70%'],
      center: ['38%', '50%'],
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      data: data.map(d => ({ name: d.label, value: d.value, itemStyle: { color: colors[d.label] || '#7776b8' } })),
    }],
  }
})

const monthChart = ref(null)
const severityChart = ref(null)

function buildPayload() {
  if (!report.value) return null
  const charts = [monthChart.value?.getDataURL(), severityChart.value?.getDataURL()]
    .filter(Boolean)
    .map((d, i) => ({ title: ['Incidents by month', 'Incidents by severity'][i], ...d }))
  return {
    title: 'Mine Safety',
    charts,
    tables: [
      { name: 'Summary', headers: ['Metric', 'Value'], rows: [
        ['Total incidents', report.value.totalIncidents],
        ['Critical', report.value.criticalCount],
        ['Major', report.value.majorCount],
        ['Minor', report.value.minorCount],
        ['Resolved', report.value.resolvedCount],
        ['Resolution rate', report.value.resolutionRate + '%'],
      ]},
      { name: 'Incidents by month', headers: ['Month', 'Count'], rows: report.value.incidentsByMonth.map(d => [d.label, d.value]) },
      { name: 'Recent incidents', headers: ['Date', 'Mine', 'Severity', 'Description', 'Status', 'Reported by'], rows:
        report.value.recentIncidents.map(i => [i.date, i.mineName, i.severity, i.description, i.status, i.reportedBy]) },
    ],
  }
}

registerExporter('mineSafety', async (fmt, el) => {
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
        <h1 class="page-title">Mine Safety</h1>
        <div class="page-desc">Safety incidents tracking and resolution monitoring</div>
      </div>
    </div>

    <div v-if="loading" class="card">Loading...</div>
    <div v-else-if="error" class="card bad-text">Error: {{ error }}</div>
    <template v-else-if="report">
      <div class="cards" style="grid-template-columns:repeat(6,1fr)">
        <div class="card">
          <div class="kpi-label">Total incidents</div>
          <div class="kpi">{{ fmt(report.totalIncidents) }}</div>
          <div class="kpi-sub">All time</div>
        </div>
        <div class="card">
          <div class="kpi-label">Critical</div>
          <div class="kpi bad-text">{{ fmt(report.criticalCount) }}</div>
          <div class="kpi-sub">Severe incidents</div>
        </div>
        <div class="card">
          <div class="kpi-label">Major</div>
          <div class="kpi warn-text">{{ fmt(report.majorCount) }}</div>
          <div class="kpi-sub">Significant incidents</div>
        </div>
        <div class="card">
          <div class="kpi-label">Minor</div>
          <div class="kpi" style="color:var(--blue)">{{ fmt(report.minorCount) }}</div>
          <div class="kpi-sub">Low severity</div>
        </div>
        <div class="card">
          <div class="kpi-label">Resolved</div>
          <div class="kpi good">{{ fmt(report.resolvedCount) }}</div>
          <div class="kpi-sub">Incidents resolved</div>
        </div>
        <div class="card">
          <div class="kpi-label">Resolution rate</div>
          <div class="kpi" :class="report.resolutionRate >= 80 ? 'good' : 'warn-text'">{{ pct(report.resolutionRate) }}</div>
          <div class="kpi-sub">Overall resolution rate</div>
        </div>
      </div>

      <div class="grid-equal">
        <div class="card">
          <div class="card-head"><h3>Incidents by month</h3><span class="small-link">count</span></div>
          <EChart ref="monthChart" :option="byMonthOption" height="270px" />
        </div>
        <div class="card">
          <div class="card-head"><h3>Incidents by severity</h3><span class="small-link">distribution</span></div>
          <EChart ref="severityChart" :option="bySeverityOption" height="270px" />
        </div>
      </div>

      <div class="card table-card">
        <div class="card-head"><h3>Recent safety incidents</h3></div>
        <div class="table-scroll">
          <table>
            <thead>
              <tr><th>Date</th><th>Mine</th><th>Severity</th><th>Description</th><th>Status</th><th>Reported by</th></tr>
            </thead>
            <tbody>
              <tr v-for="inc in report.recentIncidents" :key="inc.id">
                <td>{{ inc.date }}</td>
                <td>{{ inc.mineName }}</td>
                <td><span :class="inc.severity === 'CRITICAL' ? 'pill red' : inc.severity === 'MAJOR' ? 'pill amber' : 'pill blue'">{{ inc.severity }}</span></td>
                <td>{{ inc.description }}</td>
                <td><span :class="inc.status === 'RESOLVED' ? 'pill green' : 'pill amber'">{{ inc.status }}</span></td>
                <td>{{ inc.reportedBy }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
