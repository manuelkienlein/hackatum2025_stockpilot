<template>
  <div class="w-full rounded-xl border bg-card text-card-foreground p-4 md:p-6">
    <div class="mb-4 flex items-center justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold tracking-tight">
          Orders
        </h2>
        <p class="text-sm text-muted-foreground">
          Simulated buy / sell / limit orders for your portfolio.
        </p>
      </div>
      <span class="text-xs text-muted-foreground">
        {{ orders.length }} orders total
      </span>
    </div>

    <Table>
      <TableHeader>
        <TableRow>
          <TableHead class="w-[90px]">Symbol</TableHead>
          <TableHead>Type</TableHead>
          <TableHead>Side</TableHead>
          <TableHead class="text-right">Qty</TableHead>
          <TableHead class="text-right">Limit / Exec</TableHead>
          <TableHead>Status</TableHead>
          <TableHead class="text-right">Placed</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <TableRow
            v-for="order in orders"
            :key="order.id"
        >
          <TableCell class="font-mono font-semibold">
            {{ order.symbol }}
          </TableCell>
          <TableCell>
            <span class="text-xs font-medium uppercase tracking-wide text-muted-foreground">
              {{ order.orderType }}
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
            {{ order.quantity.toLocaleString() }}
          </TableCell>
          <TableCell class="text-right">
            <div class="flex flex-col items-end">
              <span v-if="order.orderType === 'LIMIT'" class="text-xs text-muted-foreground">
                Limit: {{ formatCurrency(order.limitPrice!, order.currency) }}
              </span>
              <span v-if="order.executedPrice !== null" class="text-xs">
                Exec: {{ formatCurrency(order.executedPrice, order.currency) }}
              </span>
              <span v-else class="text-xs text-muted-foreground">
                –
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
            {{ formatDate(order.placedAt) }}
          </TableCell>
        </TableRow>
      </TableBody>
    </Table>

    <p class="mt-3 text-xs text-muted-foreground">
      * Orders are static sample data. Connect this component to your API to display real portfolio orders.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import {
  Table,
  TableHeader,
  TableRow,
  TableHead,
  TableBody,
  TableCell,
} from "@/components/ui/table";

type OrderSide = "BUY" | "SELL";
type OrderType = "MARKET" | "LIMIT";
type OrderStatus = "OPEN" | "PARTIALLY_FILLED" | "FILLED" | "CANCELLED";

interface Order {
  id: string;
  symbol: string;
  side: OrderSide;
  orderType: OrderType;
  status: OrderStatus;
  quantity: number;
  limitPrice: number | null;
  executedPrice: number | null;
  currency: string;
  placedAt: string; // ISO string
}

const orders: Order[] = [
  {
    id: "1",
    symbol: "AAPL",
    side: "BUY",
    orderType: "LIMIT",
    status: "OPEN",
    quantity: 10,
    limitPrice: 170.0,
    executedPrice: null,
    currency: "USD",
    placedAt: "2025-11-21T10:15:00Z",
  },
  {
    id: "2",
    symbol: "MSFT",
    side: "SELL",
    orderType: "MARKET",
    status: "FILLED",
    quantity: 5,
    limitPrice: null,
    executedPrice: 310.5,
    currency: "USD",
    placedAt: "2025-11-20T14:32:00Z",
  },
  {
    id: "3",
    symbol: "SAP",
    side: "BUY",
    orderType: "LIMIT",
    status: "PARTIALLY_FILLED",
    quantity: 20,
    limitPrice: 120.0,
    executedPrice: 119.4,
    currency: "EUR",
    placedAt: "2025-11-19T09:05:00Z",
  },
  {
    id: "4",
    symbol: "VWCE",
    side: "BUY",
    orderType: "MARKET",
    status: "CANCELLED",
    quantity: 15,
    limitPrice: null,
    executedPrice: null,
    currency: "EUR",
    placedAt: "2025-11-18T16:47:00Z",
  },
];

function formatDate(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleString("de-DE", {
    day: "2-digit",
    month: "2-digit",
    year: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function formatCurrency(value: number, currency: string): string {
  const locale = currency === "EUR" ? "de-DE" : "en-US";
  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(value);
}

function statusColorClass(status: OrderStatus): string {
  switch (status) {
    case "OPEN":
      return "bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-300";
    case "PARTIALLY_FILLED":
      return "bg-sky-50 text-sky-700 dark:bg-sky-900/20 dark:text-sky-300";
    case "FILLED":
      return "bg-emerald-50 text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-300";
    case "CANCELLED":
      return "bg-slate-100 text-slate-600 dark:bg-slate-800/50 dark:text-slate-300";
    default:
      return "bg-slate-100 text-slate-600 dark:bg-slate-800/50 dark:text-slate-300";
  }
}

function statusDotClass(status: OrderStatus): string {
  switch (status) {
    case "OPEN":
      return "bg-amber-500";
    case "PARTIALLY_FILLED":
      return "bg-sky-500";
    case "FILLED":
      return "bg-emerald-500";
    case "CANCELLED":
      return "bg-slate-400";
    default:
      return "bg-slate-400";
  }
}
</script>
