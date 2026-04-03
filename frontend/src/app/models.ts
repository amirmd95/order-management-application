export interface Product {
  id?: string;
  sku: string;
  name: string;
  description: string;
  price: number;
  currency: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductRequest {
  sku: string;
  name: string;
  description: string;
  price: number;
  currency: string;
  active: boolean;
}

export interface InventoryItem {
  sku: string;
  availableQuantity: number;
  reservedQuantity: number;
  updatedAt: string;
}

export interface StockAdjustmentRequest {
  sku: string;
  quantity: number;
}

export interface OrderLineItem {
  sku: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface Order {
  id: string;
  orderNumber: string;
  customerEmail: string;
  currency: string;
  status: string;
  totalAmount: number;
  createdAt: string;
  items: OrderLineItem[];
}

export interface PlaceOrderItemRequest {
  sku: string;
  quantity: number;
}

export interface PlaceOrderRequest {
  customerEmail: string;
  currency: string;
  items: PlaceOrderItemRequest[];
}
