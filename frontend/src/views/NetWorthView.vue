<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import NetWorthChart from "@/components/NetWorthChart.vue"
import NetWorthAllocationChart from "@/components/NetWorthAllocationChart.vue"

interface NetWorthPoint {
  id: number
  date: string          // "2025-01-01"
  totalValue: number
  cashValue: number
  investedValue: number
}

interface PortfolioPoint {
  date: string
  value: number
}

const networth = ref<NetWorthPoint[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const lineData = computed<PortfolioPoint[]>(() =>
    networth.value.map((p) => ({
      date: p.date,
      value: p.totalValue,
    })),
)

onMounted(async () => {
  loading.value = true
  error.value = null
  try {
    const res = await fetch("http://localhost:8080/networth")
    if (!res.ok) {
      throw new Error(`Fehler beim Laden der Networth-Daten (${res.status})`)
    }
    const data: NetWorthPoint[] = await res.json()
    networth.value = data
  } catch (e: any) {
    error.value = e?.message ?? "Unbekannter Fehler beim Laden der Daten"
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="space-y-6">
    <div v-if="error" class="text-sm text-red-500">
      {{ error }}
    </div>

    <div v-if="loading" class="text-sm text-muted-foreground">
      Lade Vermögensverlauf…
    </div>

    <NetWorthChart v-if="!loading && networth.length" :data="lineData" />
    <NetWorthAllocationChart v-if="!loading && networth.length" :data="networth" />
  </div>
</template>
