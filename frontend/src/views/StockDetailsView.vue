<script setup lang="ts">
import {useRoute} from "vue-router";
import StockChart from "@/components/StockChart.vue";
import StockOverviewCard from "@/components/StockOverviewCard.vue";
import StockPerformanceCard from "@/components/StockPerformanceCard.vue";

const route = useRoute()
</script>

<template>
  <StockChart :symbol="route.params.symbol" currency="USD"></StockChart>
  <StockOverviewCard class="mt-5"></StockOverviewCard>
  <StockPerformanceCard class="mt-5" :symbol="route.params.symbol" />
</template>

<style scoped>

</style>