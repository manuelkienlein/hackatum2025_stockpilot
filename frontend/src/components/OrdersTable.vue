<template>
  <div class="w-full rounded-xl border bg-card text-card-foreground p-4 md:p-6 space-y-4">
    <!-- Header -->
    <div class="mb-2 flex items-center justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold tracking-tight">
          Orders
        </h2>
        <p class="text-sm text-muted-foreground">
          Simulated buy / sell / limit orders for your portfolio.
        </p>
      </div>
      <div class="flex flex-col items-end gap-1">
        <Button size="sm" @click="openCreateDialog">
          Neue Order
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
          <TableHead>Type</TableHead>
          <TableHead>Side</TableHead>
          <TableHead class="text-right">Qty</TableHead>
          <TableHead class="text-right">Limit / Exec</TableHead>
          <TableHead>Status</TableHead>
          <TableHead class="text-right">Placed</TableHead>
          <TableHead class="text-right">Actions</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <TableRow v-if="loading">
          <TableCell colspan="8" class="text-center py-6 text-sm text-muted-foreground">
            Lädt Orders…
          </TableCell>
        </TableRow>
        <TableRow v-else-if="orders.length === 0">
          <TableCell colspan="8" class="text-center py-6 text-sm text-muted-foreground">
            Noch keine Orders vorhanden.
          </TableCell>
        </TableRow>

        <TableRow
            v-else
            v-for="order in orders"
            :key="order.id"
        >
          <!-- Symbol -->
          <TableCell class="font-mono font-semibold">
            <RouterLink :to="`/stocks/${order.symbol}`">{{ order.symbol }}</RouterLink>
          </TableCell>

          <!-- Type -->
          <TableCell>
            <span class="text-xs font-medium uppercase tracking-wide text-muted-foreground">
              {{ order.type }}
            </span>
          </TableCell>

          <!-- Side -->
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

          <!-- Quantity -->
          <TableCell class="text-right">
            {{ order.quantity.toLocaleString() }}
          </TableCell>

          <!-- Limit / Exec -->
          <TableCell class="text-right">
            <div class="flex flex-col items-end">
              <span
                  v-if="order.type === 'LIMIT' || order.type === 'STOP_LIMIT'"
                  class="text-xs text-muted-foreground"
              >
                Limit: {{ order.limitPrice != null ? formatPrice(order.limitPrice) : '–' }}
              </span>
              <span v-if="order.executedPrice !== null" class="text-xs">
                Exec: {{ formatPrice(order.executedPrice!) }}
              </span>
              <span v-else-if="order.type !== 'LIMIT' && order.type !== 'STOP_LIMIT'" class="text-xs text-muted-foreground">
                –
              </span>
            </div>
          </TableCell>

          <!-- Status -->
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

          <!-- Placed (createdAt) -->
          <TableCell class="text-right text-xs text-muted-foreground">
            {{ formatDate(order.createdAt) }}
          </TableCell>

          <!-- Actions -->
          <TableCell class="text-right">
            <div class="flex items-center justify-end gap-2">
              <Button
                  size="icon"
                  variant="outline"
                  @click="openExecuteDialog(order)"
                  :disabled="order.status !== 'PENDING'"
                  title="Ausführen"
              >
                <Play class="w-4 h-4" />
              </Button>
              <Button
                  size="icon"
                  variant="outline"
                  @click="openEditDialog(order)"
                  :disabled="order.status !== 'PENDING'"
                  title="Bearbeiten"
              >
                <Pencil class="w-4 h-4" />
              </Button>
              <Button
                  size="icon"
                  variant="destructive"
                  @click="cancelOrder(order)"
                  :disabled="order.status !== 'PENDING' || cancellingId === order.id"
                  title="Abbrechen"
              >
                <Loader2 v-if="cancellingId === order.id" class="w-4 h-4 animate-spin" />
                <X v-else class="w-4 h-4" />
              </Button>
            </div>
          </TableCell>
        </TableRow>
      </TableBody>
    </Table>

    <p class="mt-3 text-xs text-muted-foreground">
      {{ orders.length }} orders total
    </p>

    <!-- Dialog für Create/Edit -->
    <Dialog v-model:open="dialogOpen">
      <DialogContent class="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>
            {{ isEditing ? 'Order bearbeiten' : 'Neue Order anlegen' }}
          </DialogTitle>
          <DialogDescription>
            {{ isEditing
              ? 'Passe die Orderdaten an. Nur PENDING Orders können bearbeitet werden.'
              : 'Lege eine neue Order an.' }}
          </DialogDescription>
        </DialogHeader>

        <form class="space-y-4" @submit.prevent="saveOrder">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div class="space-y-1">
              <Label for="symbol">Symbol</Label>
              <Input
                  id="symbol"
                  v-model="form.symbol"
                  placeholder="AAPL"
                  required
              />
            </div>

            <div class="space-y-1">
              <Label>Side</Label>
              <Select v-model="form.side">
                <SelectTrigger>
                  <SelectValue placeholder="Side wählen" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="BUY">BUY</SelectItem>
                  <SelectItem value="SELL">SELL</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div class="space-y-1">
              <Label>Type</Label>
              <Select v-model="form.type">
                <SelectTrigger>
                  <SelectValue placeholder="Ordertyp wählen" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="MARKET">MARKET</SelectItem>
                  <SelectItem value="LIMIT">LIMIT</SelectItem>
                  <SelectItem value="STOP">STOP</SelectItem>
                  <SelectItem value="STOP_LIMIT">STOP_LIMIT</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div class="space-y-1">
              <Label for="quantity">Quantity</Label>
              <Input
                  id="quantity"
                  type="number"
                  min="1"
                  step="1"
                  v-model.number="form.quantity"
                  required
              />
            </div>

            <div class="space-y-1">
              <Label for="limitPrice">Limit Price</Label>
              <Input
                  id="limitPrice"
                  type="number"
                  step="0.0001"
                  v-model="form.limitPrice"
                  :disabled="!requiresLimitPrice"
                  :placeholder="requiresLimitPrice ? 'z.B. 180.50' : 'nur für LIMIT/STOP_LIMIT'"
              />
            </div>

            <div class="space-y-1">
              <Label for="stopPrice">Stop Price</Label>
              <Input
                  id="stopPrice"
                  type="number"
                  step="0.0001"
                  v-model="form.stopPrice"
                  :disabled="!requiresStopPrice"
                  :placeholder="requiresStopPrice ? 'z.B. 175.00' : 'nur für STOP/STOP_LIMIT'"
              />
            </div>
          </div>

          <div v-if="formError" class="text-sm text-red-500">
            {{ formError }}
          </div>

          <DialogFooter class="gap-2">
            <Button
                type="button"
                variant="outline"
                @click="dialogOpen = false"
            >
              Abbrechen
            </Button>
            <Button type="submit" :disabled="saving">
              <Loader2 v-if="saving" class="w-4 h-4 mr-2 animate-spin" />
              <span>{{ isEditing ? 'Speichern' : 'Anlegen' }}</span>
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <!-- Dialog für manuelles Ausführen -->
    <Dialog v-model:open="executeDialogOpen">
      <DialogContent class="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Order ausführen</DialogTitle>
          <DialogDescription>
            Setze den Ausführungspreis und optional die ausgeführte Stückzahl.
          </DialogDescription>
        </DialogHeader>

        <div v-if="selectedOrder" class="mb-3 text-sm text-muted-foreground">
          <div>Order #{{ selectedOrder.id }} – {{ selectedOrder.symbol }}</div>
          <div>{{ selectedOrder.side }} {{ selectedOrder.quantity }} ({{ selectedOrder.type }})</div>
        </div>

        <form class="space-y-4" @submit.prevent="executeOrderWithInput">
          <div class="space-y-1">
            <Label for="executedPrice">Ausführungspreis</Label>
            <Input
                id="executedPrice"
                type="number"
                step="0.0001"
                v-model="executeForm.executedPrice"
                required
            />
          </div>

          <div class="space-y-1">
            <Label for="executedQuantity">Ausgeführte Stückzahl (optional)</Label>
            <Input
                id="executedQuantity"
                type="number"
                min="1"
                step="1"
                v-model.number="executeForm.executedQuantity"
                :placeholder="selectedOrder ? `Standard: ${selectedOrder.quantity}` : ''"
            />
            <p class="text-xs text-muted-foreground">
              Leer lassen, um die komplette Ordermenge auszuführen.
            </p>
          </div>

          <div v-if="executeError" class="text-sm text-red-500">
            {{ executeError }}
          </div>

          <DialogFooter class="gap-2">
            <Button
                type="button"
                variant="outline"
                @click="executeDialogOpen = false"
            >
              Abbrechen
            </Button>
            <Button type="submit" :disabled="executeSaving">
              <Loader2 v-if="executeSaving" class="w-4 h-4 mr-2 animate-spin" />
              <span>Ausführen</span>
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow
} from '@/components/ui/table'

// Icons (lucide-vue-next)
import { Loader2, Pencil, Play, X } from 'lucide-vue-next'

// ---- Types ----

type OrderSide = 'BUY' | 'SELL'
type OrderType = 'MARKET' | 'LIMIT' | 'STOP' | 'STOP_LIMIT'
type OrderStatus = 'PENDING' | 'EXECUTED' | 'CANCELLED'

interface Order {
  id: number
  symbol: string
  side: OrderSide
  type: OrderType
  quantity: number
  limitPrice: number | null
  stopPrice: number | null
  createdAt: string
  status: OrderStatus
  executedPrice: number | null
  executedAt: string | null
  executedQuantity: number | null
}

interface OrderForm {
  id?: number
  symbol: string
  side: OrderSide | ''
  type: OrderType | ''
  quantity: number
  limitPrice: string | null
  stopPrice: string | null
}

interface ExecuteForm {
  executedPrice: string
  executedQuantity: number | null
}

// ---- State ----

const orders = ref<Order[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const dialogOpen = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const formError = ref<string | null>(null)
const cancellingId = ref<number | null>(null)

const executeDialogOpen = ref(false)
const executeSaving = ref(false)
const executeError = ref<string | null>(null)
const selectedOrder = ref<Order | null>(null)

const form = ref<OrderForm>({
  symbol: '',
  side: '',
  type: '',
  quantity: 1,
  limitPrice: null,
  stopPrice: null
})

const executeForm = ref<ExecuteForm>({
  executedPrice: '',
  executedQuantity: null
})

// ---- Helpers ----

const apiBase = 'http://localhost:8080/orders' // ggf. anpassen

function formatDate(value: string | null | undefined): string {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
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

function statusColorClass(status: OrderStatus): string {
  switch (status) {
    case 'PENDING':
      return 'bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-300'
    case 'EXECUTED':
      return 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-300'
    case 'CANCELLED':
      return 'bg-slate-100 text-slate-600 dark:bg-slate-800/50 dark:text-slate-300'
    default:
      return 'bg-slate-100 text-slate-600 dark:bg-slate-800/50 dark:text-slate-300'
  }
}

function statusDotClass(status: OrderStatus): string {
  switch (status) {
    case 'PENDING':
      return 'bg-amber-500'
    case 'EXECUTED':
      return 'bg-emerald-500'
    case 'CANCELLED':
      return 'bg-slate-400'
    default:
      return 'bg-slate-400'
  }
}

const requiresLimitPrice = computed(
    () => form.value.type === 'LIMIT' || form.value.type === 'STOP_LIMIT'
)

const requiresStopPrice = computed(
    () => form.value.type === 'STOP' || form.value.type === 'STOP_LIMIT'
)

// ---- API Calls ----

async function fetchOrders() {
  loading.value = true
  error.value = null
  try {
    const res = await fetch(apiBase)
    if (!res.ok) {
      throw new Error(`Fehler beim Laden der Orders (${res.status})`)
    }
    const data = await res.json()
    orders.value = data
  } catch (e: any) {
    error.value = e?.message ?? 'Unbekannter Fehler beim Laden der Orders'
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  isEditing.value = false
  form.value = {
    symbol: '',
    side: '',
    type: '',
    quantity: 1,
    limitPrice: null,
    stopPrice: null
  }
  formError.value = null
  dialogOpen.value = true
}

function openEditDialog(order: Order) {
  isEditing.value = true
  form.value = {
    id: order.id,
    symbol: order.symbol,
    side: order.side,
    type: order.type,
    quantity: order.quantity,
    limitPrice: order.limitPrice != null ? String(order.limitPrice) : null,
    stopPrice: order.stopPrice != null ? String(order.stopPrice) : null
  }
  formError.value = null
  dialogOpen.value = true
}

function validateForm(): boolean {
  formError.value = null
  const f = form.value

  if (!f.symbol?.trim()) {
    formError.value = 'Symbol ist erforderlich.'
    return false
  }
  if (!f.side) {
    formError.value = 'Side muss gesetzt werden.'
    return false
  }
  if (!f.type) {
    formError.value = 'Ordertyp muss gesetzt werden.'
    return false
  }
  if (!f.quantity || f.quantity <= 0) {
    formError.value = 'Quantity muss größer als 0 sein.'
    return false
  }

  if (requiresLimitPrice.value && !f.limitPrice) {
    formError.value = 'Limit Price ist für diesen Ordertyp erforderlich.'
    return false
  }
  if (requiresStopPrice.value && !f.stopPrice) {
    formError.value = 'Stop Price ist für diesen Ordertyp erforderlich.'
    return false
  }

  return true
}

async function saveOrder() {
  if (!validateForm()) return

  saving.value = true
  formError.value = null

  const f = form.value

  const payload: any = {
    symbol: f.symbol,
    side: f.side,
    type: f.type,
    quantity: f.quantity,
    limitPrice: f.limitPrice ? Number(f.limitPrice) : null,
    stopPrice: f.stopPrice ? Number(f.stopPrice) : null
  }

  try {
    let res: Response

    if (isEditing.value && f.id != null) {
      // PUT /orders/{id} – falls im Backend vorhanden
      res = await fetch(`${apiBase}/${f.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
    } else {
      // POST /orders
      res = await fetch(apiBase, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
    }

    if (!res.ok) {
      const text = await res.text()
      throw new Error(text || `Fehler beim Speichern (${res.status})`)
    }

    dialogOpen.value = false
    await fetchOrders()
  } catch (e: any) {
    formError.value = e?.message ?? 'Fehler beim Speichern der Order'
  } finally {
    saving.value = false
  }
}

async function cancelOrder(order: Order) {
  if (!confirm(`Order #${order.id} wirklich abbrechen?`)) return
  cancellingId.value = order.id
  try {
    const res = await fetch(`${apiBase}/${order.id}/cancel`, {
      method: 'POST'
    })
    if (!res.ok) {
      const text = await res.text()
      throw new Error(text || `Fehler beim Abbrechen (${res.status})`)
    }
    await fetchOrders()
  } catch (e: any) {
    error.value = e?.message ?? 'Fehler beim Abbrechen der Order'
  } finally {
    cancellingId.value = null
  }
}

// ---- Execute-Funktionalität ----

function openExecuteDialog(order: Order) {
  selectedOrder.value = order
  executeForm.value = {
    executedPrice: order.limitPrice != null ? String(order.limitPrice) : '',
    executedQuantity: null
  }
  executeError.value = null
  executeDialogOpen.value = true
}

function validateExecuteForm(): boolean {
  executeError.value = null
  const f = executeForm.value
  const order = selectedOrder.value
  if (!order) {
    executeError.value = 'Keine Order ausgewählt.'
    return false
  }

  if (!f.executedPrice || Number(f.executedPrice) <= 0) {
    executeError.value = 'Ausführungspreis muss > 0 sein.'
    return false
  }

  if (f.executedQuantity != null) {
    if (f.executedQuantity <= 0) {
      executeError.value = 'Ausgeführte Stückzahl muss > 0 sein.'
      return false
    }
    if (f.executedQuantity > order.quantity) {
      executeError.value = 'Ausgeführte Stückzahl darf nicht größer als die Order-Menge sein.'
      return false
    }
  }

  return true
}

async function executeOrderWithInput() {
  if (!validateExecuteForm()) return

  const order = selectedOrder.value
  if (!order) return

  executeSaving.value = true
  executeError.value = null

  const f = executeForm.value

  const payload: any = {
    executedPrice: Number(f.executedPrice),
    executedQuantity: f.executedQuantity ?? null
  }

  try {
    const res = await fetch(`${apiBase}/${order.id}/execute`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (!res.ok) {
      const text = await res.text()
      throw new Error(text || `Fehler beim Ausführen (${res.status})`)
    }

    executeDialogOpen.value = false
    selectedOrder.value = null
    await fetchOrders()
  } catch (e: any) {
    executeError.value = e?.message ?? 'Fehler beim Ausführen der Order'
  } finally {
    executeSaving.value = false
  }
}

// ---- Lifecycle ----

onMounted(() => {
  fetchOrders()
})
</script>
