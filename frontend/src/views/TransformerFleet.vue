<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { transformerFleet, parallelPairs } from '../api'
import { store, toParams } from '../store'
import FilterBar from '../components/FilterBar.vue'
import EChart from '../components/EChart.vue'
import { fmt, pct, PALETTE, barBase } from '../format'
import { exportCSV, exportExcel, exportPDF } from '../export'
import { registerExporter } from '../exportStore'

const fleet = ref(null)
const pairs = ref(null)
const loading = ref(false)
const loadingPairs = ref(false)
const error = ref(null)
const errorPairs = ref(null)
const router = useRouter()

function goHome() {
  store.filters = { orgId: '', city: '', ward: '', voltageLevel: '', manufacturerId: '', assetClass: '', status: '' }
  router.push('/')
}

const parCfg = reactive({
  maxUkDiff: 10,
  sameSite: true,
  requireTapOverlap: true,
  maxPowerRatio: 0,
})

async function fetchFleet() {
  loading.value = true
  error.value = null
  try {
    fleet.value = await transformerFleet(toParams(store.filters))
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function fetchPairs() {
  loadingPairs.value = true
  errorPairs.value = null
  try {
    pairs.value = await parallelPairs(
      {
        orgId: store.filters.orgId || undefined,
        city: store.filters.city || undefined,
        ward: store.filters.ward || undefined,
        voltageLevel: store.filters.voltageLevel || undefined,
        manufacturerId: store.filters.manufacturerId || undefined,
        status: store.filters.status || undefined,
      },
      parCfg,
    )
  } catch (e) {
    errorPairs.value = e.message
  } finally {
    loadingPairs.value = false
  }
}

watch(() => store.filters, () => {
  fetchFleet()
  fetchPairs()
})
onMounted(() => {
  fetchFleet()
  fetchPairs()
})

const activeCriterion = ref('')
const criteria = computed(() => {
  const all = pairs.value?.pairs || []
  return [
    { key: 'ratio', label: 'Transformation ratio', desc: 'Exact match or deviation within configured tolerance.',
      count: all.filter((p) => p.ratioOk === false).length },
    { key: 'vector', label: 'Vector group', desc: 'Must be compatible in phase displacement.',
      count: all.filter((p) => p.vectorOk === false).length },
    { key: 'uk', label: 'Impedance Uk%', desc: 'Relative deviation compared against technical threshold.',
      count: all.filter((p) => p.ukOk === false).length },
    { key: 'tap', label: 'Tap & rated power', desc: 'Checks tap range and rated power ratio where data is available.',
      count: all.filter((p) => p.tapOk === false).length },
  ]
})

const visiblePairs = computed(() => {
  const all = pairs.value?.pairs || []
  if (!activeCriterion.value) return all
  return all.filter((p) => {
    const field = activeCriterion.value === 'ratio' ? 'ratioOk'
      : activeCriterion.value === 'vector' ? 'vectorOk'
      : activeCriterion.value === 'uk' ? 'ukOk' : 'tapOk'
    return p[field] === false
  })
})

const capacityOption = computed(() => {
  const data = fleet.value?.capacityByVoltage || []
  return {
    ...barBase,
    yAxis: { ...barBase.yAxis, name: 'MVA' },
    xAxis: { ...barBase.xAxis, data: data.map((d) => d.label) },
    series: [
      {
        type: 'bar',
        data: data.map((d) => d.value),
        itemStyle: { color: '#29a36a', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 42,
      },
    ],
  }
})

const mixOption = computed(() => {
  const data = fleet.value?.topManufacturers || []
  const total = data.reduce((s, d) => s + d.count, 0) || 1
  return {
    tooltip: { trigger: 'item', formatter: (p) => `${p.name}<br/>${fmt(p.value)} (${pct((p.value / total) * 100)})` },
    legend: { orient: 'vertical', right: 6, top: 'middle', textStyle: { fontSize: 10 } },
    series: [
      {
        type: 'pie',
        radius: ['42%', '70%'],
        center: ['38%', '50%'],
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        data: data.map((d, i) => ({ name: d.name, value: d.count, itemStyle: { color: PALETTE[i % PALETTE.length] } })),
      },
    ],
  }
})

const ageOption = computed(() => {
  const data = (fleet.value?.ageDistribution || []).slice().sort((a, b) => +a.label - +b.label)
  return {
    ...barBase,
    xAxis: { ...barBase.xAxis, data: data.map((d) => d.label), axisLabel: { fontSize: 9, interval: 3 } },
    series: [
      {
        type: 'bar',
        data: data.map((d) => d.value),
        itemStyle: { color: '#e49b2f', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 18,
      },
    ],
  }
})

const cards = computed(() => fleet.value?.cards || {})
const pillClass = (r) => (r === 'Compatible' ? 'pill green' : r === 'Review' ? 'pill amber' : 'pill red')
const capacityChart = ref(null)
const mixChart = ref(null)
const ageChart = ref(null)

function buildPayload() {
  if (!fleet.value) throw new Error('Data not loaded yet')
  const c = fleet.value.cards
  const charts = [capacityChart.value?.getDataURL(), mixChart.value?.getDataURL(), ageChart.value?.getDataURL()]
    .filter(Boolean)
    .map((d, i) => ({
      title: ['Transformer capacity by voltage level', 'Fleet mix by manufacturer', 'Age distribution'][i],
      ...d,
    }))
  const tables = [
    {
      name: 'Overview',
      headers: ['Metric', 'Value'],
      rows: [
        ['Total transformers', c.totalTransformers],
        ['Total rated capacity (MVA)', c.totalRatedCapacityMva],
        ['Nameplate coverage (%)', c.nameplatePct],
        ['Average fleet age (years)', c.avgFleetAge],
      ],
    },
    {
      name: 'Capacity by voltage level',
      headers: ['Voltage level', 'Capacity (MVA)'],
      rows: fleet.value.capacityByVoltage.map((d) => [d.label, d.value]),
    },
    {
      name: 'Fleet mix by manufacturer',
      headers: ['Manufacturer', 'Assets', 'Share (%)'],
      rows: fleet.value.topManufacturers.map((d) => [d.name, d.count, d.pct]),
    },
    {
      name: 'Age distribution',
      headers: ['Manufacturing year', 'Transformers'],
      rows: fleet.value.ageDistribution.map((d) => [d.label, d.value]),
    },
  ]
  if (pairs.value) {
    tables.push(
      {
        name: 'Parallel pairs - summary',
        headers: ['Metric', 'Value'],
        rows: [
          ['Total pairs', pairs.value.totalPairs],
          ['Compatible', pairs.value.compatible],
          ['Review', pairs.value.review],
          ['Not compatible', pairs.value.notCompatible],
          ['Max Uk% diff', pairs.value.maxUkDiff],
        ],
      },
      {
        name: 'Parallel pairs - detail',
        headers: ['Transformer A', 'Transformer B', 'Site A', 'Site B', 'Ratio', 'Vector group', 'Tap info', 'Uk% diff', 'Result'],
        rows: pairs.value.pairs.map((p) => [p.nameA, p.nameB, p.siteA, p.siteB, p.ratio, p.vectorGroup || '', p.tapInfo || '', p.ukDiff, p.result]),
      },
    )
  }
  return { title: 'Transformer Fleet', charts, tables }
}

registerExporter('fleet', async (fmt, el) => {
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
        <h1 class="page-title">Transformer Fleet</h1>
        <div class="page-desc">Inventory, capacity, manufacturer profile and parallel-operation screening</div>
      </div>
      <button class="btn secondary" @click="goHome">← Back to Overview</button>
    </div>

    <FilterBar :fields="['orgId', 'city', 'ward', 'voltageLevel', 'manufacturerId', 'status']" />

    <div v-if="loading" class="card">Loading…</div>
    <div v-else-if="error" class="card bad-text">Error: {{ error }}</div>
    <template v-else-if="fleet">
      <div class="cards">
        <div class="card">
          <div class="kpi-label">Total transformers</div>
          <div class="kpi">{{ fmt(cards.totalTransformers) }}</div>
          <div class="kpi-sub">Power transformers in scope</div>
        </div>
        <div class="card">
          <div class="kpi-label">Total rated capacity</div>
          <div class="kpi">{{ fmt(cards.totalRatedCapacityMva) }} <span style="font-size:14px;color:var(--muted)">MVA</span></div>
          <div class="kpi-sub">Normalised to MVA</div>
        </div>
        <div class="card">
          <div class="kpi-label">Nameplate coverage</div>
          <div class="kpi" :class="cards.nameplatePct < 50 ? 'bad-text' : 'good'">{{ pct(cards.nameplatePct) }}</div>
          <div class="kpi-sub">Transformers with nameplate media</div>
        </div>
        <div class="card">
          <div class="kpi-label">Average fleet age</div>
          <div class="kpi">{{ fmt(cards.avgFleetAge) }} <span style="font-size:14px;color:var(--muted)">yrs</span></div>
          <div class="kpi-sub">From manufacturing year</div>
        </div>
        <div class="card">
          <div class="kpi-label">Parallel-ready pairs</div>
          <div class="kpi good">{{ pairs ? fmt(pairs.compatible) : '-' }}</div>
          <div class="kpi-sub">Compatible pairs from screening</div>
        </div>
      </div>

      <div class="grid3">
        <div class="card">
          <div class="card-head">
            <h3>Transformer capacity by voltage level</h3>
            <span class="small-link">MVA</span>
          </div>
          <EChart ref="capacityChart" :option="capacityOption" height="270px" />
        </div>
        <div class="card">
          <div class="card-head">
            <h3>Fleet mix by manufacturer</h3>
            <span class="small-link">count</span>
          </div>
          <EChart ref="mixChart" :option="mixOption" height="270px" />
        </div>
        <div class="card">
          <div class="card-head">
            <h3>Age distribution</h3>
            <span class="small-link">transformers by manufacturing year</span>
          </div>
          <EChart ref="ageChart" :option="ageOption" height="270px" />
        </div>
      </div>
    </template>

    <div class="compat">
      <div class="compat-head">
        <div>
          <h3>Parallel operation compatibility</h3>
          <div class="page-desc" style="margin-top:2px">Technical screening of transformer pairs</div>
        </div>
        <button class="btn" @click="fetchPairs">Recalculate</button>
      </div>
      <div class="compat-body">
        <div class="criteria">
          <button
            v-for="c in criteria"
            :key="c.key"
            class="criterion"
            :class="{ active: activeCriterion === c.key, bad: c.count > 0 }"
            @click="activeCriterion = activeCriterion === c.key ? '' : c.key"
          >
            <strong>{{ c.label }}</strong>
            <span v-if="c.count > 0" class="criterion-count">{{ fmt(c.count) }} failing</span>
            <span v-else class="criterion-ok">No issues</span>
            <p>{{ c.desc }}</p>
          </button>
        </div>
        <div class="compat-controls">
          <div>
            <label>Maximum Uk% difference</label>
            <input v-model.number="parCfg.maxUkDiff" type="number" min="0" max="100" />
          </div>
          <div class="checks">
            <label><input v-model="parCfg.sameSite" type="checkbox" /> Same site only</label>
            <label><input v-model="parCfg.requireTapOverlap" type="checkbox" /> Require tap overlap</label>
            <label>Max power ratio
              <input v-model.number="parCfg.maxPowerRatio" type="number" min="0" step="0.1" style="width:70px" />
            </label>
          </div>
          <button class="btn" @click="fetchPairs">Recalculate</button>
        </div>

        <div v-if="loadingPairs" class="card" style="margin-top:11px">Loading pairs…</div>
        <div v-else-if="errorPairs" class="card bad-text" style="margin-top:11px">Error: {{ errorPairs }}</div>
        <template v-else-if="pairs">
          <div class="cards" style="grid-template-columns:repeat(4,1fr);margin-top:11px">
            <div class="card">
              <div class="kpi-label">Total pairs</div>
              <div class="kpi">{{ fmt(pairs.totalPairs) }}</div>
            </div>
            <div class="card">
              <div class="kpi-label">Compatible</div>
              <div class="kpi good">{{ fmt(pairs.compatible) }}</div>
            </div>
            <div class="card">
              <div class="kpi-label">Review</div>
              <div class="kpi warn-text">{{ fmt(pairs.review) }}</div>
            </div>
            <div class="card">
              <div class="kpi-label">Not compatible</div>
              <div class="kpi bad-text">{{ fmt(pairs.notCompatible) }}</div>
            </div>
          </div>
          <div v-if="activeCriterion" class="criterion-filter-note">
            Filtered to {{ fmt(visiblePairs.length) }} pair(s) failing
            <strong>{{ criteria.find((c) => c.key === activeCriterion)?.label }}</strong>
            — <button class="small-link" @click="activeCriterion = ''">clear</button>
          </div>
          <div class="table-card table-scroll">
            <table>
              <thead>
                <tr><th>Transformer A</th><th>Transformer B</th><th>Site</th><th>Ratio</th><th>Vector group</th><th>Tap info</th><th>Uk% diff</th><th>Result</th></tr>
              </thead>
              <tbody>
                <tr v-for="p in visiblePairs" :key="p.id">
                  <td>{{ p.nameA }}</td>
                  <td>{{ p.nameB }}</td>
                  <td>{{ p.siteA }}<template v-if="p.siteB && p.siteB !== p.siteA"> / {{ p.siteB }}</template></td>
                  <td>{{ p.ratio }}</td>
                  <td>{{ p.vectorGroup || '-' }}</td>
                  <td>{{ p.tapInfo || '-' }}</td>
                  <td>{{ p.ukDiff }}</td>
                  <td><span :class="pillClass(p.result)">{{ p.result }}</span></td>
                </tr>
                <tr v-if="!visiblePairs.length">
                  <td colspan="8" style="text-align:center;color:var(--muted)">No pairs in scope.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
