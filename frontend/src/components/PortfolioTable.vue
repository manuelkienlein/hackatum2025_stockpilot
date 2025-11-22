<template>
  <div class="w-full rounded-xl border bg-card text-card-foreground p-4 md:p-6">
    <div class="mb-4 flex items-center justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold tracking-tight">Portfolio</h2>
        <p class="text-sm text-muted-foreground">
          Overview of your current stock positions.
        </p>
      </div>
      <span class="text-sm font-medium text-muted-foreground">
        Total value:
        <span class="font-semibold">
          {{ formatCurrency(totalValue, "EUR") }}
        </span>
      </span>
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
        <TableRow
            v-for="position in positionsWithDerived"
            :key="position.symbol"
        >
          <TableCell class="font-mono font-semibold">
            {{ position.symbol }}
          </TableCell>
          <TableCell>
            <div class="flex flex-col">
              <span class="text-sm font-medium">
                {{ position.name }}
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

    <p class="mt-3 text-xs text-muted-foreground">
      * All values are sample data for demo purposes.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

// shadcn-vue table components – Pfad ggf. an dein Setup anpassen
import {
  Table,
  TableHeader,
  TableRow,
  TableHead,
  TableBody,
  TableCell,
} from "@/components/ui/table";

interface Position {
  symbol: string;
  name: string;
  exchange: string;
  currency: string;
  quantity: number;
  avgBuyPrice: number;
  currentPrice: number;
}

interface PositionWithDerived extends Position {
  marketValue: number;
  pnlAbs: number;
  pnlPct: number;
}

const positions: Position[] = [
  {
    symbol: "AAPL",
    name: "Apple Inc.",
    exchange: "NASDAQ",
    currency: "USD",
    quantity: 25,
    avgBuyPrice: 155.2,
    currentPrice: 178.4,
  },
  {
    symbol: "MSFT",
    name: "Microsoft Corp.",
    exchange: "NASDAQ",
    currency: "USD",
    quantity: 10,
    avgBuyPrice: 290.0,
    currentPrice: 310.5,
  },
  {
    symbol: "SAP",
    name: "SAP SE",
    exchange: "XETRA",
    currency: "EUR",
    quantity: 15,
    avgBuyPrice: 118.7,
    currentPrice: 122.3,
  },
  {
    symbol: "VWCE",
    name: "Vanguard FTSE All-World UCITS ETF",
    exchange: "XETRA",
    currency: "EUR",
    quantity: 20,
    avgBuyPrice: 104.5,
    currentPrice: 109.2,
  },
];

const positionsWithDerived = computed<PositionWithDerived[]>(() =>
    positions.map((p) => {
      const marketValue = p.quantity * p.currentPrice;
      const costBasis = p.quantity * p.avgBuyPrice;
      const pnlAbs = marketValue - costBasis;
      const pnlPct = costBasis !== 0 ? (pnlAbs / costBasis) * 100 : 0;

      return {
        ...p,
        marketValue,
        pnlAbs,
        pnlPct,
      };
    }),
);

const totalValue = computed(() =>
    positionsWithDerived.value.reduce(
        (sum, p) => sum + p.marketValue,
        0,
    ),
);

function formatCurrency(value: number, currency: string): string {
  const locale = currency === "EUR" ? "de-DE" : "en-US";

  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(value);
}
</script>
