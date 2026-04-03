package com.example.ordermanagement.order;

import com.example.ordermanagement.order.client.InventoryReservationClientRequest;
import com.example.ordermanagement.order.client.InventoryServiceClient;
import com.example.ordermanagement.order.client.ProductCatalogResponse;
import com.example.ordermanagement.order.client.ProductServiceClient;
import com.example.ordermanagement.order.dto.PlaceOrderItemRequest;
import com.example.ordermanagement.order.dto.PlaceOrderRequest;
import com.example.ordermanagement.order.repository.CustomerOrderRepository;
import com.example.ordermanagement.order.service.OrderManagementService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderManagementServiceTest {

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private InventoryServiceClient inventoryServiceClient;

    @InjectMocks
    private OrderManagementService orderManagementService;

    @Captor
    private ArgumentCaptor<InventoryReservationClientRequest> reservationCaptor;

    private PlaceOrderRequest request;

    @BeforeEach
    void setUp() {
        request = new PlaceOrderRequest(
                "buyer@example.com",
                "INR",
                List.of(
                        new PlaceOrderItemRequest("SKU-1", 2),
                        new PlaceOrderItemRequest("SKU-2", 1)
                )
        );
    }

    @Test
    void shouldReleaseEarlierReservationsWhenLaterReservationFails() {
        when(productServiceClient.getProductBySku("SKU-1"))
                .thenReturn(new ProductCatalogResponse("SKU-1", "Keyboard", "Mechanical", new BigDecimal("100.00"), "INR", true));
        when(productServiceClient.getProductBySku("SKU-2"))
                .thenReturn(new ProductCatalogResponse("SKU-2", "Mouse", "Wireless", new BigDecimal("50.00"), "INR", true));
        when(inventoryServiceClient.reserveStock(argThat(request -> request != null && request.sku().equals("SKU-1"))))
                .thenReturn(new com.example.ordermanagement.order.client.InventoryReservationClientResponse(
                        "SKU-1",
                        2,
                        "ref-1",
                        true
                ));
        when(inventoryServiceClient.reserveStock(argThat(request -> request != null && request.sku().equals("SKU-2"))))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Insufficient inventory"));

        assertThatThrownBy(() -> orderManagementService.placeOrder(request))
                .isInstanceOf(ResponseStatusException.class);

        verify(inventoryServiceClient, times(1)).releaseStock(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().sku()).isEqualTo("SKU-1");
    }

    @Test
    void shouldRejectMixedCurrencyOrders() {
        when(productServiceClient.getProductBySku("SKU-1"))
                .thenReturn(new ProductCatalogResponse("SKU-1", "Keyboard", "Mechanical", new BigDecimal("100.00"), "USD", true));

        assertThatThrownBy(() -> orderManagementService.placeOrder(
                new PlaceOrderRequest("buyer@example.com", "INR", List.of(new PlaceOrderItemRequest("SKU-1", 1)))
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("currency");
    }

    @Test
    void shouldSendReservationPayloadWithSkuAndQuantity() {
        when(productServiceClient.getProductBySku("SKU-1"))
                .thenReturn(new ProductCatalogResponse("SKU-1", "Keyboard", "Mechanical", new BigDecimal("100.00"), "INR", true));

        when(customerOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        orderManagementService.placeOrder(new PlaceOrderRequest(
                "buyer@example.com",
                "INR",
                List.of(new PlaceOrderItemRequest("SKU-1", 2))
        ));

        verify(inventoryServiceClient).reserveStock(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().sku()).isEqualTo("SKU-1");
        assertThat(reservationCaptor.getValue().quantity()).isEqualTo(2);
    }
}
