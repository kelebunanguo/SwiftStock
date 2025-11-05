package com.swiftstock.service.impl;

import com.swiftstock.dto.OrderCreateDTO;
import com.swiftstock.entity.Order;
import com.swiftstock.entity.OrderItem;
import com.swiftstock.entity.OrderStatus;
import com.swiftstock.entity.Product;
import com.swiftstock.mapper.OrderMapper;
import com.swiftstock.mapper.OrderItemMapper;
import com.swiftstock.service.OrderService;
import com.swiftstock.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private static final AtomicInteger ORDER_SEQUENCE = new AtomicInteger(1);
    private static final DateTimeFormatter ORDER_NO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductService productService;

    @Override
    @Transactional
    public Order createOrder(OrderCreateDTO orderDTO) {
        // 1. 创建订单
        Order order = new Order();
        // 使用前端传递的订单编号，如果没有则自动生成
        if (orderDTO.getOrderNo() != null && !orderDTO.getOrderNo().isEmpty()) {
            order.setOrderNo(orderDTO.getOrderNo());
        } else {
            order.setOrderNo(generateOrderNo());
        }
        order.setCustomerName(orderDTO.getCustomerName());
        order.setCustomerPhone(orderDTO.getCustomerPhone());
        order.setRemark(orderDTO.getRemark());
        // 使用前端传递的状态，如果没有则默认为UNPAID
        if (orderDTO.getStatus() != null && !orderDTO.getStatus().isEmpty()) {
            order.setStatus(OrderStatus.valueOf(orderDTO.getStatus().toUpperCase()));
        } else {
            order.setStatus(OrderStatus.UNPAID);
        }
        order.setCreatedTime(LocalDateTime.now());
        
        // 2. 处理订单项
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderCreateDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            // 创建订单项
            OrderItem item = new OrderItem();
            item.setProductId(itemDTO.getProductId());
            item.setProductName(itemDTO.getProductName());
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(BigDecimal.valueOf(itemDTO.getPrice()));
            item.setAmount(BigDecimal.valueOf(itemDTO.getAmount()));
            item.setCreatedTime(LocalDateTime.now());
            
            orderItems.add(item);
            totalAmount = totalAmount.add(item.getAmount());
        }
        
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        // 3. 保存订单
        orderMapper.insert(order);
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
            
            // 4. 更新库存（只有在订单状态为PAID时才扣减库存）
            if (order.getStatus() == OrderStatus.PAID) {
                productService.updateStock(item.getProductId(), -item.getQuantity());
                log.info("订单{}状态为已付款，自动扣减商品{}库存{}件", order.getOrderNo(), item.getProductName(), item.getQuantity());
            }
        }

        return order;
    }

    private String generateOrderNo() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(ORDER_NO_DATE_FORMATTER);
        String sequencePart = String.format("%04d", ORDER_SEQUENCE.getAndIncrement());
        return "ORD" + datePart + sequencePart;
    }

    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    @Override
    public List<Order> findByCondition(Order searchParams) {
        try {
            log.debug("Finding orders by condition: {}", searchParams);
            if (searchParams == null) {
                log.debug("Search params is null, returning all orders");
                return findAll();
            }
            
            // 处理日期范围查询
            if (searchParams.getStartDate() != null || searchParams.getEndDate() != null) {
                log.debug("Date range search - start: {}, end: {}", 
                    searchParams.getStartDate(), searchParams.getEndDate());
            }
            
            List<Order> orders = orderMapper.findByCondition(searchParams);
            log.debug("Found {} orders matching the condition", orders.size());
            return orders;
        } catch (Exception e) {
            log.error("Error finding orders by condition: {}", searchParams, e);
            throw new RuntimeException("查询订单失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Order findById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.valueOf(status);
        
        // 检查状态流转是否合法
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new RuntimeException(String.format("订单状态不能从%s流转到%s", 
                oldStatus.getDescription(), newStatus.getDescription()));
        }
        
        // 业务逻辑验证
        validateStatusTransition(order, oldStatus, newStatus);
        
        // 处理状态变更时的库存操作
        if (oldStatus != newStatus) {
            handleStockOperationOnStatusChange(order, oldStatus, newStatus);
        }
        
        order.setStatus(newStatus);
        order.setUpdatedTime(LocalDateTime.now());
        orderMapper.updateStatus(order);
        
        log.info("订单{}状态从{}变更为{}", order.getOrderNo(), oldStatus.getDescription(), newStatus.getDescription());
    }
    
    /**
     * 处理订单状态变更时的库存操作
     */
    private void handleStockOperationOnStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        // 如果新状态是"已付款"，需要扣减库存
        if (newStatus == OrderStatus.PAID && oldStatus != OrderStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                productService.updateStock(item.getProductId(), -item.getQuantity());
                log.info("订单{}状态变更为已付款，自动扣减商品{}库存{}件", order.getOrderNo(), item.getProductName(), item.getQuantity());
            }
        }
        // 如果从"已付款"变更为其他状态（非已完成），需要恢复库存
        else if (oldStatus == OrderStatus.PAID && newStatus != OrderStatus.COMPLETED && newStatus != OrderStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                productService.updateStock(item.getProductId(), item.getQuantity());
                log.info("订单{}状态从已付款变更为{}，自动恢复商品{}库存{}件", order.getOrderNo(), newStatus.getDescription(), item.getProductName(), item.getQuantity());
            }
        }
    }

    @Override
    @Transactional
    public void transitionStatus(Long id, String targetStatus, String reason) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        
        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.valueOf(targetStatus);
        
        // 检查状态流转是否合法
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new RuntimeException(String.format("订单状态不能从%s流转到%s", 
                currentStatus.getDescription(), newStatus.getDescription()));
        }
        
        // 业务逻辑验证
        validateStatusTransition(order, currentStatus, newStatus);
        
        // 处理状态流转
        handleStockOperationOnStatusChange(order, currentStatus, newStatus);
        
        order.setStatus(newStatus);
        order.setUpdatedTime(LocalDateTime.now());
        orderMapper.updateStatus(order);
        
        log.info("订单{}状态流转：{} -> {}，原因：{}", order.getOrderNo(), 
                currentStatus.getDescription(), newStatus.getDescription(), reason);
    }
    
    /**
     * 验证状态流转的业务逻辑
     */
    private void validateStatusTransition(Order order, OrderStatus currentStatus, OrderStatus newStatus) {
        // 检查配货中状态的库存要求
        if (newStatus.requiresStockValidation()) {
            validateStockForPreparing(order);
        }
        
        // 检查发货状态的业务要求
        if (newStatus == OrderStatus.SHIPPED) {
            validateForShipping(order);
        }
        
        // 检查完成状态的业务要求
        if (newStatus == OrderStatus.COMPLETED) {
            validateForCompletion(order);
        }
        
        // 检查取消状态的业务要求
        if (newStatus == OrderStatus.CANCELLED) {
            validateForCancellation(order, currentStatus);
        }
        
        // 检查退款状态的业务要求
        if (newStatus == OrderStatus.REFUNDED) {
            validateForRefund(order, currentStatus);
        }
    }
    
    /**
     * 验证配货中的库存要求
     */
    private void validateStockForPreparing(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = productService.findById(item.getProductId());
            if (product == null) {
                throw new RuntimeException(String.format("商品%s不存在", item.getProductName()));
            }
            
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException(String.format("商品%s库存不足，当前库存：%d，需要：%d", 
                    item.getProductName(), product.getStockQuantity(), item.getQuantity()));
            }
        }
        log.info("订单{}配货前库存检查通过", order.getOrderNo());
    }
    
    /**
     * 验证发货状态的业务要求
     */
    private void validateForShipping(Order order) {
        // 检查订单是否已付款
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.PREPARING) {
            throw new RuntimeException("只有已付款或配货中的订单才能发货");
        }
        
        // 检查是否有有效的收货信息
        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
            throw new RuntimeException("订单缺少客户信息，无法发货");
        }
        
        log.info("订单{}发货前业务检查通过", order.getOrderNo());
    }
    
    /**
     * 验证完成状态的业务要求
     */
    private void validateForCompletion(Order order) {
        // 检查订单是否已发货
        if (order.getStatus() != OrderStatus.SHIPPED && order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("只有已发货或已送达的订单才能完成");
        }
        
        log.info("订单{}完成前业务检查通过", order.getOrderNo());
    }
    
    /**
     * 验证取消状态的业务要求
     */
    private void validateForCancellation(Order order, OrderStatus currentStatus) {
        // 检查订单是否可以取消
        if (!currentStatus.canBeCancelled()) {
            throw new RuntimeException(String.format("订单状态为%s，无法取消", currentStatus.getDescription()));
        }
        
        // 检查订单是否有有效的取消原因
        if (order.getRemark() == null || order.getRemark().trim().isEmpty()) {
            log.warn("订单{}取消时未提供原因", order.getOrderNo());
        }
        
        log.info("订单{}取消前业务检查通过", order.getOrderNo());
    }
    
    /**
     * 验证退款状态的业务要求
     */
    private void validateForRefund(Order order, OrderStatus currentStatus) {
        // 检查订单是否可以退款
        if (!currentStatus.canBeRefunded()) {
            throw new RuntimeException(String.format("订单状态为%s，无法退款", currentStatus.getDescription()));
        }
        
        // 检查订单金额是否大于0
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("订单金额无效，无法退款");
        }
        
        log.info("订单{}退款前业务检查通过", order.getOrderNo());
    }

    @Override
    @Transactional
    public void cancelOrder(Long id, String reason) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        
        OrderStatus currentStatus = order.getStatus();
        
        // 检查是否可以取消
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new RuntimeException("订单已完成或已取消，无法再次取消");
        }
        
        // 如果订单已付款，需要恢复库存
        if (currentStatus == OrderStatus.PAID || currentStatus == OrderStatus.PREPARING || 
            currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.DELIVERED) {
            for (OrderItem item : order.getItems()) {
                productService.updateStock(item.getProductId(), item.getQuantity());
                log.info("订单{}取消，恢复商品{}库存{}件", order.getOrderNo(), item.getProductName(), item.getQuantity());
            }
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedTime(LocalDateTime.now());
        orderMapper.updateStatus(order);
        
        log.info("订单{}已取消，原因：{}", order.getOrderNo(), reason);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        
        // 如果订单已付款，需要恢复库存
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.PREPARING || 
            order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            for (OrderItem item : order.getItems()) {
                productService.updateStock(item.getProductId(), item.getQuantity());
                log.info("删除订单{}，恢复商品{}库存{}件", order.getOrderNo(), item.getProductName(), item.getQuantity());
            }
        }
        
        // 删除订单项
        orderItemMapper.deleteByOrderId(id);
        
        // 删除订单
        orderMapper.deleteById(id);
        
        log.info("订单{}已删除", order.getOrderNo());
    }

    @Override
    public List<Map<String, Object>> getStatusHistory(Long id) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        
        List<Map<String, Object>> history = new ArrayList<>();
        
        // 创建时间
        Map<String, Object> createRecord = new java.util.HashMap<>();
        createRecord.put("status", OrderStatus.UNPAID.name());
        createRecord.put("statusText", OrderStatus.UNPAID.getDescription());
        createRecord.put("time", order.getCreatedTime());
        createRecord.put("reason", "订单创建");
        history.add(createRecord);
        
        // 根据当前状态生成完整的状态流转历史
        OrderStatus currentStatus = order.getStatus();
        LocalDateTime currentTime = order.getUpdatedTime() != null ? order.getUpdatedTime() : order.getCreatedTime();
        
        // 获取状态流转顺序
        OrderStatus[] statusFlow = OrderStatus.getStatusFlow();
        
        // 找到当前状态在流转中的位置
        int currentIndex = -1;
        for (int i = 0; i < statusFlow.length; i++) {
            if (statusFlow[i] == currentStatus) {
                currentIndex = i;
                break;
            }
        }
        
        // 如果当前状态在正常流转中，添加所有中间状态
        if (currentIndex > 0) {
            for (int i = 1; i <= currentIndex; i++) {
                OrderStatus status = statusFlow[i];
                Map<String, Object> statusRecord = new java.util.HashMap<>();
                statusRecord.put("status", status.name());
                statusRecord.put("statusText", status.getDescription());
                
                // 根据状态设置原因
                String reason = getStatusReason(status);
                statusRecord.put("reason", reason);
                
                // 设置时间（使用当前时间，实际项目中应该有具体的时间记录）
                statusRecord.put("time", currentTime);
                
                history.add(statusRecord);
            }
        }
        
        // 处理特殊状态（取消、退款）
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.REFUNDED) {
            Map<String, Object> specialRecord = new java.util.HashMap<>();
            specialRecord.put("status", currentStatus.name());
            specialRecord.put("statusText", currentStatus.getDescription());
            specialRecord.put("reason", getStatusReason(currentStatus));
            specialRecord.put("time", currentTime);
            history.add(specialRecord);
        }
        
        return history;
    }
    
    /**
     * 根据状态获取原因描述
     */
    private String getStatusReason(OrderStatus status) {
        switch (status) {
            case PAID:
                return "客户付款";
            case PREPARING:
                return "开始配货";
            case SHIPPED:
                return "商品已发货";
            case DELIVERED:
                return "商品已送达";
            case COMPLETED:
                return "订单完成";
            case CANCELLED:
                return "订单取消";
            case REFUNDED:
                return "申请退款";
            default:
                return "状态更新";
        }
    }
} 