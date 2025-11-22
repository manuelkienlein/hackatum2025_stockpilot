<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-semibold tracking-tight">Orders</h1>
      <Button @click="openCreateDialog">
        Neue Order
      </Button>
    </div>

    <!-- Fehleranzeige -->
    <div v-if="error" class="text-sm text-red-500">
      {{ error }}
    </div>

    <!-- Tabelle -->
    <div class="border rounded-xl overflow-hidden">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Id</TableHead>
            <TableHead>Symbol</TableHead>
            <TableHead>Side</TableHead>
            <TableHead>Type</TableHead>
            <TableHead class="text-right">Quantity</TableHead>
            <TableHead class="text-right">Limit</TableHead>
            <TableHead class="text-right">Stop</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Created</TableHead>
            <TableHead>Executed</TableHead>
            <TableHead class="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-if="loading">
            <TableCell colspan="11" class="text-center py-6 text-sm text-muted-foreground">
              Lädt Orders…
            </TableCell>
          </TableRow>
          <TableRow v-else-if="orders.length === 0">
            <TableCell colspan="11" class="text-center py-6 text-sm text-muted-foreground">
              Noch keine Orders vorhanden.
            </TableCell>
          </TableRow>
          <TableRow
              v-else
              v-for="order in orders"
              :key="order.id"
          >
            <TableCell>{{ order.id }}</TableCell>
            <TableCell>{{ order.symbol }}</TableCell>
            <TableCell>
              <Badge :variant="order.side === 'BUY' ? 'default' : 'secondary'">
                {{ order.side }}
              </Badge>
            </TableCell>
            <TableCell>{{ order.type }}</TableCell>
            <TableCell class="text-right">
              {{ order.quantity }}
            </TableCell>
            <TableCell class="text-right">
              {{ order.limitPrice ?? '–' }}
            </TableCell>
            <TableCell class="text-right">
              {{ order.stopPrice ?? '–' }}
            </TableCell>
            <TableCell>
              <Badge
                  :variant="statusVariant(order.status)"
                  class="uppercase"
              >
                {{ order.status }}
              </Badge>
            </TableCell>
            <TableCell>
              <span class="text-xs text-muted-foreground">
                {{ formatDate(order.createdAt) }}
              </span>
            </TableCell>
            <TableCell>
              <div class="flex flex-col text-xs text-muted-foreground">
                <span v-if="order.executedAt">
                  {{ formatDate(order.executedAt) }}
                </span>
                <span v-if="order.executedPrice">
                  @ {{ order.executedPrice }}
                </span>
              </div>
            </TableCell>
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
    </div>

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
import { Badge } from '@/components/ui/badge'

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

const apiBase = 'http://localhost:8080/orders' // ggf. anpassen, z.B. '/api/orders'

function formatDate(value: string | null | undefined): string {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleString()
}

function statusVariant(status: OrderStatus) {
  switch (status) {
    case 'PENDING':
      return 'outline'
    case 'EXECUTED':
      return 'default'
    case 'CANCELLED':
      return 'destructive'
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
