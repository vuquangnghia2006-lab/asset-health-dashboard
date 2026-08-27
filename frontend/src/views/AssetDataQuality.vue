<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { dataQuality } from '../api'
import { store, toParams } from '../store'
import FilterBar from '../components/FilterBar.vue'
import EChart from '../components/EChart.vue'
import { fmt, pct, PALETTE, barBase } from '../format'
import { exportCSV, exportExcel, exportPDF } from '../export'
import { registerExporter } from '../exportStore'

const report = ref(null)
const loading = ref(false)
const error = ref(null)
const activeTab = ref('Transformer')

async function fetchData() {
  loading.value = true
  error.value = null
  try {
    report.value = await dataQuality(toParams(store.filters))
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

watch(() => store.filters, fetchData)
onMounted(fetchData)

const cards = computed(() => report.value?.cards || {})

const catOption = computed(() => {
  const data = report.value?.completenessByCategory || []
  return {
    ...barBase,
    yAxis: { ...barBase.yAxis, name: '%', max: 100 },
    xAxis: { ...barBase.xAxis, data: data.map((d) => d.label), axisLabel: { fontSize: 9, interval: 0, rotate: 18 } },
    series: [
      {
        type: 'bar',
        data: data.map((d) => d.value),
        itemStyle: {
          color: (p) => (p.value >= 80 ? '#29a36a' : p.value >= 50 ? '#e49b2f' : '#d95763'),
          borderRadius: [4, 4, 0, 0],
        },
        barMaxWidth: 42,
      },
    ],
  }
})

const statusOption = computed(() => {
  const data = report.value?.statusDistribution || []
  return {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: 6, top: 'middle', textStyle: { fontSize: 10 } },
    series: [
      {
        type: 'pie',
        radius: ['42%', '70%'],
        center: ['38%', '50%'],
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        data: data.map((d, i) => ({ name: d.label, value: d.value, itemStyle: { color: PALETTE[i % PALETTE.length] } })),
      },
    ],
  }
})

const tabs = computed(() => {
  const set = new Set((report.value?.missingKeyFields || []).map((m) => m.assetClass))
  return ['Transformer', 'Circuit Breaker', 'Current Transformer (CT)', 'Voltage Transformer (VT)', 'Disconnector', 'Surge Arrester', 'Bushing'].filter((c) => set.has(c))
})

const visibleMissing = computed(() => {
  const all = report.value?.missingKeyFields || []
  const cls = activeTab.value
  if (!all.some((m) => m.assetClass === cls) && tabs.value.length) {
    return all.filter((m) => m.assetClass === tabs.value[0])
  }
  return all.filter((m) => m.assetClass === cls)
})

const phaseStatusClass = (s) => (s === 'Balanced' ? 'pill green' : s === 'Mismatch' ? 'pill red' : 'pill blue')

const phaseTotals = computed(() => {
  const rows = report.value?.phaseBalance || []
  return {
    a: rows.reduce((s, r) => s + (r.phaseA || 0), 0),
    b: rows.reduce((s, r) => s + (r.phaseB || 0), 0),
    c: rows.reduce((s, r) => s + (r.phaseC || 0), 0),
  }
})
const catChart = ref(null)
const statusChart = ref(null)

function buildPayload() {
  if (!report.value) throw new Error('Data not loaded yet')
  const c = report.value.cards
  const charts = [catChart.value?.getDataURL(), statusChart.value?.getDataURL()]
    .filter(Boolean)
    .map((d, i) => ({
      title: i === 0 ? 'Completeness by category' : 'Status distribution',
      ...d,
    }))
  return {
    title: 'Asset Data Quality',
    charts,
    tables: [
      {
        name: 'Overview',
        headers: ['Metric', 'Value'],
        rows: [
          ['Overall completeness (%)', c.overallCompletenessPct],
          ['Assets with missing fields', c.assetsWithMissingFields],
          ['Missing nameplates', c.missingNameplates],
          ['Phase-monitored assets', c.phaseMonitoredAssets],
          ['Balanced groups (%)', c.balancedGroupsPct],
          ['Phase mismatches', c.phaseMismatches],
        ],
      },
      {
        name: 'Completeness by category',
        headers: ['Category', 'Completeness (%)'],
        rows: report.value.completenessByCategory.map((d) => [d.label, d.value]),
      },
      {
        name: 'Status distribution',
        headers: ['Status', 'Assets'],
        rows: report.value.statusDistribution.map((d) => [d.label, d.value]),
      },
      {
        name: 'Phase balance',
        headers: ['Asset class', 'Phase A', 'Phase B', 'Phase C', 'Status'],
        rows: report.value.phaseBalance.map((r) => [r.assetClass, r.phaseA, r.phaseB, r.phaseC, r.status]),
      },
      {
        name: 'Missing key fields',
        headers: ['Asset class', 'Field', 'Category', 'Missing', 'Total', 'Missing %'],
        rows: report.value.missingKeyFields.map((m) => [m.assetClass, m.field, m.category, m.missing, m.total, m.missingPct]),
      },
    ],
  }
}

registerExporter('quality', async (fmt, el) => {
  const p = buildPayload()
  if (fmt === 'csv') exportCSV(p)
  else if (fmt === 'xlsx') exportExcel(p)
  else await exportPDF(el, p)
})
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h1 class="page-title">Asset Data Quality</h1>
        <div class="page-desc">Completeness, key-field coverage and phase-balance monitoring</div>
      </div>
    </div>

    <FilterBar />

    <div v-if="loading" class="card">Loading…</div>
    <div v-else-if="error" class="card bad-text">Error: {{ error }}</div>
    <template v-else-if="report">
      <div class="cards six">
        <div class="card">
          <div class="kpi-label">Overall completeness</div>
          <div class="kpi" :class="cards.overallCompletenessPct >= 80 ? 'good' : cards.overallCompletenessPct >= 50 ? 'warn-text' : 'bad-text'">
            {{ pct(cards.overallCompletenessPct) }}
          </div>
          <div class="kpi-sub">Weighted across key fields</div>
        </div>
        <div class="card">
          <div class="kpi-label">Assets with missing fields</div>
          <div class="kpi bad-text">{{ fmt(cards.assetsWithMissingFields) }}</div>
          <div class="kpi-sub">At least one key field missing</div>
        </div>
        <div class="card">
          <div class="kpi-label">Missing nameplates</div>
          <div class="kpi warn-text">{{ fmt(cards.missingNameplates) }}</div>
          <div class="kpi-sub">No nameplate media attached</div>
        </div>
        <div class="card">
          <div class="kpi-label">Phase-monitored assets</div>
          <div class="kpi">{{ fmt(cards.phaseMonitoredAssets) }}</div>
          <div class="kpi-sub">Phase tag present on asset</div>
        </div>
        <div class="card">
          <div class="kpi-label">Balanced groups</div>
          <div class="kpi" :class="cards.balancedGroupsPct >= 80 ? 'good' : 'warn-text'">{{ pct(cards.balancedGroupsPct) }}</div>
          <div class="kpi-sub">Equal A/B/C counts per substation group</div>
        </div>
        <div class="card">
          <div class="kpi-label">Phase mismatches</div>
          <div class="kpi bad-text">{{ fmt(cards.phaseMismatches) }}</div>
          <div class="kpi-sub">Groups requiring review</div>
        </div>
      </div>

      <div class="grid2">
        <div class="card">
          <div class="card-head"><h3>Completeness by category</h3><span class="small-link">%</span></div>
          <EChart ref="catChart" :option="catOption" height="270px" />
        </div>
        <div class="card">
          <div class="card-head"><h3>Status distribution</h3><span class="small-link">assets</span></div>
          <EChart ref="statusChart" :option="statusOption" height="270px" />
        </div>
      </div>

      <div class="grid-equal">
        <div class="card">
          <div class="card-head"><h3>Asset count by phase</h3></div>
          <div class="phase-scroll">
            <div class="phase-grid">
              <div class="phase-head">Asset class</div>
              <div class="phase-head">Phase A</div>
              <div class="phase-head">Phase B</div>
              <div class="phase-head">Phase C</div>
              <div class="phase-head">Status</div>
              <template v-for="r in report.phaseBalance" :key="r.assetClass">
                <div>{{ r.assetClass }}</div>
                <div class="phase-a">{{ fmt(r.phaseA) }}</div>
                <div class="phase-b">{{ fmt(r.phaseB) }}</div>
                <div class="phase-c">{{ fmt(r.phaseC) }}</div>
                <div><span :class="phaseStatusClass(r.status)">{{ r.status }}</span></div>
              </template>
            </div>
          </div>
          <div class="phase-total">
            <div class="phase-total-cell phase-a-bg">
              <span>Phase A total</span>
              <strong>{{ fmt(phaseTotals.a) }}</strong>
            </div>
            <div class="phase-total-cell phase-b-bg">
              <span>Phase B total</span>
              <strong>{{ fmt(phaseTotals.b) }}</strong>
            </div>
            <div class="phase-total-cell phase-c-bg">
              <span>Phase C total</span>
              <strong>{{ fmt(phaseTotals.c) }}</strong>
            </div>
          </div>
        </div>

        <div class="card table-card">
          <div class="card-head"><h3>Missing key fields</h3></div>
          <div class="asset-tabs">
            <button
              v-for="t in tabs"
              :key="t"
              class="asset-tab"
              :class="{ active: activeTab === t }"
              @click="activeTab = t"
            >
              {{ t }}
            </button>
          </div>
          <div class="table-note">Each asset-type tab includes the common key fields plus its asset-specific key fields.</div>
          <div class="scroll-area">
            <table>
              <thead>
                <tr><th>Field</th><th>Category</th><th>Missing count</th><th>Missing percent</th></tr>
              </thead>
              <tbody>
                <tr v-for="m in visibleMissing" :key="m.assetClass + m.field">
                  <td>{{ m.field }}</td>
                  <td>{{ m.category }}</td>
                  <td>{{ fmt(m.missing) }} / {{ fmt(m.total) }}</td>
                  <td :class="m.missingPct > 10 ? 'bad-text' : 'good'">{{ pct(m.missingPct) }}</td>
                </tr>
                <tr v-if="!visibleMissing.length">
                  <td colspan="4" style="text-align:center;color:var(--muted)">No data.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
