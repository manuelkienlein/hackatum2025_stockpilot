<template>
  <div class="w-full rounded-xl border bg-card text-card-foreground p-4 md:p-6">
    <h2 class="text-lg font-semibold tracking-tight mb-4">Cash vs. Stocks</h2>

    <div class="h-64 md:h-80">
      <canvas ref="canvasRef"></canvas>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from "vue"
import {
  Chart,
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend,
} from "chart.js"

Chart.register(BarController, BarElement, CategoryScale, LinearScale, Tooltip, Legend)

interface NetWorthPoint {
  date: string        // "2025-01-01"
  cashValue: number   // echte Beträge
  investedValue: number
}

const props = defineProps<{
  data: NetWorthPoint[]
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
let chart: Chart<"bar"> | null = null

function createChart() {
  if (!canvasRef.value) return
  const ctx = canvasRef.value.getContext("2d")
  if (!ctx) return

  chart = new Chart(ctx, {
    type: "bar",
    data: {
      labels: props.data.map((p) =>
          new Date(p.date).toLocaleDateString("de-DE", { month: "short", year: "numeric" })
      ),
      datasets: [
        {
          label: "Cash",
          data: props.data.map((p) => p.cashValue),
          backgroundColor: "rgba(99, 102, 241, 0.8)",  // Indigo
          borderColor: "rgba(79, 70, 229, 1)",
          borderWidth: 1,
          stack: "total",
        },
        {
          label: "Stocks",
          data: props.data.map((p) => p.investedValue),
          backgroundColor: "rgba(16, 185, 129, 0.8)", // Emerald
          borderColor: "rgba(5, 150, 105, 1)",
          borderWidth: 1,
          stack: "total",
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          stacked: true,
        },
        y: {
          stacked: true,
          beginAtZero: true,
          ticks: {
            callback: (value) =>
                new Intl.NumberFormat("de-DE", {
                  style: "currency",
                  currency: "EUR",
                  maximumFractionDigits: 0,
                }).format(Number(value)),
          },
        },
      },
      plugins: {
        tooltip: {
          callbacks: {
            label(context) {
              return `${context.dataset.label}: ${new Intl.NumberFormat("de-DE", {
                style: "currency",
                currency: "EUR",
                maximumFractionDigits: 0,
              }).format(Number(context.raw))}`
            },
          },
        },
      },
    },
  })
}

function destroyChart() {
  if (chart) {
    chart.destroy()
    chart = null
  }
}

watch(
    () => props.data,
    () => {
      if (!chart) createChart()
      else {
        chart.data.labels = props.data.map((p) =>
            new Date(p.date).toLocaleDateString("de-DE", { month: "short", year: "numeric" })
        )
        chart.data.datasets[0].data = props.data.map((p) => p.cashValue)
        chart.data.datasets[1].data = props.data.map((p) => p.investedValue)
        chart.update()
      }
    },
    { deep: true }
)

onMounted(createChart)
onBeforeUnmount(destroyChart)
</script>
