<template>
  <div class="w-full rounded-xl border bg-card text-card-foreground p-4 md:p-6 space-y-4">
    <!-- Header -->
    <div class="mb-2 flex items-center justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold tracking-tight">
          Watchlist
        </h2>
        <p class="text-sm text-muted-foreground">
          Stocks you are keeping an eye on with live prices & performance.
        </p>
      </div>
      <div class="flex flex-col items-end gap-1">
        <Button size="sm" @click="openAddDialog">
          <Plus class="w-4 h-4 mr-1" />
          Add to watchlist
        </Button>
      </div>
    </div>

    <!-- Fehleranzeige -->
    <div v-if="error" class="text-sm text-red-500">
      {{ error }}
    </div>

    <!-- Tabelle -->
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead class="w-[90px]">Symbol</TableHead>
          <TableHead>Name</TableHead>
          <TableHead class="text-right">Initial Price</TableHead>
          <TableHead class="text-right">Current Price</TableHead>
          <TableHead class="text-right">Performance</TableHead>
          <TableHead class="text-right">Added</TableHead>
          <TableHead class="text-right">Actions</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <TableRow v-if="loading">
          <TableCell colspan="7" class="text-center py-6 text-sm text-muted-foreground">
            Loading Watchlist…
          </TableCell>
        </TableRow>
        <TableRow v-else-if="entries.length === 0">
          <TableCell colspan="7" class="text-center py-6 text-sm text-muted-foreground">
            No entries available in watchlist
          </TableCell>
        </TableRow>

        <TableRow
            v-else
            v-for="entry in entries"
            :key="entry.id"
            class="hover:bg-muted/40"
        >
          <!-- Symbol -->
          <TableCell
              class="font-mono font-semibold cursor-pointer hover:underline"
              @click="goToStock(entry.stock.symbol)"
          >
            {{ entry.stock.symbol }}
          </TableCell>

          <!-- Name -->
          <TableCell class="truncate max-w-[200px]">
            <div class="text-sm font-medium">
              {{ entry.stock.name || '—' }}
            </div>
            <div class="text-xs text-muted-foreground">
              {{ entry.stock.exchange || entry.stock.isin || '' }}
            </div>
          </TableCell>

          <!-- Initial Price -->
          <TableCell class="text-right text-sm">
            <span v-if="entry.initialPrice != null">
              {{ formatPrice(entry.initialPrice) }}
            </span>
            <span v-else class="text-muted-foreground">–</span>
          </TableCell>

          <!-- Last Price -->
          <TableCell class="text-right text-sm">
            <span v-if="entry.currentPrice != null">
              {{ formatPrice(entry.currentPrice) }}
            </span>
            <span v-else class="text-muted-foreground">–</span>
          </TableCell>

          <!-- Performance -->
          <TableCell class="text-right text-sm">
            <div v-if="performance(entry)" class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold"
                 :class="performance(entry)!.abs >= 0
                   ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-300'
                   : 'bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-300'">
              <span class="mr-1 inline-block h-1.5 w-1.5 rounded-full"
                    :class="performance(entry)!.abs >= 0 ? 'bg-emerald-500' : 'bg-red-500'"/>
              <span>
                {{ performance(entry)!.abs >= 0 ? '+' : '' }}{{ formatPrice(performance(entry)!.abs) }}
                ({{ performance(entry)!.pct >= 0 ? '+' : '' }}{{ performance(entry)!.pct.toFixed(2) }}%)
              </span>
            </div>
            <span v-else class="text-xs text-muted-foreground">–</span>
          </TableCell>

          <!-- Created -->
          <TableCell class="text-right text-xs text-muted-foreground">
            {{ formatDate(entry.createdAt) }}
          </TableCell>

          <!-- Actions -->
          <TableCell class="text-right">
            <div class="flex items-center justify-end gap-2">
              <Button
                  size="icon"
                  variant="destructive"
                  @click="removeFromWatchlist(entry)"
                  :disabled="removingId === entry.id"
                  title="Von Watchlist entfernen"
              >
                <Loader2 v-if="removingId === entry.id" class="w-4 h-4 animate-spin" />
                <Trash2 v-else class="w-4 h-4" />
              </Button>
            </div>
          </TableCell>
        </TableRow>
      </TableBody>
    </Table>

    <!-- Hinweis -->
    <p class="mt-3 text-xs text-muted-foreground">
      * Price and performance is based from initial stock value at creation and the last available closing price.
    </p>

    <!-- Dialog: Stock zur Watchlist hinzufügen -->
    <Dialog v-model:open="addDialogOpen">
      <DialogContent class="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Add stock to watchlist</DialogTitle>
          <DialogDescription>
            Enter the symbol of a stock that you want to observe.
          </DialogDescription>
        </DialogHeader>

        <form class="space-y-4" @submit.prevent="addToWatchlist">
          <div class="space-y-1">
            <Label for="symbol">Symbol</Label>
            <Input
                id="symbol"
                v-model="addForm.symbol"
                placeholder="z.B. AAPL"
                class="uppercase"
                required
            />
            <p class="text-xs text-muted-foreground">
              The stock symbol must be available in the system.
            </p>
          </div>

          <div v-if="addError" class="text-sm text-red-500">
            {{ addError }}
          </div>

          <DialogFooter class="gap-2">
            <Button
                type="button"
                variant="outline"
                @click="addDialogOpen = false"
            >
              Cancel
            </Button>
            <Button type="submit" :disabled="adding">
              <Loader2 v-if="adding" class="w-4 h-4 mr-2 animate-spin" />
              <span>Add</span>
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

// shadcn-vue Components
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow
} from '@/components/ui/table'

// Icons (lucide-vue-next)
import { ArrowUpRight, Loader2, Plus, Trash2 } from 'lucide-vue-next'

// ---- Types ----

interface StockRef {
  id: number
  symbol: string
  name: string | null
  exchange?: string | null
  isin?: string | null
}

interface WatchlistEntry {
  id: number
  createdAt: string
  initialPrice: number | null
  currentPrice: number | null
  stock: StockRef
}

interface Performance {
  abs: number
  pct: number
}

interface AddForm {
  symbol: string
}

// ---- State ----

const entries = ref<WatchlistEntry[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const addDialogOpen = ref(false)
const addForm = ref<AddForm>({ symbol: '' })
const addError = ref<string | null>(null)
const adding = ref(false)

const removingId = ref<number | null>(null)

const apiBase = 'http://localhost:8080/watchlist'
const router = useRouter()

// ---- Helpers ----

function formatDate(iso: string | null | undefined): string {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString('de-DE', {
    day: '2-digit',
    month: '2-digit',
    year: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatPrice(value: number): string {
  return new Intl.NumberFormat('de-DE', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 4
  }).format(value)
}

/**
 * Berechnet absolute und prozentuale Performance oder null, wenn nicht möglich.
 */
function performance(entry: WatchlistEntry): Performance | null {
  if (
      entry.initialPrice == null ||
      entry.initialPrice === 0 ||
      entry.currentPrice == null
  ) {
    return null
  }
  const abs = entry.currentPrice - entry.initialPrice
  const pct = (abs / entry.initialPrice) * 100
  return { abs, pct }
}

function goToStock(symbol: string) {
  router.push(`/stocks/${symbol}`)
}

// ---- API Calls ----

async function loadWatchlist() {
  loading.value = true
  error.value = null
  try {
    const res = await fetch(apiBase)
    if (!res.ok) {
      throw new Error(`Error while loading watchlist (${res.status})`)
    }
    const data = await res.json()
    entries.value = data
  } catch (e: any) {
    error.value = e?.message ?? 'Unknown error occurred while loading watchlist'
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  addForm.value = { symbol: '' }
  addError.value = null
  addDialogOpen.value = true
}

async function addToWatchlist() {
  addError.value = null
  adding.value = true

  const payload = {
    symbol: addForm.value.symbol.trim().toUpperCase()
  }

  try {
    const res = await fetch(apiBase, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (!res.ok) {
      const text = await res.text()
      throw new Error(text || `Failed to add (${res.status})`)
    }

    addDialogOpen.value = false
    await loadWatchlist()
  } catch (e: any) {
    addError.value = e?.message ?? 'Failed to add to watchlist'
  } finally {
    adding.value = false
  }
}

async function removeFromWatchlist(entry: WatchlistEntry) {
  removingId.value = entry.id
  try {
    const res = await fetch(`${apiBase}/${entry.id}`, {
      method: 'DELETE'
    })
    if (!res.ok) {
      const text = await res.text()
      throw new Error(text || `Failed to remove (${res.status})`)
    }
    await loadWatchlist()
  } catch (e: any) {
    error.value = e?.message ?? 'Failed to remove from watchlist'
  } finally {
    removingId.value = null
  }
}

// ---- Lifecycle ----

onMounted(() => {
  loadWatchlist()
})
</script>
