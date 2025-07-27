package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 添加订单
     * @param orders
     */
    void addOrder(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    Orders getOrderById(Long id);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO getOrderStatistics();

    /**
     * 查询指定时间内和状态为已完成的订单
     * @param status
     * @param time
     * @return
     */
    List<Orders> getOrdersByStatusAndTime(Integer status, LocalDateTime time);

    /**
     * 统计每日营业额
     * @param map
     * @return
     */
    Double getTotalAmountByStatusAndDate(Map map);

    /**
     * 查询指定时间和状态内的订单数量
     * @param map
     * @return
     */
    Integer getCountByStatusAndDate(Map map);

    /**
     * 查询销量排名top10
     * @param map
     * @return
     */
    List<GoodsSalesDTO> getSumTop10(Map map);
}
