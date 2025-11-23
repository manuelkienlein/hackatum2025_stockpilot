<template>
  <div class="w-full rounded-xl border bg-card text-card-foreground p-4 md:p-6 space-y-4">
    <!-- Header -->
    <div class="flex items-center justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold tracking-tight">
          Bestand & Orders
        </h2>
        <p class="text-sm text-muted-foreground">
          Überblick über deinen aktuellen Bestand und Orders für {{ symbol }}.
        </p>
      </div>
      <div class="text-right text-sm text-muted-foreground">
        <div class="text-xs uppercase tracking-wide">Symbol</div>
        <div class="font-mono font-semibold text-base">
          {{ symbol }}
        </div>
      </div>
    </div>

    <!-- Fehler -->
    <div v-if="error" class="text-sm text-red-500">
      {{ error }}
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-sm text-muted-foreground">
      Lädt Orders…
    </div>

    <!-- Inhalt -->
    <div v-else>
      <!-- Kennzahlen -->
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div class="rounded-lg border bg-muted/30 p-3">
          <div class="text-xs text-muted-foreground uppercase tracking-wide">
            Aktueller Bestand
          </div>
          <div class="mt-1 text-2xl font-semibold">
            {{ position.quantity }}
          </div>
          <div class="text-xs text-muted-foreground">
            Stück
          </div>
        </div>

        <div class="rounded-lg border bg-muted/30 p-3">
          <div class="text-xs text-muted-foreground uppercase tracking-wide">
            Investiertes Kapital
          </div>
          <div class="mt-1 text-2xl font-semibold">
            {{ formatCurrency(position.invested, 'EUR') }}
          </div>
          <div class="text-xs text-muted-foreground">
            Nur ausgeführte Kauforders
          </div>
        </div>

        <div class="rounded-lg border bg-muted/30 p-3">
          <div class="text-xs text-muted-foreground uppercase tracking-wide">
            Durchschnittlicher Kaufkurs
          </div>
          <div class="mt-1 text-2xl font-semibold">
            {{ position.quantity > 0 ? formatCurrency(position.avgBuy, 'EUR') : '–' }}
          </div>
          <div class="text-xs text-muted-foreground">
            basiert auf ausgeführten Käufen
          </div>
        </div>
      </div>

      <!-- Hinweis, wenn kein Bestand -->
      <p
          v-if="position.quantity === 0"
          class="mt-2 text-xs text-muted-foreground"
      >
        Du hältst aktuell keinen Bestand in {{ symbol }} (oder es gibt noch keine ausgeführten Orders).
      </p>

      <!-- Orders Tabelle -->
      <div class="mt-4">
        <div class="mb-2 flex items-center justify-between">
          <h3 class="text-sm font-semibold">
            Orders für {{ symbol }}
          </h3>
          <span class="text-xs text-muted-foreground">
            {{ ordersForSymbol.length }} Orders
          </span>
        </div>

        <div class="rounded-lg border overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Typ</TableHead>
                <TableHead>Side</TableHead>
                <TableHead class="text-right">Menge</TableHead>
                <TableHead class="text-right">Limit / Stop</TableHead>
                <TableHead class="text-right">Ausführung</TableHead>
                <TableHead>Status</TableHead>
                <TableHead class="text-right">Erstellt</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-if="ordersForSymbol.length === 0">
                <TableCell colspan="7" class="py-4 text-center text-sm text-muted-foreground">
                  Noch keine Orders für {{ symbol }} vorhanden.
                </TableCell>
              </TableRow>

              <TableRow
                  v-else
                  v-for="order in ordersForSymbolSorted"
                  :key="order.id"
              >
                <TableCell>
                  <span class="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    {{ order.type }}
                  </span>
                </TableCell>
                <TableCell>
                  <span
                      :class="[
                      'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold',
                      order.side === 'BUY'
                        ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-300'
                        : 'bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-300',
                    ]"
                  >
                    {{ order.side }}
                  </span>
                </TableCell>
                <TableCell class="text-right">
                  {{ order.quantity }}
                </TableCell>
                <TableCell class="text-right text-xs">
                  <div class="flex flex-col items-end">
                    <span v-if="order.limitPrice != null">
                      Limit: {{ order.limitPrice }}
                    </span>
                    <span v-if="order.stopPrice != null" class="text-muted-foreground">
                      Stop: {{ order.stopPrice }}
                    </span>
                    <span v-if="order.limitPrice == null && order.stopPrice == null" class="text-muted-foreground">
                      –
                    </span>
                  </div>
                </TableCell>
                <TableCell class="text-right text-xs">
                  <div class="flex flex-col items-end text-muted-foreground">
                    <span v-if="order.executedPrice != null">
                      {{ order.executedPrice }}
                    </span>
                    <span v-if="order.executedAt">
                      {{ formatDate(order.executedAt) }}
                    </span>
                    <span v-else-if="order.status === 'PENDING'">
                      offen
                    </span>
                  </div>
                </TableCell>
                <TableCell>
                  <span
                      :class="[
                      'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold',
                      statusColorClass(order.status),
                    ]"
                  >
                    <span
                        class="mr-1 inline-block h-1.5 w-1.5 rounded-full"
                        :class="statusDotClass(order.status)"
                    />
                    {{ order.status }}
                  </span>
                </TableCell>
                <TableCell class="text-right text-xs text-muted-foreground">
                  {{ formatDate(order.createdAt) }}
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from "vue"
import { useRoute } from "vue-router"

import {
  Table,
  TableHeader,
  TableRow,
  TableHead,
  TableBody,
  TableCell,
} from "@/components/ui/table"
import {query} from "happy-dom/lib/PropertySymbol.d.ts.js";

// Types passend zu deinem OrderEntity JSON
type OrderSide = "BUY" | "SELL"
type OrderType = "MARKET" | "LIMIT" | "STOP" | "STOP_LIMIT"
type OrderStatus = "PENDING" | "EXECUTED" | "CANCELLED"

interface Order {
  id: number
  symbol: string
  side: OrderSide
  type: OrderType
  quantity: number
  limitPrice: number | null
  stopPrice: number | null
  createdAt: string
  status: OrderStatus
  executedPrice: number | null
  executedAt: string | null
  executedQuantity: number | null
}

interface PositionInfo {
  quantity: number
  invested: number
  avgBuy: number
}

const route = useRoute()
const symbol = computed(() => String(route.params.symbol || "").toUpperCase())

const orders = ref<Order[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const apiBase = "http://localhost:8080/orders" // ggf. anpassen

// Nur Orders für dieses Symbol
const ordersForSymbol = computed(() =>
    orders.value.filter((o) => o.symbol?.toUpperCase() === symbol.value)
)

// Nach Datum sortiert (neueste zuerst)
const ordersForSymbolSorted = computed(() =>
    [...ordersForSymbol.value].sort((a, b) => {
      const da = new Date(a.createdAt).getTime()
      const db = new Date(b.createdAt).getTime()
      return db - da
    })
)

// Bestand & Kennzahlen aus ausgeführten Orders
const position = computed<PositionInfo>(() => {
  let qty = 0
  let buyQty = 0
  let invested = 0

  for (const o of ordersForSymbol.value) {
    if (o.status !== "EXECUTED") {
      continue
    }

    const signedQty = o.side === "BUY" ? o.executedQuantity : -o.executedQuantity
    qty += signedQty

    if (o.side === "BUY") {
      buyQty += signedQty
      console.log("o.executedPrice", o.executedPrice)
      console.log("o.executedQuanrity", o.executedQuantity)
      invested += o.executedPrice * o.executedQuantity
    }
    // Für einfaches MVP ignorieren wir hier die "Rückzahlung" von SELLs auf die Cost-Basis
  }

  const avgBuy = qty > 0 ? invested / buyQty : 0
  console.log(invested + " : " + buyQty)

  return {
    quantity: qty,
    invested,
    avgBuy,
  }
})

async function fetchOrders() {
  loading.value = true
  error.value = null
  try {
    const res = await fetch(apiBase)
    if (!res.ok) {
      throw new Error(`Fehler beim Laden der Orders (${res.status})`)
    }
    const data: Order[] = await res.json()
    orders.value = data
  } catch (e: any) {
    error.value = e?.message ?? "Unbekannter Fehler beim Laden der Orders"
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrders()
})

// Helper: Status-Styling (ähnlich wie in deinen Tabellen)
function statusColorClass(status: OrderStatus): string {
  switch (status) {
    case "PENDING":
      return "bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-300"
    case "EXECUTED":
      return "bg-emerald-50 text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-300"
    case "CANCELLED":
      return "bg-slate-100 text-slate-600 dark:bg-slate-800/50 dark:text-slate-300"
    default:
      return "bg-slate-100 text-slate-600 dark:bg-slate-800/50 dark:text-slate-300"
  }
}

function statusDotClass(status: OrderStatus): string {
  switch (status) {
    case "PENDING":
      return "bg-amber-500"
    case "EXECUTED":
      return "bg-emerald-500"
    case "CANCELLED":
      return "bg-slate-400"
    default:
      return "bg-slate-400"
  }
}

function formatDate(value: string | null | undefined): string {
  if (!value) return ""
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleString("de-DE", {
    day: "2-digit",
    month: "2-digit",
    year: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  })
}

function formatCurrency(value: number, currency: string): string {
  const locale = currency === "EUR" ? "de-DE" : "en-US"
  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(value)
}
</script>
