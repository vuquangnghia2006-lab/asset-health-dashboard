<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { assetOverview } from '../api'
import { store, toParams } from '../store'
import FilterBar from '../components/FilterBar.vue'
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
    report.value = await assetOverview(toParams(store.filters))
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

watch(() => store.filters, fetchData)
onMounted(fetchData)

const capacityOption = computed(() => {
  const data = report.value?.capacityByVoltage || []
  return {
    ...barBase,
    yAxis: { ...barBase.yAxis, name: 'MVA' },
    xAxis: { ...barBase.xAxis, data: data.map((d) => d.label) },
    series: [
      {
        type: 'bar',
        data: data.map((d) => d.value),
        itemStyle: { color: '#168aad', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 42,
      },
    ],
  }
})

const mixOption = computed(() => {
  const data = report.value?.assetMixByOrg || []
  return {
    tooltip: { formatter: (p) => `${p.name}<br/>${fmt(p.value)} assets<br/><span style="color:#bbb">Click to open fleet</span>` },
    series: [
      {
        type: 'treemap',
        roam: false,
        nodeClick: false,
        breadcrumb: { show: false },
        label: { fontSize: 10, color: '#fff' },
        upperLabel: { show: false },
        itemStyle: { borderColor: '#fff', borderWidth: 2, gapWidth: 2, cursor: 'pointer' },
        levels: [
          { itemStyle: { borderColor: '#fff', borderWidth: 2, gapWidth: 2 } },
        ],
        data: data.map((d, i) => ({
          name: d.label,
          value: d.value,
          id: d.id,
          itemStyle: { color: PALETTE[i % PALETTE.length] },
        })),
      },
    ],
  }
})

const cards = computed(() => report.value?.cards || {})
const capacityChart = ref(null)
const mixChart = ref(null)
const router = useRouter()

function openOrgMix(params) {
  const id = params?.data?.id
  if (!id) return
  store.filters = { ...store.filters, orgId: String(id) }
  router.push('/transformer-fleet')
}

function buildPayload() {
  if (!report.value) return null
  const c = report.value.cards
  const charts = [capacityChart.value?.getDataURL(), mixChart.value?.getDataURL()]
    .filter(Boolean)
    .map((d, i) => ({
      title: i === 0 ? 'Transformer capacity by voltage level' : 'Asset mix by organisation',
      ...d,
    }))
  return {
    title: 'Asset Overview',
    charts,
    tables: [
      {
        name: 'Overview',
        headers: ['Metric', 'Value'],
        rows: [
          ['Total assets', c.totalAssets],
          ['Total substations', c.totalSubstations],
          ['Total transformers', c.totalTransformers],
          ['Transformer capacity (MVA)', c.transformerCapacityMva],
        ],
      },
      {
        name: 'Capacity by voltage level',
        headers: ['Voltage level', 'Capacity (MVA)'],
        rows: report.value.capacityByVoltage.map((d) => [d.label, d.value]),
      },
      {
        name: 'Asset mix by organisation',
        headers: ['Organisation', 'Assets'],
        rows: report.value.assetMixByOrg.map((d) => [d.label, d.value]),
      },
      {
        name: 'Top manufacturers',
        headers: ['Manufacturer', 'Assets', 'Share (%)'],
        rows: report.value.topManufacturers.map((d) => [d.name, d.count, d.pct]),
      },
      {
        name: 'Data completeness - transformers',
        headers: ['Field', 'Missing', 'Total', 'Missing %'],
        rows: report.value.dataCompleteness.map((d) => [d.field, d.missing, d.total, d.missingPct]),
      },
    ],
  }
}

registerExporter('overview', async (fmt, el) => {
  const p = buildPayload()
  if (!p) throw new Error('Data not loaded yet')
  if (fmt === 'csv') exportCSV(p)
  else if (fmt === 'xlsx') exportExcel(p)
  else await exportPDF(el, p)
})
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">Asset Overview</h1>
        <div class="page-desc">Inventory, capacity and data completeness across the asset fleet</div>
      </div>
    </div>

    <FilterBar />

    <div v-if="loading" class="card">Loading…</div>
    <div v-else-if="error" class="card bad-text">Error: {{ error }}</div>
    <template v-else-if="report">
      <div class="cards" style="grid-template-columns:repeat(4,1fr)">
        <div class="card">
          <div class="kpi-label">Total assets</div>
          <div class="kpi">{{ fmt(cards.totalAssets) }}</div>
          <div class="kpi-sub">All asset classes in scope</div>
        </div>
        <div class="card">
          <div class="kpi-label">Total substations</div>
          <div class="kpi">{{ fmt(cards.totalSubstations) }}</div>
          <div class="kpi-sub">Substations owning assets</div>
        </div>
        <div class="card">
          <div class="kpi-label">Total transformers</div>
          <div class="kpi">{{ fmt(cards.totalTransformers) }}</div>
          <div class="kpi-sub">Power transformers</div>
        </div>
        <div class="card">
          <div class="kpi-label">Transformer capacity</div>
          <div class="kpi">{{ fmt(cards.transformerCapacityMva) }} <span style="font-size:14px;color:var(--muted)">MVA</span></div>
          <div class="kpi-sub">Rated power, normalised to MVA</div>
        </div>
      </div>

      <div class="grid2">
        <div class="card">
          <div class="card-head">
            <h3>Transformer capacity by voltage level</h3>
            <span class="small-link">MVA</span>
          </div>
          <EChart ref="capacityChart" :option="capacityOption" height="270px" />
        </div>
        <div class="card">
          <div class="card-head">
            <h3>Asset mix by organisation</h3>
            <span class="small-link">count</span>
          </div>
          <EChart ref="mixChart" :option="mixOption" height="270px" @click="openOrgMix" />
        </div>
      </div>

      <div class="grid2">
        <div class="card table-card">
          <div class="card-head"><h3>Top manufacturers</h3></div>
          <table>
            <thead>
              <tr><th>Manufacturer</th><th>Assets</th><th>Share</th></tr>
            </thead>
            <tbody>
              <tr v-for="m in report.topManufacturers" :key="m.name">
                <td>{{ m.name }}</td>
                <td>{{ fmt(m.count) }}</td>
                <td>{{ pct(m.pct) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="card table-card">
          <div class="card-head"><h3>Data completeness — transformers</h3></div>
          <table>
            <thead>
              <tr><th>Field</th><th>Missing</th><th>Missing %</th></tr>
            </thead>
            <tbody>
              <tr v-for="d in report.dataCompleteness" :key="d.field">
                <td>{{ d.field }}</td>
                <td>{{ fmt(d.missing) }} / {{ fmt(d.total) }}</td>
                <td :class="d.missingPct > 10 ? 'bad-text' : 'good'">{{ pct(d.missingPct) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
