<template>
  <section class="dashboard">
    <div class="header">Asset &amp; Capacity Overview</div>

    <!-- FILTERS -->
    <div class="filters">
      <select v-model="filters.organisation">
        <option value="">Organisation</option>
        <option v-for="o in organisations" :key="o.id" :value="o.id">{{ o.shortName || o.name }}</option>
      </select>
      <select v-model="filters.city" @change="onCityChange">
        <option value="">City/Province</option>
        <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
      </select>
      <select v-model="filters.ward">
        <option value="">Ward</option>
        <option v-for="w in wards" :key="w" :value="w">{{ w }}</option>
      </select>
      <select v-model="filters.voltage">
        <option value="">Voltage Level</option>
        <option value="500">500 kV</option>
        <option value="220">220 kV</option>
        <option value="110">110 kV</option>
        <option value="35">35 kV</option>
        <option value="22">22 kV</option>
      </select>
      <select v-model="filters.manufacturer">
        <option value="">Manufacturer</option>
        <option v-for="m in manufacturers" :key="m.id" :value="m.id">{{ m.name }}</option>
      </select>
      <select v-model="filters.status">
        <option value="">Status</option>
        <option value="2">In Operation</option>
        <option value="0">Out of Operation</option>
      </select>
      <button class="apply-btn" @click="fetchData">Apply</button>
    </div>

    <!-- KPI CARDS -->
    <div class="cards">
      <div class="card">
        <div class="card-title">Total Assets</div>
        <div class="card-value">{{ summary.totalAssets?.toLocaleString() || '—' }}</div>
      </div>
      <div class="card">
        <div class="card-title">Total Substations</div>
        <div class="card-value">{{ summary.totalSubstations?.toLocaleString() || '—' }}</div>
      </div>
      <div class="card">
        <div class="card-title">Total Transformers</div>
        <div class="card-value">{{ summary.totalTransformers?.toLocaleString() || '—' }}</div>
      </div>
      <div class="card">
        <div class="card-title">Transformer Capacity</div>
        <div class="card-value">{{ summary.totalCapacityMVA ? Number(summary.totalCapacityMVA).toLocaleString(undefined, {maximumFractionDigits: 1}) : '—' }} <span class="unit">MVA</span></div>
      </div>
    </div>

    <!-- CONTENT: BAR CHART + TREEMAP -->
    <div class="content">
      <div class="panel">
        <div class="panel-title">Transformer Capacity by Voltage Level</div>
        <div class="chart">
          <div class="bar" v-for="(item, idx) in capacityData" :key="item.label">
            <span :style="{ height: barHeightPx(item.heightPct) }">{{ item.capacityMVA?.toLocaleString(undefined, {maximumFractionDigits: 0}) }}</span>
            <div class="label">{{ item.label }}</div>
          </div>
        </div>
      </div>
      <div class="panel">
        <div class="panel-title">Asset Mix by Organisation</div>
        <div class="treemap" v-if="orgMixData.length >= 3">
          <div class="box evnnpc clickable" @click="selectOrg(orgMixData[0])">{{ orgMixData[0]?.name }}<br>{{ orgMixData[0]?.count?.toLocaleString() }} MBA<br>{{ orgMixData[0]?.pct }}%</div>
          <div class="box evnspc clickable" @click="selectOrg(orgMixData[1])">{{ orgMixData[1]?.name }}<br>{{ orgMixData[1]?.count?.toLocaleString() }} MBA<br>{{ orgMixData[1]?.pct }}%</div>
          <div class="right">
            <div class="box evncp clickable" @click="selectOrg(orgMixData[2])">{{ orgMixData[2]?.name }}<br>{{ orgMixData[2]?.count?.toLocaleString() }} MBA<br>{{ orgMixData[2]?.pct }}%</div>
            <div class="box evnhn clickable" v-if="orgMixData[3]" @click="selectOrg(orgMixData[3])">{{ orgMixData[3]?.name }}<br>{{ orgMixData[3]?.count?.toLocaleString() }} MBA<br>{{ orgMixData[3]?.pct }}%</div>
            <div class="box evnhcm clickable" v-if="orgMixData[4]" @click="selectOrg(orgMixData[4])">{{ orgMixData[4]?.name }}<br>{{ orgMixData[4]?.count?.toLocaleString() }} MBA<br>{{ orgMixData[4]?.pct }}%</div>
          </div>
        </div>
        <div v-else class="empty-state">No data available</div>

        <!-- DETAIL OVERLAY -->
        <div class="org-detail-overlay" v-if="selectedOrg" @click.self="selectedOrg = null">
          <div class="org-detail-card">
            <div class="org-detail-header">
              <h3>{{ selectedOrg.name }}</h3>
              <button class="close-btn" @click="selectedOrg = null">&times;</button>
            </div>
            <div class="org-detail-stats">
              <div class="org-stat">
                <div class="org-stat-val">{{ selectedOrg.count?.toLocaleString() }}</div>
                <div class="org-stat-label">Transformers</div>
              </div>
              <div class="org-stat">
                <div class="org-stat-val">{{ selectedOrg.pct }}%</div>
                <div class="org-stat-label">Share</div>
              </div>
              <div class="org-stat">
                <div class="org-stat-val">{{ selectedOrg.capacityMVA ? Number(selectedOrg.capacityMVA).toLocaleString(undefined, {maximumFractionDigits: 1}) : '—' }}</div>
                <div class="org-stat-label">Capacity (MVA)</div>
              </div>
            </div>
            <div class="org-detail-bar">
              <div class="org-bar-label">Proportion of total fleet</div>
              <div class="org-bar-track">
                <div class="org-bar-fill" :style="{ width: selectedOrg.pct + '%' }"></div>
              </div>
              <div class="org-bar-pct">{{ selectedOrg.pct }}%</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- BOTTOM: MANUFACTURERS + DATA COMPLETENESS -->
    <div class="bottom">
      <div class="panel">
        <div class="panel-title">Top Manufacturers</div>
        <table class="table" v-if="topMfrs.length">
          <tr v-for="(m, idx) in topMfrs" :key="idx">
            <td>{{ idx + 1 }}</td>
            <td>{{ m.label }}</td>
            <td class="percent">{{ m.pct }}%</td>
          </tr>
        </table>
        <div v-else class="empty-state">No data available</div>
      </div>
      <div class="panel">
        <div class="panel-title">Data Completeness</div>
        <table class="table" v-if="completenessData.length">
          <tr v-for="(d, idx) in completenessData" :key="idx">
            <td>{{ d.fieldName }}</td>
            <td class="percent">{{ d.missingPct }}%</td>
          </tr>
        </table>
        <div v-else class="empty-state">No data available</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const filters = ref({ organisation: '', city: '', ward: '', voltage: '', manufacturer: '', assetClass: '', status: '' })
const summary = ref({})
const capacityData = ref([])
const orgMixData = ref([])
const topMfrs = ref([])
const completenessData = ref([])
const organisations = ref([])
const manufacturers = ref([])
const cities = ref([])
const wards = ref([])

const selectedOrg = ref(null)

function selectOrg(org) {
  selectedOrg.value = org
}

function barHeightPx(pct) {
  if (!pct || pct <= 0) return '4px'
  return Math.round(pct * 1.8) + 'px'
}

async function onCityChange() {
  filters.value.ward = ''
  try {
    const res = await api.getWards(filters.value.city || undefined)
    wards.value = res?.data || []
  } catch (e) { wards.value = [] }
}

async function fetchData() {
  const p = filters.value
  try {
    const [sumRes, capRes, orgRes, mfrRes, dcRes] = await Promise.all([
      api.getTransformerFleetSummary(p).catch(() => null),
      api.getTransformerCapacityByVoltage(p).catch(() => null),
      api.getMixByOrganisation(p).catch(() => null),
      api.getTopManufacturers(p).catch(() => null),
      api.getDataCompleteness(p).catch(() => null),
    ])
    if (sumRes?.data) summary.value = sumRes.data
    if (capRes?.data?.length) capacityData.value = capRes.data.filter(i => (i.heightPct || 0) > 0)
    if (orgRes?.data?.length) orgMixData.value = orgRes.data.filter(i => (i.pct || 0) > 0)
    if (mfrRes?.data?.length) topMfrs.value = mfrRes.data
    if (dcRes?.data?.length) completenessData.value = dcRes.data
  } catch (e) { console.warn('API not available') }
}

onMounted(async () => {
  fetchData()
  try {
    const [orgRes, mfgRes, cityRes] = await Promise.all([
      api.getOrganisationsWithData().catch(() => null),
      api.getManufacturers().catch(() => null),
      api.getCities().catch(() => null),
    ])
    if (orgRes?.data) organisations.value = orgRes.data
    if (mfgRes?.data) manufacturers.value = mfgRes.data
    if (cityRes?.data) cities.value = cityRes.data
  } catch (e) {}
})
</script>

<style scoped>
* { box-sizing: border-box; }
.dashboard { width: 95%; margin: 20px auto; }

.header {
  background: #06458f; color: white;
  padding: 18px 25px; font-size: 24px; font-weight: bold;
}

.filters {
  display: grid; grid-template-columns: repeat(6, 1fr) auto;
  gap: 12px; margin: 15px 0;
}
.filters select {
  height: 35px; border: 1px solid #d0d9e6; border-radius: 5px;
  padding: 0 10px; color: #34495e; background: white;
}
.apply-btn {
  height: 35px; padding: 0 20px; border: none; border-radius: 5px;
  background: #06458f; color: white; font-weight: 600; font-size: 13px;
  cursor: pointer; transition: background .2s;
}
.apply-btn:hover { background: #053670; }

.cards {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px;
}
.card {
  background: white; padding: 20px; border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,.08);
}
.card-title { font-size: 14px; font-weight: bold; color: #445; }
.card-value { font-size: 40px; margin-top: 10px; font-weight: bold; color: #163b70; }
.unit { font-size: 18px; }

.content {
  display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-top: 15px;
}
.panel {
  background: white; padding: 20px; border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,.08);
}
.panel-title { font-weight: bold; margin-bottom: 20px; }

/* Bar chart */
.chart {
  height: 220px; display: flex; align-items: flex-end; justify-content: space-around;
}
.bar {
  width: 55px; text-align: center; color: white; padding-top: 5px;
  display: flex; flex-direction: column; align-items: center;
}
.bar span {
  display: block; background: #1d67c1; border-radius: 3px 3px 0 0;
  width: 100%; min-height: 4px;
  transition: height .3s;
}
.bar:nth-child(2) span { background: #54a853; }
.bar:nth-child(3) span { background: #f6a623; }
.bar:nth-child(4) span { background: #7560b5; }
.bar:nth-child(5) span { background: #1597a7; }
.label { color: #444; margin-top: 10px; font-size: 13px; }

/* Treemap */
.treemap {
  display: grid; grid-template-columns: 35% 25% 40%; height: 220px; gap: 2px;
}
.box {
  display: flex; align-items: center; justify-content: center;
  color: white; font-weight: bold; text-align: center; font-size: 13px;
}
.evnnpc { background: #1473d4; }
.evnspc { background: #55aa4f; }
.right { display: grid; grid-template-rows: repeat(3, 1fr); }
.evncp { background: #f5a623; }
.evnhn { background: #6561b3; }
.evnhcm { background: #1597a7; }
.clickable { cursor: pointer; transition: filter .2s, transform .2s; }
.clickable:hover { filter: brightness(1.15); transform: scale(1.02); }

/* Org detail overlay */
.org-detail-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.45);
  display: flex; align-items: center; justify-content: center; z-index: 100;
}
.org-detail-card {
  background: white; border-radius: 12px; padding: 28px 32px;
  min-width: 400px; max-width: 500px; box-shadow: 0 8px 32px rgba(0,0,0,.2);
}
.org-detail-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;
}
.org-detail-header h3 { margin: 0; font-size: 18px; color: #1d3557; flex: 1; padding-right: 12px; }
.close-btn {
  background: none; border: none; font-size: 24px; cursor: pointer; color: #999;
  line-height: 1; padding: 0 4px;
}
.close-btn:hover { color: #333; }
.org-detail-stats {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 20px;
}
.org-stat {
  background: #f5f8fc; border-radius: 8px; padding: 14px; text-align: center;
}
.org-stat-val { font-size: 22px; font-weight: 800; color: #163b70; }
.org-stat-label { font-size: 11px; color: #6b7280; margin-top: 4px; }
.org-detail-bar { margin-top: 4px; }
.org-bar-label { font-size: 12px; color: #6b7280; margin-bottom: 6px; }
.org-bar-track {
  height: 10px; background: #eef2f5; border-radius: 99px; overflow: hidden;
}
.org-bar-fill {
  height: 100%; border-radius: 99px; background: linear-gradient(90deg, #1473d4, #38bdf8);
  transition: width .4s ease;
}
.org-bar-pct { font-size: 12px; font-weight: 700; color: #163b70; text-align: right; margin-top: 4px; }

/* Bottom */
.bottom {
  display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-top: 15px;
}

/* Table */
.table { width: 100%; border-collapse: collapse; }
.table td { padding: 10px; border-bottom: 1px solid #eee; }
.percent { text-align: right; color: #2363bd; font-weight: bold; }

.empty-state { padding: 40px; text-align: center; color: #9ca3af; font-size: 13px; }
</style>
