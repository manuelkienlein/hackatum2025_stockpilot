<template>
  <div class="w-full rounded-xl border bg-card text-card-foreground p-4 md:p-6">
    <div class="mb-4 flex items-center justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold tracking-tight">
          Net Worth
        </h2>
        <p class="text-sm text-muted-foreground">
          Development of your net worth over the last year.
        </p>
      </div>

      <div class="text-right">
        <div class="text-sm text-muted-foreground">
          Letzter Wert:
          <span class="font-semibold">
        {{ formatCurrency(latestValue, "EUR") }}
      </span>
        </div>

        <div
            class="text-sm font-semibold"
            :class="performanceAbs >= 0 ? 'text-emerald-600' : 'text-red-600'"
        >
          <span v-if="performanceAbs >= 0">▲</span>
          <span v-else>▼</span>

          {{ formatCurrency(performanceAbs, 'EUR') }}
          ({{ performancePct.toFixed(2) }}%)
        </div>
      </div>
    </div>

    <div class="h-64 md:h-80">
      <canvas ref="canvasRef"></canvas>
    </div>

    <p class="mt-3 text-xs text-muted-foreground">
      * This chart is updated on a monthly basis.
    </p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, computed, watch } from "vue"
import {
  Chart,
  type ChartData,
  type ChartOptions,
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  TimeScale,
  Tooltip,
  Legend,
  Filler,
  CategoryScale,
} from "chart.js"

// WICHTIG: Time-Scale & Adapter
import "chartjs-adapter-date-fns"
import { de } from "date-fns/locale"

// Chart.js-Controller registrieren
Chart.register(
    LineController,
    LineElement,
    PointElement,
    LinearScale,
    TimeScale,
    Tooltip,
    Legend,
    Filler,
    CategoryScale
)

// ---- Props (optional: später echte Daten vom Backend übergeben) ----

interface PortfolioPoint {
  date: string | Date // ISO-String oder Date
  value: number       // Gesamtvermögenswert
}

const props = defineProps<{
  data?: PortfolioPoint[]
}>()

// Demo-Daten fallback, falls noch nichts vom Backend kommt
const demoData: PortfolioPoint[] = [
  { date: "2025-11-18T10:00:00Z", value: 12000 },
  { date: "2025-11-19T10:00:00Z", value: 12350 },
  { date: "2025-11-20T10:00:00Z", value: 12800 },
  { date: "2025-11-21T10:00:00Z", value: 12650 },
  { date: "2025-11-22T10:00:00Z", value: 13120 },
  { date: "2025-11-23T10:00:00Z", value: 13480 },
]

const points = computed<PortfolioPoint[]>(() =>
    props.data && props.data.length > 0 ? props.data : demoData
)

const latestValue = computed(() => {
  if (points.value.length === 0) return 0
  return points.value[points.value.length - 1].value
})

const firstValue = computed(() => {
  if (points.value.length === 0) return 0
  return points.value[0].value
})

const performanceAbs = computed(() => {
  return latestValue.value - firstValue.value
})

const performancePct = computed(() => {
  if (firstValue.value === 0) return 0
  return (performanceAbs.value / firstValue.value) * 100
})

// ---- Chart Setup ----

const canvasRef = ref<HTMLCanvasElement | null>(null)
let chart: Chart<"line"> | null = null

function createChart() {
  if (!canvasRef.value) return

  const ctx = canvasRef.value.getContext("2d")
  if (!ctx) return

  const chartData: ChartData<"line"> = {
    labels: points.value.map((p) => p.date),
    datasets: [
      {
        label: "Portfolio-Wert",
        data: points.value.map((p) => p.value),
        fill: true,
        tension: 0.25,
        borderWidth: 2,
        backgroundColor: "rgba(16, 185, 129, 0.7)", // grün
        borderColor: "rgb(16, 185, 129)",
      },
    ],
  }

  const options: ChartOptions<"line"> = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      mode: "index",
      intersect: false,
    },
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        callbacks: {
          label(context) {
            const value = context.parsed.y
            return ` ${formatCurrency(value, "EUR")}`
          },
        },
      },
    },
    scales: {
      x: {
        type: "time",
        time: {
          unit: "month",
          tooltipFormat: "MMM yyyy",
          displayFormats: {
            month: "MMM yyyy",
          },
        },
        adapters: {
          date: {
            locale: de,
          },
        },
        ticks: {
          maxRotation: 0,
          autoSkipPadding: 16,
        },
        grid: {
          display: false,
        },
      },
      y: {
        beginAtZero: true,
        ticks: {
          callback(value) {
            return formatCurrency(Number(value), "EUR")
          },
        },
      },
    },
  }

  chart = new Chart(ctx, {
    type: "line",
    data: chartData,
    options,
  })
}

function destroyChart() {
  if (chart) {
    chart.destroy()
    chart = null
  }
}

// Wenn sich die Daten (props.data) ändern → Chart updaten
watch(
    points,
    () => {
      if (!chart) {
        createChart()
      } else {
        chart.data.labels = points.value.map((p) => p.date)
        chart.data.datasets[0].data = points.value.map((p) => p.value)
        chart.update()
      }
    },
    { deep: true }
)

onMounted(() => {
  createChart()
})

onBeforeUnmount(() => {
  destroyChart()
})

// ---- Helfer für Währung ----

function formatCurrency(value: number, currency: string): string {
  const locale = currency === "EUR" ? "de-DE" : "en-US"

  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(value)
}
</script>
