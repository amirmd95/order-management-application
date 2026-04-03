import { CommonModule, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { OrderManagementApiService } from './order-management-api.service';
import { InventoryItem, Order, Product } from './models';

@Component({
  selector: 'app-root',
  imports: [CommonModule, ReactiveFormsModule, DatePipe],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  private readonly formBuilder = inject(FormBuilder);
  private readonly api = inject(OrderManagementApiService);

  protected readonly angularVersion = '21.1';
  protected readonly products = signal<Product[]>([]);
  protected readonly orders = signal<Order[]>([]);
  protected readonly inventoryBySku = signal<Record<string, InventoryItem>>({});
  protected readonly isRefreshing = signal(false);
  protected readonly notice = signal<{ type: 'success' | 'error'; message: string } | null>(null);

  protected readonly productForm = this.formBuilder.group({
    sku: ['', [Validators.required, Validators.maxLength(64)]],
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', [Validators.required, Validators.maxLength(300)]],
    price: [1499, [Validators.required, Validators.min(0.01)]],
    currency: ['INR', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    active: [true]
  });

  protected readonly stockForm = this.formBuilder.group({
    sku: ['', [Validators.required, Validators.maxLength(64)]],
    quantity: [0, [Validators.required, Validators.min(0)]]
  });

  protected readonly orderForm = this.formBuilder.group({
    customerEmail: ['buyer@example.com', [Validators.required, Validators.email]],
    currency: ['INR', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    items: this.formBuilder.array([this.createOrderItemGroup()])
  });

  protected readonly stockedSkuCount = computed(
    () =>
      Object.values(this.inventoryBySku()).filter((item) => item.availableQuantity > 0 || item.reservedQuantity > 0)
        .length
  );

  protected readonly orderableProducts = computed(() => this.products().filter((product) => product.active));

  protected readonly latestOrders = computed(() =>
    [...this.orders()]
      .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())
      .slice(0, 4)
  );

  protected readonly inventoryRows = computed(() =>
    this.products().map((product) => {
      const inventory = this.inventoryBySku()[product.sku];
      const available = inventory?.availableQuantity ?? 0;
      const reserved = inventory?.reservedQuantity ?? 0;

      return {
        sku: product.sku,
        name: product.name,
        available,
        reserved,
        sellable: available - reserved
      };
    })
  );

  protected readonly summaryCards = computed(() => [
    {
      label: 'Catalog size',
      value: this.products().length.toString().padStart(2, '0'),
      hint: 'Products available for stock and sales orchestration'
    },
    {
      label: 'Stocked SKUs',
      value: this.stockedSkuCount().toString().padStart(2, '0'),
      hint: 'Inventory snapshots ready to support order reservations'
    },
    {
      label: 'Confirmed orders',
      value: this.orders().length.toString().padStart(2, '0'),
      hint: 'Orders persisted by the order-service'
    }
  ]);

  constructor() {
    void this.refreshDashboard();
  }

  protected orderItemControls() {
    return this.orderItems.controls;
  }

  protected addOrderItem(initialSku = ''): void {
    this.orderItems.push(this.createOrderItemGroup(initialSku));
  }

  protected removeOrderItem(index: number): void {
    if (this.orderItems.length === 1) {
      return;
    }

    this.orderItems.removeAt(index);
  }

  protected appendProductToOrder(sku: string): void {
    const emptySlot = this.orderItems.controls.find((control) => !control.get('sku')?.value);
    if (emptySlot) {
      emptySlot.patchValue({ sku });
      return;
    }

    this.addOrderItem(sku);
  }

  protected prepareInventory(sku: string): void {
    const current = this.inventoryBySku()[sku];
    this.stockForm.patchValue({
      sku,
      quantity: current?.availableQuantity ?? 0
    });
  }

  protected async refreshDashboard(): Promise<void> {
    this.isRefreshing.set(true);

    try {
      const [products, orders] = await Promise.all([
        firstValueFrom(this.api.listProducts()),
        firstValueFrom(this.api.listOrders())
      ]);

      const inventoryEntries = await Promise.all(
        products.map(async (product) => {
          try {
            const inventory = await firstValueFrom(this.api.getInventory(product.sku));
            return [product.sku, inventory] as const;
          } catch (error) {
            if (error instanceof HttpErrorResponse && error.status === 404) {
              return [product.sku, null] as const;
            }

            throw error;
          }
        })
      );

      this.products.set(products);
      this.orders.set(
        [...orders].sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())
      );
      this.inventoryBySku.set(
        Object.fromEntries(
          inventoryEntries.filter((entry): entry is readonly [string, InventoryItem] => entry[1] !== null)
        )
      );
    } catch (error) {
      this.showNotice('error', this.readError(error, 'Unable to refresh the dashboard.'));
    } finally {
      this.isRefreshing.set(false);
    }
  }

  protected async submitProduct(): Promise<void> {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    try {
      const raw = this.productForm.getRawValue();
      const sku = raw.sku?.trim() ?? '';
      const name = raw.name?.trim() ?? '';
      const description = raw.description?.trim() ?? '';
      const currency = raw.currency?.trim().toUpperCase() ?? 'INR';
      await firstValueFrom(
        this.api.createProduct({
          sku,
          name,
          description,
          price: Number(raw.price),
          currency,
          active: !!raw.active
        })
      );
      this.productForm.patchValue({
        sku: '',
        name: '',
        description: '',
        price: 1499,
        currency,
        active: true
      });
      this.showNotice('success', 'Product created and ready for stock setup.');
      await this.refreshDashboard();
    } catch (error) {
      this.showNotice('error', this.readError(error, 'Unable to create the product.'));
    }
  }

  protected async submitInventory(): Promise<void> {
    if (this.stockForm.invalid) {
      this.stockForm.markAllAsTouched();
      return;
    }

    try {
      const raw = this.stockForm.getRawValue();
      const sku = raw.sku?.trim() ?? '';
      await firstValueFrom(
        this.api.upsertInventory({
          sku,
          quantity: Number(raw.quantity)
        })
      );
      this.showNotice('success', `Inventory snapshot saved for ${sku}.`);
      await this.refreshDashboard();
    } catch (error) {
      this.showNotice('error', this.readError(error, 'Unable to update inventory.'));
    }
  }

  protected async submitOrder(): Promise<void> {
    if (this.orderForm.invalid) {
      this.orderForm.markAllAsTouched();
      return;
    }

    try {
      const raw = this.orderForm.getRawValue();
      await firstValueFrom(
        this.api.placeOrder({
          customerEmail: raw.customerEmail?.trim() ?? '',
          currency: raw.currency?.trim().toUpperCase() ?? 'INR',
          items:
            raw.items?.map((item) => ({
              sku: item?.sku?.trim() ?? '',
              quantity: Number(item?.quantity ?? 1)
            })) ?? []
        })
      );
      this.orderForm.patchValue({
        customerEmail: raw.customerEmail?.trim() ?? '',
        currency: raw.currency?.trim().toUpperCase() ?? 'INR'
      });
      this.orderItems.clear();
      this.orderItems.push(this.createOrderItemGroup());
      this.showNotice('success', 'Order placed successfully and inventory reserved.');
      await this.refreshDashboard();
    } catch (error) {
      this.showNotice('error', this.readError(error, 'Unable to place the order.'));
    }
  }

  protected formatMoney(value: number, currency: string): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency
    }).format(value);
  }

  private get orderItems(): FormArray {
    return this.orderForm.get('items') as FormArray;
  }

  private createOrderItemGroup(initialSku = '') {
    return this.formBuilder.group({
      sku: [initialSku, [Validators.required]],
      quantity: [1, [Validators.required, Validators.min(1)]]
    });
  }

  private showNotice(type: 'success' | 'error', message: string): void {
    this.notice.set({ type, message });
  }

  private readError(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      return typeof error.error === 'string' && error.error ? error.error : error.message || fallback;
    }

    if (error instanceof Error && error.message) {
      return error.message;
    }

    return fallback;
  }
}
