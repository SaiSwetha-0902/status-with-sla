package com.example.status.controller;

import com.example.status.dao.OrderStateHistoryDao;
import com.example.status.entity.OrderStateHistoryEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order State History", description = "Order state tracking endpoints")
public class OrderStateController {

    @Autowired
    private OrderStateHistoryDao orderStateHistoryDao;

    @GetMapping("/all")
    @Operation(summary = "Get all order states", description = "Returns all order state history records")
    public List<OrderStateHistoryEntity> getAllOrderStates() {
        return orderStateHistoryDao.findAll();
    }

    @GetMapping("/file/{fileId}")
    @Operation(summary = "Get order states by file ID", description = "Returns order state history for a specific file ID")
    public List<OrderStateHistoryEntity> getOrderStatesByFileId(@PathVariable String fileId) {
        return orderStateHistoryDao.findByFileIdOrderByEventTimeDesc(fileId);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get order states by order ID", description = "Returns order state history for a specific order ID")
    public List<OrderStateHistoryEntity> getOrderStatesByOrderId(@PathVariable String orderId) {
        return orderStateHistoryDao.findByOrderIdOrderByEventTimeDesc(orderId);
    }
}