package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量添加订单详情
     * @param orderDetailList
     */
    void addOrderDetail(List<OrderDetail> orderDetailList);

    /**
     * 根据orderId查询订单详情
     * @param orderId
     */
    List<OrderDetail> getOrderDetailByOrderId(Long orderId);
}
