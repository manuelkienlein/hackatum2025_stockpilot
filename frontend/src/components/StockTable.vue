<template>
  <div class="w-full rounded-xl border bg-card text-card-foreground p-4 md:p-6">
    <div class="mb-4 flex items-center justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold tracking-tight">
          Stocks
        </h2>
        <p class="text-sm text-muted-foreground">
          List of available stocks in the system.
        </p>
      </div>
      <span class="text-xs text-muted-foreground">
        {{ stocks.length }} items
      </span>
    </div>

    <Table v-if="stocks.length">
      <TableHeader>
        <TableRow>
          <TableHead class="w-[70px]">ID</TableHead>
          <TableHead class="w-[110px]">Symbol</TableHead>
          <TableHead>Name</TableHead>
          <TableHead>Exchange</TableHead>
          <TableHead class="w-[180px]">ISIN</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <TableRow
            v-for="stock in stocks"
            :key="stock.id"
            @click="$router.push(`/stocks/${stock.symbol}`)"
            class="hover:cursor-pointer"
        >
          <TableCell class="text-xs text-muted-foreground">
            {{ stock.id }}
          </TableCell>
          <TableCell class="font-mono font-semibold">
            {{ stock.symbol }}
          </TableCell>
          <TableCell>
            {{ stock.name }}
          </TableCell>
          <TableCell class="text-sm text-muted-foreground">
            {{ stock.exchange }}
          </TableCell>
          <TableCell class="font-mono text-xs">
            {{ stock.isin }}
          </TableCell>
        </TableRow>
      </TableBody>
    </Table>

    <div
        v-else
        class="flex items-center justify-center py-10 text-sm text-muted-foreground"
    >
      No stocks available.
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Table,
  TableHeader,
  TableRow,
  TableHead,
  TableBody,
  TableCell,
} from "@/components/ui/table";
import type { Stock } from "@/services/stockService";

const props = defineProps<{
  stocks: Stock[];
}>();
</script>
