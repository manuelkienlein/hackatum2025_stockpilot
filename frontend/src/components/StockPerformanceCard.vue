<template>
  <div class="w-full rounded-xl border bg-card text-card-foreground p-4 md:p-6">
    <div class="mb-3 flex items-center justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold tracking-tight">
          Performance
        </h2>
        <p class="text-sm text-muted-foreground">
          Entwicklung von {{ symbol }} über verschiedene Zeiträume.
        </p>
      </div>

      <div v-if="latestClose !== null" class="text-sm text-muted-foreground text-right">
        Letzter Schlusskurs:
        <span class="font-semibold">
          {{ formatPrice(latestClose) }}
        </span>
      </div>
    </div>

    <!-- Loading / Error -->
    <div v-if="loading" class="flex items-center justify-center py-6 text-sm text-muted-foreground">
      <Loader2 class="mr-2 h-4 w-4 animate-spin" />
      Lade Kursverlauf…
    </div>
    <div v-else-if="error" class="text-sm text-red-500">
      {{ error }}
    </div>
    <div v-else-if="history.length === 0" class="text-sm text-muted-foreground">
      Keine Kursdaten gefunden.
    </div>

    <!-- Performance-Tabelle -->
    <div v-else class="grid grid-cols-2 gap-3 text-sm md:grid-cols-4">
      <div
          v-for="row in performances"
          :key="row.key"
          class="flex flex-col rounded-lg border bg-muted/40 px-3 py-2"
      >
        <span class="text-xs font-medium text-muted-foreground uppercase tracking-wide">
          {{ row.label }}
        </span>
        <span v-if="row.value !== null"
              :class="[
                'mt-1 inline-flex items-center text-sm font-semibold',
                row.value > 0
                  ? 'text-emerald-600 dark:text-emerald-400'
                  : row.value < 0
                    ? 'text-red-600 dark:text-red-400'
                    : 'text-muted-foreground'
              ]">
          <span v-if="row.value > 0">▲</span>
          <span v-else-if="row.value < 0">▼</span>
          <span class="ml-1">
            {{ row.value.toFixed(2) }}%
          </span>
        </span>
        <span v-else class="mt-1 text-xs text-muted-foreground">
          –
        </span>
      </div>
    </div>

    <p class="mt-3 text-xs text-muted-foreground">
      * Performance basiert auf historischen Schlusskursen deiner Stock-History-API.
    </p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from "vue"
import { useRoute } from "vue-router"
import { Loader2 } from "lucide-vue-next"

// Optional: falls du eine Card-Komponente von shadcn-ui hast, kannst du die hier verwenden
// import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card"

interface HistoryPoint {
  id?: number
  date: string              // z.B. "2025-01-01" oder ISO-String
  totalValue?: number       // falls du direkt Portfolio-Werte hast
  close?: number            // falls es reine Stock-Preise sind
}

// Props optional: symbol kann über Route ODER Prop kommen
const props = defineProps<{
  symbol?: string
}>()

const route = useRoute()
const symbol = computed(() => (props.symbol ?? (route.params.symbol as string)))

// State
const history = ref<HistoryPoint[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

// Hilfsfunktion: parse Date
function parseDate(d: string | Date): Date {
  return d instanceof Date ? d : new Date(d)
}

// Annahme: wir verwenden close-Preis. Falls du totalValue benutzt, einfach anpassen.
const sortedHistory = computed(() => {
  return [...history.value]
      .filter(p => p.close != null || p.totalValue != null)
      .sort((a, b) => parseDate(a.date).getTime() - parseDate(b.date).getTime())
})

const latestClose = computed<number | null>(() => {
  const arr = sortedHistory.value
  if (arr.length === 0) return null
  const last = arr[arr.length - 1]
  return (last.close ?? last.totalValue) ?? null
})

// Performance-Berechnung
function computePerformance(daysBack: number | "all"): number | null {
  const arr = sortedHistory.value
  if (arr.length === 0) return null

  const last = arr[arr.length - 1]
  const lastVal = (last.close ?? last.totalValue) ?? null
  if (lastVal == null || lastVal === 0) return null

  let startVal: number | null = null

  if (daysBack === "all") {
    const first = arr[0]
    startVal = (first.close ?? first.totalValue) ?? null
  } else {
    const latestDate = parseDate(last.date)
    const cutoff = new Date(latestDate)
    cutoff.setDate(cutoff.getDate() - daysBack)

    // finde ersten Punkt, der nach dem Cutoff liegt
    let candidate: HistoryPoint | null = null
    for (const p of arr) {
      if (parseDate(p.date).getTime() >= cutoff.getTime()) {
        candidate = p
        break
      }
    }
    // Fallback: wenn nichts gefunden, nimm ersten Punkt
    if (!candidate) candidate = arr[0]
    startVal = (candidate.close ?? candidate.totalValue) ?? null
  }

  if (startVal == null || startVal === 0) return null
  const perf = (lastVal - startVal) / startVal * 100
  return perf
}

const performances = computed(() => {
  return [
    { key: "1d",  label: "1 Tag",         value: computePerformance(1) },
    { key: "1w",  label: "1 Woche",      value: computePerformance(7) },
    { key: "1m",  label: "1 Monat",      value: computePerformance(30) },
    { key: "3m",  label: "3 Monate",     value: computePerformance(90) },
    { key: "6m",  label: "6 Monate",     value: computePerformance(180) },
    { key: "1y",  label: "1 Jahr",       value: computePerformance(365) },
    { key: "3y",  label: "3 Jahre",      value: computePerformance(365 * 3) },
    { key: "all", label: "All Time",     value: computePerformance("all") },
  ]
})

// API-Call
async function fetchHistory() {
  if (!symbol.value) {
    error.value = "Kein Symbol angegeben."
    return
  }

  loading.value = true
  error.value = null

  try {
    const apiUrl = `http://localhost:8080/stocks/${encodeURIComponent(symbol.value)}/history`
    const res = await fetch(apiUrl)
    if (!res.ok) {
      throw new Error(`Fehler beim Laden der History (${res.status})`)
    }
    const data: HistoryPoint[] = await res.json()

    history.value = data
  } catch (e: any) {
    error.value = e?.message ?? "Unbekannter Fehler beim Laden der History"
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHistory()
})

// Formatierung
function formatPrice(value: number): string {
  return new Intl.NumberFormat("de-DE", {
    style: "currency",
    currency: "EUR",
    maximumFractionDigits: 2,
  }).format(value)
}
</script>
