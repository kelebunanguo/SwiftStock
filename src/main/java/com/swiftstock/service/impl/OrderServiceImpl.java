package com.swiftstock.service.impl;

import com.swiftstock.dto.OrderCreateDTO;
import com.swiftstock.entity.Order;
import com.swiftstock.entity.OrderItem;
import com.swiftstock.entity.OrderStatus;
import com.swiftstock.entity.OrderStatusHistory;
import com.swiftstock.entity.Product;
import com.swiftstock.mapper.OrderMapper;
import com.swiftstock.mapper.OrderItemMapper;
import com.swiftstock.mapper.OrderStatusHistoryMapper;
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

/**
 * 订单业务实现（Service）
 *
 * <p>核心点：
 * <ul>
 *   <li>订单创建：写入订单与订单项，并计算总金额</li>
 *   <li>订单状态机：通过 {@link com.swiftstock.entity.OrderStatus#canTransitionTo(OrderStatus)} 校验合法流转</li>
 *   <li>库存一致性：状态进入/退出“已付款”时触发库存扣减/恢复（避免超卖/少卖）</li>
 *   <li>事务控制：关键写操作使用 {@code @Transactional} 保证订单与库存变更原子性</li>
 * </ul>
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_NO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderStatusHistoryMapper orderStatusHistoryMapper;

    @Override
    @Transactional
    public Order createOrder(OrderCreateDTO orderDTO) {
        // 1) 创建订单主表（orders）
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
        
        // 2) 处理订单项（order_item）并累计总金额
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
        
        // 3) 保存订单与订单项（在同一事务内）
        orderMapper.insert(order);
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
            
            // 4) 更新库存（只有在订单状态为 PAID 时才扣减库存）
            //    解释：UNPAID/待付款不锁库存；进入 PAID 才认为“已收款，需要备货”，因此扣减库存。
            if (order.getStatus() == OrderStatus.PAID) {
                productService.updateStock(item.getProductId(), -item.getQuantity());
                log.info("订单{}状态为已付款，自动扣减商品{}库存{}件", order.getOrderNo(), item.getProductName(), item.getQuantity());
            }
        }

        // 5) 记录初始状态历史（fromStatus 为空，toStatus 为初始状态）
        logStatusHistory(null, order.getStatus(), "订单创建", order);

        return order;
    }

    /**
     * 生成订单编号
     * 格式：ORD + yyyyMMdd + 4位序号（如 ORD202512180001）
     * 
     * <p>逻辑：查询当天最大订单号，提取序号部分+1，确保每天从0001开始
     */
    private String generateOrderNo() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(ORDER_NO_DATE_FORMATTER);
        String prefix = "ORD" + datePart;
        
        // 从数据库查询今天的最大订单号
        String maxOrderNo = orderMapper.selectMaxOrderNoByPrefix(prefix);
        
        int nextSequence = 1; // 默认从1开始
        
        if (maxOrderNo != null && maxOrderNo.length() >= prefix.length() + 4) {
            try {
                // 提取序号部分（最后4位）
                String seqStr = maxOrderNo.substring(prefix.length());
                int currentSeq = Integer.parseInt(seqStr);
                nextSequence = currentSeq + 1;
            } catch (Exception e) {
                // 如果解析失败，从1开始
                log.warn("解析订单号失败：{}，将从0001开始", maxOrderNo, e);
            }
        }
        
        // 生成新的序号（固定4位，不足补0）
        String sequencePart = String.format("%04d", nextSequence);
        
        return prefix + sequencePart;
    }

    /**
     * 查询所有订单（无条件）
     *
     * @return 订单列表
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 按条件查询订单
     *
     * @param searchParams 查询条件封装的 Order（支持订单号/客户名/状态/日期范围等）
     * @return 满足条件的订单列表
     */
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

    /**
     * 根据 ID 查询订单
     *
     * @param id 订单 ID
     * @return 订单实体或 null
     */
    @Override
    public Order findById(Long id) {
        return orderMapper.selectById(id);
    }

    /**
     * 更新订单状态（直接设置目标状态）
     *
     * <p>业务校验与库存操作在 Service 内完成，方法为事务性操作。</p>
     *
     * @param id     订单 ID
     * @param status 目标状态（如 PAID、SHIPPED 等）
     */
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.valueOf(status);
        
        // 1) 检查状态流转是否合法（状态机约束）
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new RuntimeException(String.format("订单状态不能从%s流转到%s", 
                oldStatus.getDescription(), newStatus.getDescription()));
        }
        
        // 2) 业务逻辑验证（库存/发货/完成/取消/退款等前置条件）
        validateStatusTransition(order, oldStatus, newStatus);
        
        // 3) 处理状态变更时的库存操作（扣减/恢复）
        if (oldStatus != newStatus) {
            handleStockOperationOnStatusChange(order, oldStatus, newStatus);
        }
        
        order.setStatus(newStatus);
        order.setUpdatedTime(LocalDateTime.now());
        orderMapper.updateStatus(order);

        // 4) 记录状态历史
        logStatusHistory(oldStatus, newStatus, "状态更新", order);
        
        log.info("订单{}状态从{}变更为{}", order.getOrderNo(), oldStatus.getDescription(), newStatus.getDescription());
    }
    
    /**
     * 处理订单状态变更时的库存操作
     */
    private void handleStockOperationOnStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        // 场景 A：进入“已付款” => 认为订单已生效，需要从可售库存中扣减
        if (newStatus == OrderStatus.PAID && oldStatus != OrderStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                productService.updateStock(item.getProductId(), -item.getQuantity());
                log.info("订单{}状态变更为已付款，自动扣减商品{}库存{}件", order.getOrderNo(), item.getProductName(), item.getQuantity());
            }
        }
        // 场景 B：从“已付款”离开到非“已付款/已完成” => 认为订单失效或回滚，需要恢复库存
        // 说明：已完成（COMPLETED）表示履约闭环，理论上不应恢复库存。
        else if (oldStatus == OrderStatus.PAID && newStatus != OrderStatus.COMPLETED && newStatus != OrderStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                productService.updateStock(item.getProductId(), item.getQuantity());
                log.info("订单{}状态从已付款变更为{}，自动恢复商品{}库存{}件", order.getOrderNo(), newStatus.getDescription(), item.getProductName(), item.getQuantity());
            }
        }
    }

    /**
     * 按工作流进行订单状态流转（允许携带原因用于审计）
     *
     * @param id           订单 ID
     * @param targetStatus 目标状态
     * @param reason       操作原因/备注
     */
    @Override
    @Transactional
    public void transitionStatus(Long id, String targetStatus, String reason) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        
        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.valueOf(targetStatus);
        
        // 1) 状态机校验：是否允许从当前状态流转到目标状态
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new RuntimeException(String.format("订单状态不能从%s流转到%s", 
                currentStatus.getDescription(), newStatus.getDescription()));
        }
        
        // 2) 业务校验：库存/付款/发货/完成/取消/退款等规则
        validateStatusTransition(order, currentStatus, newStatus);
        
        // 3) 状态流转触发的库存扣减/恢复
        handleStockOperationOnStatusChange(order, currentStatus, newStatus);
        
        order.setStatus(newStatus);
        order.setUpdatedTime(LocalDateTime.now());
        orderMapper.updateStatus(order);

        // 4) 记录状态历史
        logStatusHistory(currentStatus, newStatus, reason, order);
        
        // reason 为“操作原因/备注”，用于审计与排查（当前主要体现在日志中）
        log.info("订单{}状态流转：{} -> {}，原因：{}", order.getOrderNo(), 
                currentStatus.getDescription(), newStatus.getDescription(), reason);
    }
    
    /**
     * 验证状态流转的业务逻辑
     */
    private void validateStatusTransition(Order order, OrderStatus currentStatus, OrderStatus newStatus) {
        // 配货中（PREPARING）前做库存校验：避免出现“进入配货才发现缺货”
        if (newStatus.requiresStockValidation()) {
            validateStockForPreparing(order);
        }
        
        // 发货（SHIPPED）前置条件校验
        if (newStatus == OrderStatus.SHIPPED) {
            validateForShipping(order);
        }
        
        // 完成（COMPLETED）前置条件校验
        if (newStatus == OrderStatus.COMPLETED) {
            validateForCompletion(order);
        }
        
        // 取消（CANCELLED）前置条件校验
        if (newStatus == OrderStatus.CANCELLED) {
            validateForCancellation(order, currentStatus);
        }
        
        // 退款（REFUNDED）前置条件校验
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

    /**
     * 取消订单（根据业务规则恢复库存/记录日志）
     *
     * @param id     订单 ID
     * @param reason 取消原因
     */
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

        // 记录状态历史
        logStatusHistory(currentStatus, OrderStatus.CANCELLED, reason, order);
        
        log.info("订单{}已取消，原因：{}", order.getOrderNo(), reason);
    }

    /**
     * 删除订单（若已付款则先恢复库存）
     *
     * @param id 订单 ID
     */
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

    /**
     * 获取订单状态流转历史
     *
     * @param id 订单 ID
     * @return 状态历史列表
     */
    @Override
    public List<OrderStatusHistory> getStatusHistory(Long id) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        return orderStatusHistoryMapper.findByOrderId(id);
    }

    /**
     * 记录订单状态变更历史
     */
    private void logStatusHistory(OrderStatus fromStatus, OrderStatus toStatus, String reason, Order order) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(order.getId());
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        history.setChangedTime(LocalDateTime.now());
        orderStatusHistoryMapper.insert(history);
    }
} 