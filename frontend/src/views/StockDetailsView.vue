<script setup lang="ts">
import {useRoute} from "vue-router";
import StockChart from "@/components/StockChart.vue";

const route = useRoute()
</script>

<template>
Stock {{ route.params.id }}
  <StockChart></StockChart>
</template>

<style scoped>

</style>