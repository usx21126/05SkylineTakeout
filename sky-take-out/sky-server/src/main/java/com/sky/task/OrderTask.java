package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，处理超时未支付订单与一直配送中的订单
 */
@Component
public class OrderTask {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderMapper orderMapper;
    /**
     * 每分钟检查超时订单：状态为待付款订单时间超过15min自动取消
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void processOutTimeOrder(){
        LocalDateTime time = LocalDateTime.now().minusMinutes(15);
        List<Orders> ordersList = orderMapper.getOrdersByStatusAndTime(Orders.PENDING_PAYMENT,time);
        if(ordersList!=null && !ordersList.isEmpty()){
            ordersList.forEach(order -> {
                order.setStatus(Orders.CANCELLED);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason(MessageConstant.PAYMENT_TIMEOUT);
                orderMapper.update(order);
            });
        }
    }

    /**
     * 每天凌晨1点检查状态为派送中订单修改为已完成
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void processDeliveryOrder(){
        LocalDateTime time = LocalDateTime.now().minusHours(1);
        List<Orders> ordersList = orderMapper.getOrdersByStatusAndTime(Orders.DELIVERY_IN_PROGRESS, time);

        if(ordersList!=null && !ordersList.isEmpty()){
            ordersList.forEach(order -> {
                order.setStatus(Orders.COMPLETED);
                order.setDeliveryTime(time);
                orderMapper.update(order);
            });
        }
    }
}
