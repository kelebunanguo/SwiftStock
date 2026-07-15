package com.swiftstock.scheduler;

import com.swiftstock.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 定期关闭超过付款期限的待付款订单。
 */
@Slf4j
@Component
public class OrderTimeoutScheduler {

    private static final long PAYMENT_TIMEOUT_MINUTES = 30;

    private final OrderService orderService;

    public OrderTimeoutScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 每分钟扫描一次，取消创建超过 30 分钟且仍未付款的订单。
     */
    @Scheduled(fixedDelay = 1, initialDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void cancelExpiredUnpaidOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        int cancelledCount = orderService.cancelExpiredUnpaidOrders(deadline);

        if (cancelledCount > 0) {
            log.info("本次自动取消{}个超过30分钟未付款的订单", cancelledCount);
        }
    }
}
