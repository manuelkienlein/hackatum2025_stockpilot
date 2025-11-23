<template>
  <div class="w-full rounded-xl border bg-card text-card-foreground p-4 md:p-6">
    <div class="mb-4 flex items-center justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold tracking-tight">Portfolio</h2>
        <p class="text-sm text-muted-foreground">
          Overview of your current stock positions.
        </p>
      </div>

      <div class="flex flex-col items-end gap-1">
        <span class="text-sm font-medium text-muted-foreground">
          Total value:
          <span class="font-semibold">
            {{ formatCurrency(totalValue, "EUR") }}
          </span>
        </span>
        <span v-if="error" class="text-xs text-red-500">
          {{ error }}
        </span>
      </div>
    </div>

    <Table>
      <TableHeader>
        <TableRow>
          <TableHead class="w-[120px]">Symbol</TableHead>
          <TableHead>Company</TableHead>
          <TableHead class="text-right">Quantity</TableHead>
          <TableHead class="text-right">Avg. Buy</TableHead>
          <TableHead class="text-right">Current</TableHead>
          <TableHead class="text-right">Value</TableHead>
          <TableHead class="text-right">P/L</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <TableRow v-if="loading">
          <TableCell colspan="7" class="py-6 text-center text-sm text-muted-foreground">
            Lade Portfolio…
          </TableCell>
        </TableRow>
        <TableRow v-else-if="positionsWithDerived.length === 0">
          <TableCell colspan="7" class="py-6 text-center text-sm text-muted-foreground">
            Keine Positionen vorhanden.
          </TableCell>
        </TableRow>
        <TableRow
            v-else
            v-for="position in positionsWithDerived"
            :key="position.symbol"
        >
          <TableCell class="font-mono font-semibold">
            <RouterLink :to="`/stocks/${position.symbol}`">
              {{ position.symbol }}
            </RouterLink>
          </TableCell>

          <TableCell>
            <div class="flex flex-col">
              <span class="text-sm font-medium">
                <RouterLink :to="`/stocks/${position.symbol}`">
                  {{ position.name }}
                </RouterLink>
              </span>
              <span class="text-xs text-muted-foreground">
                {{ position.exchange }} • {{ position.currency }}
              </span>
            </div>
          </TableCell>

          <TableCell class="text-right">
            {{ position.quantity.toLocaleString() }}
          </TableCell>

          <TableCell class="text-right">
            {{ formatCurrency(position.avgBuyPrice, position.currency) }}
          </TableCell>

          <TableCell class="text-right">
            {{ formatCurrency(position.currentPrice, position.currency) }}
          </TableCell>

          <TableCell class="text-right">
            {{ formatCurrency(position.marketValue, position.currency) }}
          </TableCell>

          <TableCell class="text-right">
            <span
                :class="[
                'inline-flex items-center justify-end text-xs font-semibold',
                position.pnlAbs > 0
                  ? 'text-emerald-600 dark:text-emerald-400'
                  : position.pnlAbs < 0
                    ? 'text-red-600 dark:text-red-400'
                    : 'text-muted-foreground',
              ]"
            >
              <span v-if="position.pnlAbs > 0">▲</span>
              <span v-else-if="position.pnlAbs < 0">▼</span>
              <span class="ml-1">
                {{ formatCurrency(position.pnlAbs, position.currency) }}
                ({{ position.pnlPct.toFixed(2) }}%)
              </span>
            </span>
          </TableCell>
        </TableRow>
      </TableBody>
    </Table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { RouterLink } from "vue-router";

import {
  Table,
  TableHeader,
  TableRow,
  TableHead,
  TableBody,
  TableCell,
} from "@/components/ui/table";

interface ApiPortfolioEntry {
  symbol: string;
  name: string;
  quantity: number;
  avg_buy: number;
  current: number;
  value: number;
  profit_absolut: number;
  profit_percent: number; // in deinem Backend: 0.23 = 23%
  total_spend_for_stocks: number;
}

interface Position {
  symbol: string;
  name: string;
  exchange: string;
  currency: string;
  quantity: number;
  avgBuyPrice: number;
  currentPrice: number;
  marketValue: number;
  pnlAbs: number;
  pnlPct: number; // 23.0 = 23%
}

const positions = ref<Position[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

// API-Basis (ggf. auf '/portfolio' ändern, wenn Frontend über gleichen Origin läuft)
const apiUrl = "http://localhost:8080/portfolio";

async function fetchPortfolio() {
  loading.value = true;
  error.value = null;
  try {
    const res = await fetch(apiUrl);
    if (!res.ok) {
      throw new Error(`Fehler beim Laden des Portfolios (${res.status})`);
    }
    const data: ApiPortfolioEntry[] = await res.json();

    positions.value = data.map((e) => {
      // dein Backend liefert profit_percent als Faktor (z.B. 0.23 für 23%)
      const pnlPct = e.profit_percent * 100;

      return {
        symbol: e.symbol,
        name: e.name,
        // aktuell hast du im Backend keine Exchange/Currency – ggf. später erweitern
        exchange: e.exchange,
        currency: "EUR",
        quantity: e.quantity,
        avgBuyPrice: e.avg_buy,
        currentPrice: e.current,
        marketValue: e.value,
        pnlAbs: e.profit_absolut,
        pnlPct,
      };
    });
  } catch (e: any) {
    error.value = e?.message ?? "Unbekannter Fehler beim Laden des Portfolios";
  } finally {
    loading.value = false;
  }
}

const positionsWithDerived = computed(() => positions.value);

const totalValue = computed(() =>
    positionsWithDerived.value.reduce((sum, p) => sum + p.marketValue, 0),
);

function formatCurrency(value: number, currency: string): string {
  const locale = currency === "EUR" ? "de-DE" : "en-US";

  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(value);
}

onMounted(() => {
  fetchPortfolio();
});
</script>
