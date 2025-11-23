<template>
  <div class="w-full rounded-xl border bg-card text-card-foreground p-4 md:p-6">
    <!-- Header -->
    <div class="mb-4 flex items-center justify-between gap-2">
      <div class="flex flex-col gap-1">
        <div class="flex items-center gap-2">
          <span class="font-mono text-sm px-2 py-0.5 rounded-full bg-muted">
            {{ symbol.toUpperCase() }}
          </span>
          <span v-if="stock?.exchange" class="text-xs text-muted-foreground uppercase">
            {{ stock.exchange }}
          </span>
        </div>

        <h2 class="text-lg font-semibold tracking-tight">
          <span v-if="stock">{{ stock.name || symbol.toUpperCase() }}</span>
          <span v-else>Loading …</span>
        </h2>

        <p class="text-xs text-muted-foreground">
          ISIN:
          <span class="font-mono">{{ stock?.isin || "–" }}</span>
        </p>
      </div>

      <!-- Preis / Performance -->
      <div v-if="stock" class="text-right space-y-1">
        <div v-if="hasChange" class="text-xs font-medium">
          <span
              :class="[
              'inline-flex items-center rounded-full px-2 py-0.5',
              (stock.changeAbs ?? 0) > 0
                ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-300'
                : (stock.changeAbs ?? 0) < 0
                  ? 'bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-300'
                  : 'bg-slate-100 text-slate-600 dark:bg-slate-800/50 dark:text-slate-300',
            ]"
          >
            <span v-if="(stock.changeAbs ?? 0) > 0">▲</span>
            <span v-else-if="(stock.changeAbs ?? 0) < 0">▼</span>
            <span class="ml-1">
              {{ (stock.changeAbs ?? 0).toFixed(2) }}
              ({{ (stock.changePct ?? 0).toFixed(2) }}%)
            </span>
          </span>
          <div v-if="stock.previousClose != null" class="text-[10px] text-muted-foreground mt-0.5">
            Vortag: {{ formatCurrency(stock.previousClose, stock.currency || "EUR") }}
          </div>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 text-sm">
      <div class="space-y-1">
        <h3 class="text-xs font-medium uppercase text-muted-foreground">Basisdaten</h3>
        <div class="flex justify-between">
          <span class="text-muted-foreground">Symbol</span>
          <span class="font-mono">{{ stock?.symbol || symbol.toUpperCase() }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-muted-foreground">Name</span>
          <span class="text-right truncate max-w-[180px]">
            {{ stock?.name || "–" }}
          </span>
        </div>
        <div class="flex justify-between">
          <span class="text-muted-foreground">Börse</span>
          <span>{{ stock?.exchange || "–" }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-muted-foreground">ISIN</span>
          <span class="font-mono">{{ stock?.isin || "–" }}</span>
        </div>
      </div>

      <div class="space-y-1">
        <h3 class="text-xs font-medium uppercase text-muted-foreground">
          Kursinformationen
        </h3>
        <div class="flex justify-between">
          <span class="text-muted-foreground">Währung</span>
          <span>{{ stock?.currency || "EUR" }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-muted-foreground">Letzter Schluss</span>
          <span>
            <span v-if="stock?.previousClose != null">
              {{ formatCurrency(stock.previousClose, stock.currency || "EUR") }}
            </span>
            <span v-else>–</span>
          </span>
        </div>
        <div class="flex justify-between">
          <span class="text-muted-foreground">Tageshoch</span>
          <span>
            <span v-if="stock?.high != null">
              {{ formatCurrency(stock.high, stock.currency || "EUR") }}
            </span>
            <span v-else>–</span>
          </span>
        </div>
        <div class="flex justify-between">
          <span class="text-muted-foreground">Tagestief</span>
          <span>
            <span v-if="stock?.low != null">
              {{ formatCurrency(stock.low, stock.currency || "EUR") }}
            </span>
            <span v-else>–</span>
          </span>
        </div>
      </div>

      <div class="space-y-1">
        <h3 class="text-xs font-medium uppercase text-muted-foreground">
          Sonstiges
        </h3>
        <div class="flex justify-between">
          <span class="text-muted-foreground">Volumen</span>
          <span>
            <span v-if="stock?.volume != null">
              {{ stock.volume.toLocaleString() }}
            </span>
            <span v-else>–</span>
          </span>
        </div>
        <!-- Platz für weitere Felder aus der API -->
      </div>
    </div>

    <!-- Loading / Error -->
    <div v-if="loading" class="mt-4 text-xs text-muted-foreground">
      Lade Kursdaten …
    </div>
    <div v-else-if="error" class="mt-4 text-xs text-red-500">
      {{ error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, watch } from "vue"
import { useRoute } from "vue-router"

// Falls du shadcn-Komponenten hier brauchst, könntest du z.B.
// eine Card oder Badge importieren – aktuell mache ich alles mit Tailwind-Utility-Klassen.

// ---- Types ----

// Dieses Interface kannst du an deine echte API anpassen.
// Ich habe alle zusätzlichen Felder optional gemacht.
interface StockDetail {
  id?: number
  symbol: string
  name?: string
  exchange?: string
  isin?: string
  currency?: string

  // optionale Felder, falls du sie vom Backend mitlieferst
  currentPrice?: number
  previousClose?: number
  changeAbs?: number
  changePct?: number
  high?: number
  low?: number
  volume?: number
}

// ---- Routing / State ----

const route = useRoute()
const symbol = computed(() => (route.params.symbol as string || "").toUpperCase())

const stock = ref<StockDetail | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

// API-Basis-URL an dein Backend anpassen:
const apiBase = "http://localhost:8080"

// ---- Computed ----

const hasChange = computed(
    () =>
        stock.value != null &&
        (stock.value.changeAbs != null || stock.value.changePct != null)
)

// ---- Fetch ----

async function fetchStock() {
  if (!symbol.value) return
  loading.value = true
  error.value = null
  stock.value = null

  try {
    // Beispiel-Endpoint: /stocks/{symbol}
    const res = await fetch(`${apiBase}/stocks/${encodeURIComponent(symbol.value)}`)
    if (!res.ok) {
      throw new Error(`Fehler beim Laden des Stocks (${res.status})`)
    }

    const data = await res.json()
    // Hier nehme ich an, dass der Endpoint direkt ein StockDetail-ähnliches Objekt zurückgibt
    stock.value = data as StockDetail
  } catch (e: any) {
    error.value = e?.message ?? "Fehler beim Laden der Stock-Daten"
  } finally {
    loading.value = false
  }
}

onMounted(fetchStock)

// Wenn sich das Symbol in der URL ändert (z.B. durch Routing), neu laden
watch(symbol, () => {
  fetchStock()
})

// ---- Helpers ----

function formatCurrency(value: number, currency: string): string {
  const locale = currency === "EUR" ? "de-DE" : "en-US"

  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency,
    maximumFractionDigits: 2
  }).format(value)
}
</script>
