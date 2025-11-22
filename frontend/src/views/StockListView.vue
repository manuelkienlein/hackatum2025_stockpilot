<template>
  <div class="mx-auto flex max-w-5xl flex-col gap-4 px-4 py-6">
    <header class="flex items-center justify-between">
      <h1 class="text-xl font-semibold tracking-tight">
        Stock List
      </h1>
      <button
          type="button"
          class="text-sm text-muted-foreground hover:text-foreground underline-offset-4 hover:underline"
          @click="reload"
          :disabled="isLoading"
      >
        Reload
      </button>
    </header>

    <div v-if="error" class="rounded-md border border-red-500/40 bg-red-500/5 p-3 text-sm text-red-600">
      {{ error }}
    </div>

    <div v-if="isLoading" class="text-sm text-muted-foreground">
      Loading stocks…
    </div>

    <StockTable
        v-if="!isLoading && !error"
        :stocks="stocks"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import StockTable from "@/components/StockTable.vue";
import {getStocks, type Stock} from "@/service/stockService.ts";

const stocks = ref<Stock[]>([]);
const isLoading = ref(false);
const error = ref<string | null>(null);

async function reload() {
  isLoading.value = true;
  error.value = null;
  try {
    stocks.value = await getStocks();
  } catch (e: any) {
    console.error(e);
    error.value = e?.message ?? "Failed to load stocks";
  } finally {
    isLoading.value = false;
  }
}

onMounted(() => {
  reload();
});
</script>