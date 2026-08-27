<script setup>
import { ref, computed, onMounted } from 'vue'
import { mineProduction } from '../api'
import EChart from '../components/EChart.vue'
import { fmt, pct, PALETTE } from '../format'
import { exportCSV, exportExcel, exportPDF } from '../export'
import { registerExporter } from '../exportStore'

const report = ref(null)
const loading = ref(false)
const error = ref(null)

async function fetchData() {
  loading.value = true
  error.value = null
  try {
    report.value = await mineProduction()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const dailyOption = computed(() => {
  const data = report.value?.dailyProduction || []
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 44, right: 12, top: 18, bottom: 40 },
    xAxis: { type: 'category', data: data.map(d => d.label), axisLabel: { fontSize: 9, rotate: 45 } },
    yAxis: { type: 'value', name: 'Tonnes', axisLabel: { fontSize: 10 } },
    series: [{
      type: 'line',
      data: data.map(d => d.value),
      smooth: true,
      areaStyle: { color: 'rgba(22,138,173,0.15)' },
      itemStyle: { color: '#168aad' },
    }],
  }
})

const materialOption = computed(() => {
  const data = report.value?.productionByMaterial || []
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

const byMineOption = computed(() => {
  const data = report.value?.productionByMine || []
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 100, right: 12, top: 12, bottom: 12 },
    xAxis: { type: 'value', name: 'Tonnes', axisLabel: { fontSize: 10 } },
    yAxis: { type: 'category', data: data.map(d => d.label), axisLabel: { fontSize: 10 } },
    series: [{
      type: 'bar',
      data: data.map(d => d.value),
      itemStyle: { color: '#29a36a', borderRadius: [0, 4, 4, 0] },
      barMaxWidth: 28,
    }],
  }
})

const dailyChart = ref(null)
const materialChart = ref(null)
const mineChart = ref(null)

function buildPayload() {
  if (!report.value) return null
  const charts = [dailyChart.value?.getDataURL(), materialChart.value?.getDataURL(), mineChart.value?.getDataURL()]
    .filter(Boolean)
    .map((d, i) => ({ title: ['Daily production', 'Production by material', 'Production by mine'][i], ...d }))
  return {
    title: 'Mine Production',
    charts,
    tables: [
      { name: 'Summary (30 days)', headers: ['Metric', 'Value'], rows: [
        ['Total extracted', report.value.totalExtracted + ' tonnes'],
        ['Total processed', report.value.totalProcessed + ' tonnes'],
        ['Total shipped', report.value.totalShipped + ' tonnes'],
        ['Processing efficiency', report.value.processingEfficiency + '%'],
      ]},
      { name: 'Production by material', headers: ['Material', 'Tonnes'], rows: report.value.productionByMaterial.map(d => [d.label, d.value]) },
      { name: 'Recent records', headers: ['Date', 'Mine', 'Material', 'Extracted', 'Processed', 'Shipped', 'Shift'], rows:
        report.value.recentRecords.map(r => [r.date, r.mineName, r.materialType, r.extractedTonnes, r.processedTonnes, r.shippedTonnes, r.shift]) },
    ],
  }
}

registerExporter('mineProduction', async (fmt, el) => {
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
        <h1 class="page-title">Mine Production</h1>
        <div class="page-desc">Production tracking - extraction, processing and shipping</div>
      </div>
    </div>

    <div v-if="loading" class="card">Loading...</div>
    <div v-else-if="error" class="card bad-text">Error: {{ error }}</div>
    <template v-else-if="report">
      <div class="cards" style="grid-template-columns:repeat(4,1fr)">
        <div class="card">
          <div class="kpi-label">Total extracted</div>
          <div class="kpi">{{ fmt(report.totalExtracted) }} <span style="font-size:13px;color:var(--muted)">t</span></div>
          <div class="kpi-sub">Last 30 days</div>
        </div>
        <div class="card">
          <div class="kpi-label">Total processed</div>
          <div class="kpi">{{ fmt(report.totalProcessed) }} <span style="font-size:13px;color:var(--muted)">t</span></div>
          <div class="kpi-sub">After processing</div>
        </div>
        <div class="card">
          <div class="kpi-label">Total shipped</div>
          <div class="kpi">{{ fmt(report.totalShipped) }} <span style="font-size:13px;color:var(--muted)">t</span></div>
          <div class="kpi-sub">Delivered to customers</div>
        </div>
        <div class="card">
          <div class="kpi-label">Processing efficiency</div>
          <div class="kpi" :class="report.processingEfficiency >= 85 ? 'good' : 'warn-text'">{{ pct(report.processingEfficiency) }}</div>
          <div class="kpi-sub">Processed / Extracted</div>
        </div>
      </div>

      <div class="card" style="margin-bottom:13px">
        <div class="card-head"><h3>Daily production trend (30 days)</h3><span class="small-link">tonnes</span></div>
        <EChart ref="dailyChart" :option="dailyOption" height="280px" />
      </div>

      <div class="grid-equal">
        <div class="card">
          <div class="card-head"><h3>Production by material</h3><span class="small-link">tonnes</span></div>
          <EChart ref="materialChart" :option="materialOption" height="270px" />
        </div>
        <div class="card">
          <div class="card-head"><h3>Production by mine</h3><span class="small-link">tonnes</span></div>
          <EChart ref="mineChart" :option="byMineOption" height="270px" />
        </div>
      </div>

      <div class="card table-card">
        <div class="card-head"><h3>Recent production records</h3></div>
        <div class="table-scroll">
          <table>
            <thead>
              <tr><th>Date</th><th>Mine</th><th>Material</th><th>Extracted</th><th>Processed</th><th>Shipped</th><th>Shift</th></tr>
            </thead>
            <tbody>
              <tr v-for="r in report.recentRecords" :key="r.id">
                <td>{{ r.date }}</td>
                <td>{{ r.mineName }}</td>
                <td>{{ r.materialType }}</td>
                <td>{{ fmt(r.extractedTonnes) }} t</td>
                <td>{{ fmt(r.processedTonnes) }} t</td>
                <td>{{ fmt(r.shippedTonnes) }} t</td>
                <td>{{ r.shift }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
