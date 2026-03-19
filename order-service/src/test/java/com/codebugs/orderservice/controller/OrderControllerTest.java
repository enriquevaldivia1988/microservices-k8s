package com.codebugs.orderservice.controller;

import com.codebugs.orderservice.dto.CreateOrderRequest;
import com.codebugs.orderservice.dto.OrderResponse;
import com.codebugs.orderservice.exception.OrderNotFoundException;
import com.codebugs.orderservice.model.OrderStatus;
import com.codebugs.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private final UUID orderId = UUID.randomUUID();

    private OrderResponse sampleResponse() {
        return new OrderResponse(orderId, "customer-1", "product-1", 2,
            new BigDecimal("99.99"), OrderStatus.PENDING, Instant.now());
    }

    @Test
    void createOrder_returns201() throws Exception {
        when(orderService.createOrder(any())).thenReturn(sampleResponse());

        CreateOrderRequest request = new CreateOrderRequest(
            "customer-1", "product-1", 2, new BigDecimal("99.99"));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerId").value("customer-1"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createOrder_invalidBody_returns400() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("", "product-1", 0, BigDecimal.ZERO);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getOrder_returns200() throws Exception {
        when(orderService.getOrder(orderId)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/orders/" + orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(orderId.toString()));
    }

    @Test
    void getOrder_notFound_returns404() throws Exception {
        when(orderService.getOrder(orderId)).thenThrow(new OrderNotFoundException(orderId));

        mockMvc.perform(get("/api/orders/" + orderId))
            .andExpect(status().isNotFound());
    }

    @Test
    void cancelOrder_returns200() throws Exception {
        OrderResponse cancelled = new OrderResponse(orderId, "customer-1", "product-1", 2,
            new BigDecimal("99.99"), OrderStatus.CANCELLED, Instant.now());
        when(orderService.cancelOrder(orderId)).thenReturn(cancelled);

        mockMvc.perform(post("/api/orders/" + orderId + "/cancel"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelOrder_invalidState_returns409() throws Exception {
        when(orderService.cancelOrder(orderId))
            .thenThrow(new IllegalStateException("Cannot cancel order in status: CONFIRMED"));

        mockMvc.perform(post("/api/orders/" + orderId + "/cancel"))
            .andExpect(status().isConflict());
    }
}
