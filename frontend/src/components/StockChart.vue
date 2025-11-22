<template>
  <div class="w-full rounded-xl border bg-card text-card-foreground p-4 md:p-6">
    <div class="mb-4 flex items-center justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold tracking-tight">
          {{ symbol }} Stock Chart
        </h2>
        <p class="text-sm text-muted-foreground">
          {{ formattedDateRange }} • Close price ({{ currency }})
        </p>
      </div>
      <div v-if="latestPoint" class="text-right">
        <div class="text-sm text-muted-foreground">Last price</div>
        <div class="text-lg font-semibold">
          {{ formatCurrency(latestPoint.close, currency) }}
        </div>
        <div
            class="text-xs font-medium"
            :class="trendChange >= 0
            ? 'text-emerald-600 dark:text-emerald-400'
            : 'text-red-600 dark:text-red-400'"
        >
          <span v-if="trendChange >= 0">▲</span>
          <span v-else>▼</span>
          {{ trendChange.toFixed(2) }}%
        </div>
      </div>
    </div>

    <div
        class="relative h-56 w-full select-none"
        @mousemove="onMouseMove"
        @mouseleave="onMouseLeave"
        ref="chartContainer"
    >
      <svg
          v-if="normalizedPoints.length > 1"
          :viewBox="`0 0 ${width} ${height}`"
          class="h-full w-full"
          preserveAspectRatio="none"
      >
        <!-- Grid lines -->
        <g class="stroke-muted" stroke-width="0.5" stroke-linecap="round">
          <line
              v-for="(y, i) in gridLinesY"
              :key="`grid-${i}`"
              :x1="0"
              :x2="width"
              :y1="y"
              :y2="y"
              stroke-dasharray="2 2"
          />
        </g>

        <!-- Area under line -->
        <path
            :d="areaPath"
            fill="currentColor"
            class="opacity-10"
        />

        <!-- Price line -->
        <path
            :d="linePath"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
        />

        <!-- Hover line + dot -->
        <g v-if="hoverPoint">
          <line
              :x1="hoverPoint.x"
              :x2="hoverPoint.x"
              y1="0"
              :y2="height"
              class="stroke-muted"
              stroke-width="0.8"
              stroke-dasharray="3 3"
          />
          <circle
              :cx="hoverPoint.x"
              :cy="hoverPoint.y"
              r="4"
              class="fill-background stroke-primary"
              stroke-width="2"
          />
        </g>
      </svg>

      <!-- Tooltip -->
      <div
          v-if="hoverPoint"
          class="pointer-events-none absolute rounded-md border bg-popover px-3 py-2 text-xs shadow-lg"
          :style="tooltipStyle"
      >
        <div class="font-medium">
          {{ formatCurrency(hoverPoint.close, currency) }}
        </div>
        <div class="text-muted-foreground">
          {{ formatDate(hoverPoint.date) }}
        </div>
      </div>

      <div
          v-else-if="normalizedPoints.length <= 1"
          class="flex h-full items-center justify-center text-xs text-muted-foreground"
      >
        Not enough data to render chart.
      </div>
    </div>

    <p class="mt-3 text-xs text-muted-foreground">
      * Sample data for demo purposes. Replace with live API data in your app.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";

interface StockPoint {
  date: string | Date;
  close: number;
}

// Props mit Fallback-Daten
const props = withDefaults(
    defineProps<{
      symbol?: string;
      currency?: string;
      data?: StockPoint[];
    }>(),
    {
      symbol: "AAPL",
      currency: "USD",
      data: () => [
        { date: "2025-11-13", close: 168.2 },
        { date: "2025-11-14", close: 169.8 },
        { date: "2025-11-17", close: 167.5 },
        { date: "2025-11-18", close: 171.1 },
        { date: "2025-11-19", close: 173.4 },
        { date: "2025-11-20", close: 172.0 },
        { date: "2025-11-21", close: 174.7 },
      ],
    },
);

const symbol = computed(() => props.symbol);
const currency = computed(() => props.currency);
const rawData = computed(() =>
    [...props.data].sort(
        (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime(),
    ),
);

const width = 1000;
const height = 300;

interface NormalizedPoint {
  x: number;
  y: number;
  date: Date;
  close: number;
}

const normalizedPoints = computed<NormalizedPoint[]>(() => {
  if (!rawData.value.length) return [];

  const dates = rawData.value.map((d) => new Date(d.date));
  const prices = rawData.value.map((d) => d.close);

  const minPrice = Math.min(...prices);
  const maxPrice = Math.max(...prices);
  const priceRange = maxPrice - minPrice || 1;

  const n = rawData.value.length;
  const stepX = n > 1 ? width / (n - 1) : 0;

  return rawData.value.map((d, idx) => {
    const date = new Date(d.date);
    const x = stepX * idx;
    const norm =
        priceRange === 0 ? 0.5 : (d.close - minPrice) / priceRange; // 0..1
    const y = height - norm * (height - 10) - 5; // padding
    return { x, y, date, close: d.close };
  });
});

const linePath = computed(() => {
  if (normalizedPoints.value.length === 0) return "";
  return normalizedPoints.value
      .map((p, i) => `${i === 0 ? "M" : "L"} ${p.x} ${p.y}`)
      .join(" ");
});

const areaPath = computed(() => {
  if (normalizedPoints.value.length === 0) return "";
  const first = normalizedPoints.value[0];
  const last = normalizedPoints.value[normalizedPoints.value.length - 1];
  const line = normalizedPoints.value
      .map((p, i) => `${i === 0 ? "M" : "L"} ${p.x} ${p.y}`)
      .join(" ");

  return `${line} L ${last.x} ${height} L ${first.x} ${height} Z`;
});

const gridLinesY = computed(() => {
  const lines = 3;
  return Array.from({ length: lines }, (_, i) => {
    const t = i / (lines - 1 || 1);
    return 5 + t * (height - 10);
  });
});

const latestPoint = computed(() =>
    normalizedPoints.value[normalizedPoints.value.length - 1],
);

const trendChange = computed(() => {
  const pts = normalizedPoints.value;
  if (pts.length < 2) return 0;
  const first = rawData.value[0].close;
  const last = rawData.value[rawData.value.length - 1].close;
  return ((last - first) / first) * 100;
});

const formattedDateRange = computed(() => {
  if (!rawData.value.length) return "";
  const first = rawData.value[0].date;
  const last = rawData.value[rawData.value.length - 1].date;
  return `${formatDate(first)} – ${formatDate(last)}`;
});

function formatDate(d: string | Date): string {
  const date = d instanceof Date ? d : new Date(d);
  return date.toLocaleDateString("de-DE", {
    day: "2-digit",
    month: "2-digit",
    year: "2-digit",
  });
}

function formatCurrency(value: number, curr: string): string {
  const locale = curr === "EUR" ? "de-DE" : "en-US";
  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency: curr,
    maximumFractionDigits: 2,
  }).format(value);
}

// Hover / Tooltip
const chartContainer = ref<HTMLElement | null>(null);
const hoverPoint = ref<NormalizedPoint | null>(null);
const tooltipStyle = computed(() => {
  if (!hoverPoint.value || !chartContainer.value) return {};
  const rect = chartContainer.value.getBoundingClientRect();

  const relativeX = (hoverPoint.value.x / width) * rect.width;
  const relativeY = (hoverPoint.value.y / height) * rect.height;

  const left = Math.min(
      Math.max(relativeX + 8, 8),
      rect.width - 140,
  );
  const top = Math.min(
      Math.max(relativeY - 40, 8),
      rect.height - 40,
  );

  return {
    left: `${left}px`,
    top: `${top}px`,
  };
});

function onMouseMove(event: MouseEvent) {
  if (!chartContainer.value || !normalizedPoints.value.length) return;

  const rect = chartContainer.value.getBoundingClientRect();
  const relX = event.clientX - rect.left;
  const ratioX = relX / rect.width;
  const targetX = ratioX * width;

  // Nächsten Punkt finden
  let closest: NormalizedPoint | null = null;
  let minDist = Infinity;
  for (const p of normalizedPoints.value) {
    const dist = Math.abs(p.x - targetX);
    if (dist < minDist) {
      minDist = dist;
      closest = p;
    }
  }
  hoverPoint.value = closest;
}

function onMouseLeave() {
  hoverPoint.value = null;
}

onMounted(() => {
  // nothing special, but ensures ref is ready
});
</script>
