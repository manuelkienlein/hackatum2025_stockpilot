// src/services/stockService.ts
import axios from "axios";

export interface Stock {
    id: number;
    symbol: string;
    name: string;
    exchange: string;
    isin: string;
}

export interface StockHistoryItem {
    id: number;
    symbol: string;
    date: string; // ISO-String
    open: number;
    close: number;
    high: number;
    low: number;
    volume: number;
}

// payload zum Erstellen (id kommt vom Backend)
export type CreateStockPayload = Omit<Stock, "id">;

// payload zum Updaten (alles optional)
export type UpdateStockPayload = Partial<CreateStockPayload>;

const api = axios.create({
    baseURL: "http://localhost:8080",
    // headers: { ... } // falls du Auth etc. brauchst
});

export async function getStocks(): Promise<Stock[]> {
    const res = await api.get<Stock[]>("/stocks");
    return res.data;
}

export async function getStock(id: number): Promise<Stock> {
    const res = await api.get<Stock>(`/stocks/${id}`);
    return res.data;
}

export async function createStock(payload: CreateStockPayload): Promise<Stock> {
    const res = await api.post<Stock>("/stocks", payload);
    return res.data;
}

export async function updateStock(
    id: number,
    payload: UpdateStockPayload,
): Promise<Stock> {
    const res = await api.put<Stock>(`/stocks/${id}`, payload);
    return res.data;
}

export async function deleteStock(id: number): Promise<void> {
    await api.delete(`/stocks/${id}`);
}

export async function getStockHistory(
    symbol: string,
): Promise<StockHistoryItem[]> {
    const res = await api.get<StockHistoryItem[]>(`/stocks/${symbol}/history`);
    return res.data;
}
