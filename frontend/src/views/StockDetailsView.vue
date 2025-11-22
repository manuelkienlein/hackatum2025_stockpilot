<script setup lang="ts">
import {useRoute} from "vue-router";
import StockChart from "@/components/StockChart.vue";

const route = useRoute()
</script>

<template>
  <StockChart :symbol="route.params.symbol" currency="USD"></StockChart>
</template>

<style scoped>

</style>