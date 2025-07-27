package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.UserNotLoginException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;
    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String ak;
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 检查用户的配送范围是否超出配送范围
        checkOutOfRange(addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getUserById(userId);
        if(user == null){
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        List<ShoppingCart> cartList = shoppingCartMapper.getShoppingCartListByUserId(userId);
        if(cartList == null || cartList.isEmpty()){
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //构造order
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setNumber(System.currentTimeMillis()+"");
        orders.setStatus(Orders.PENDING_PAYMENT);    //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());    //收货人手机
        orders.setAddress(addressBook.getDetail()); //详细地址
        orders.setUserName(user.getName()); //下单人
        orders.setConsignee(addressBook.getConsignee());    //收货人
        orderMapper.addOrder(orders);
        log.info("订单id:{}",orders.getId());
        //构造order_detail
        List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>();
        cartList.forEach(cart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail,"id");
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        });
        orderDetailMapper.addOrderDetail(orderDetailList);
        //清空购物车
        shoppingCartMapper.deleteShoppingCartByUserId(userId);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime()).build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getUserById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));

        /////////////////////////////
        //模拟支付成功 - 修改订单状态 0未支付 - 1已支付
        OrderPaymentVO vo = new OrderPaymentVO();
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);

        Map map = new HashMap();
        map.put("type", 1);//消息类型，1表示来单提醒
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + outTradeNo);

        //通过WebSocket实现来单提醒，向客户端浏览器推送消息
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 查询历史订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @Transactional
    @Override
    public PageResult getHistoryOrders(Integer page, Integer pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = OrdersPageQueryDTO.builder()
                .userId(BaseContext.getCurrentId())
                .status(status).build();
        Page<Orders> orderPages = orderMapper.getHistoryOrders(ordersPageQueryDTO);
        List<OrderVO> orderVOList = new ArrayList<>();
        if(orderPages != null && orderPages.getTotal() > 0){
            for (Orders order : orderPages.getResult()) {
                OrderVO orderVO = new OrderVO();
                List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailByOrderId(order.getId());
                orderVO.setOrderDetailList(orderDetailList);
                BeanUtils.copyProperties(order, orderVO);
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(orderPages.getTotal(), orderVOList);
    }

    /**
     * 根据id查询订单详情
     * @param id
     * @return
     */
    @Transactional
    @Override
    public OrderVO getOrderDetail(Long id) {
        Orders order = orderMapper.getOrderById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailByOrderId(id);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 根据订单id取消订单(更新订单状态)
     * @param id
     * @throws Exception
     */
    @Transactional
    @Override
    public void cancelOrderById(Long id) throws Exception {
        Orders order = orderMapper.getOrderById(id);
        if(order == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(order.getStatus() > 2) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        order.setStatus(Orders.CANCELLED);
            // 待付款 -- 取消订单，未付款      待接单 -- 退款，取消订单，退款
        if(order.getStatus().equals(Orders.TO_BE_CONFIRMED) ){
//          weChatPayUtil.refund(order.getNumber(),order.getNumber(),order.getAmount(),order.getAmount());
            order.setPayStatus(Orders.REFUND);
        }
        order.setCancelReason(MessageConstant.USER_CANCEL_ORDER);
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 再来一单
     * @param id
     */
    @Transactional
    @Override
    public void repetition(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailByOrderId(id);
        if(orderDetailList == null || orderDetailList.isEmpty()) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        for(OrderDetail orderDetail : orderDetailList){
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart,"id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.addShoppingCart(shoppingCart);
        }
    }

    /**
     * 根据条件搜索订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    @Transactional
    public PageResult getOrderPages(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> historyOrders = orderMapper.getHistoryOrders(ordersPageQueryDTO);
        return new PageResult(historyOrders.getTotal(),getOrderVOList(historyOrders));
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO getOrderStatistics() {
        return orderMapper.getOrderStatistics();
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    @Transactional
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        Orders order = orderMapper.getOrderById(ordersConfirmDTO.getId());
        if(order == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        order.setStatus(Orders.CONFIRMED);
        orderMapper.update(order);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Orders order = orderMapper.getOrderById(ordersRejectionDTO.getId());
        if(order == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        order.setStatus(Orders.CANCELLED);
        order.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(ordersRejectionDTO.getRejectionReason());
        if(order.getPayStatus().equals(Orders.PAID)){
//            weChatPayUtil.refund(order.getNumber(),order.getNumber(),order.getAmount(),order.getAmount());
        }

        orderMapper.update(order);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @throws Exception
     */
    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Orders order = orderMapper.getOrderById(ordersCancelDTO.getId());
        if(order == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(order.getStatus() <= 2) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        order.setStatus(Orders.CANCELLED);
        if(order.getPayStatus().equals(Orders.PAID)){
//            weChatPayUtil.refund(order.getNumber(),order.getNumber(),order.getAmount(),order.getAmount());
        }
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 配送订单
     * @param id
     */
    @Override
    public void deliveryOrder(Long id) {
        Orders order = orderMapper.getOrderById(id);
        if(order == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(order.getPayStatus().equals(Orders.CONFIRMED)) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);{}
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(order);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void completeOrder(Long id) {
        Orders order = orderMapper.getOrderById(id);
        if(order == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(order.getPayStatus().equals(Orders.DELIVERY_IN_PROGRESS)) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        order.setStatus(Orders.COMPLETED);
        order.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders order = orderMapper.getOrderById(id);
        if(order == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        Map map = new HashMap();
        map.put("type",2);
        map.put("orderId", id);
        map.put("content","订单号"+order.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 方法：根据order列表信息 补充orderDetailList字段
     * @param page
     * @return
     */
    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailByOrderId(orders.getId());
                orderVO.setOrderDetailList(orderDetailList);
                String orderDishes = getOrderDishesStr(orderDetailList);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orderDetailList
     * @return
     */
    private String getOrderDishesStr(List<OrderDetail> orderDetailList) {
        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());
        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }
    /**
     * 检查客户的收货地址是否超出配送范围
     * @param address
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 5000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }

}
