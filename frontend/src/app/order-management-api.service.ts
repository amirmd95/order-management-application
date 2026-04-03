import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  InventoryItem,
  Order,
  PlaceOrderRequest,
  Product,
  ProductRequest,
  StockAdjustmentRequest
} from './models';

@Injectable({ providedIn: 'root' })
export class OrderManagementApiService {
  private readonly http = inject(HttpClient);

  listProducts(): Observable<Product[]> {
    return this.http.get<Product[]>('/products-api/products');
  }

  createProduct(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>('/products-api/products', request);
  }

  getInventory(sku: string): Observable<InventoryItem> {
    return this.http.get<InventoryItem>(`/inventory-api/inventory/${sku}`);
  }

  upsertInventory(request: StockAdjustmentRequest): Observable<InventoryItem> {
    return this.http.post<InventoryItem>('/inventory-api/inventory/stock', request);
  }

  listOrders(): Observable<Order[]> {
    return this.http.get<Order[]>('/orders-api/orders');
  }

  placeOrder(request: PlaceOrderRequest): Observable<Order> {
    return this.http.post<Order>('/orders-api/orders', request);
  }
}
