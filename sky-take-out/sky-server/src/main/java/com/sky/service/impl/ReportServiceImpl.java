package com.sky.service.impl;


import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        // 1.DateList
        List<LocalDate> dateList = getDateList(begin, end);
        // 2.turnoverList
        List<Double> turnoverList = new ArrayList<>();
        dateList.forEach(date->{
            Map map = new HashMap();
            map.put("status", Orders.CANCELLED);
            map.put("beginTime", LocalDateTime.of(date,LocalTime.MIN));
            map.put("endTime", LocalDateTime.of(date,LocalTime.MAX));
            Double turnover = orderMapper.getTotalAmountByStatusAndDate(map);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        });
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        // 1.dateList
        List<LocalDate> dateList = getDateList(begin, end);
        // 2.newUserList & totalUserList
        List<Integer> newUserList = new ArrayList<>();
        List<AtomicReference<Integer>> totalUserList = new ArrayList<>();
        Map total_map = new HashMap();
        total_map.put("beginTime",null);
        total_map.put("endTime",LocalDateTime.of(begin,LocalTime.MIN));
        AtomicReference<Integer> total = new AtomicReference<>(userMapper.getTotalUserByDate(total_map));
        dateList.forEach(date->{
            Map map = new HashMap();
            map.put("beginTime", LocalDateTime.of(date,LocalTime.MIN));
            map.put("endTime", LocalDateTime.of(date,LocalTime.MAX));
            Integer newUser = userMapper.getTotalUserByDate(map);
            newUser = newUser == null ? 0 : newUser;
            newUserList.add(newUser);
            Integer finalNewUser = newUser;
            total.updateAndGet(v -> v + (finalNewUser));
            totalUserList.add(total);
        });
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 订单统计接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        // 1.dateList
        List<LocalDate> dateList = getDateList(begin, end);
        // 2.orderCountList & validOrderCountList
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();
        dateList.forEach(date->{
            Map map = new HashMap();
            map.put("status", Orders.COMPLETED);
            map.put("beginTime",LocalDateTime.of(date,LocalTime.MIN));
            map.put("endTime",LocalDateTime.of(date,LocalTime.MAX));
            Integer validOrderCount = orderMapper.getCountByStatusAndDate(map);
            map.put("status", null);
            Integer orderCount = orderMapper.getCountByStatusAndDate(map);
            orderCountList.add(orderCount);
            validOrderList.add(validOrderCount);
        });
        // 3.totalOrderCount & validOrderCount
//        Map total_map = new HashMap();
//        total_map.put("beginTime", LocalDateTime.of(begin,LocalTime.MIN));
//        total_map.put("endTime", LocalDateTime.of(end,LocalTime.MAX));
//        Integer totalOrderCount = orderMapper.getCountByStatusAndDate(total_map);
//        total_map.put("status", Orders.COMPLETED);
//        Integer validOrderCount = orderMapper.getCountByStatusAndDate(total_map);
        Integer totalOrderCount = orderCountList.stream().reduce(0,Integer::sum);
        Integer validOrderCount = validOrderList.stream().reduce(0,Integer::sum);

        // 4.orderCompletionRate
        BigDecimal rate;
        if (totalOrderCount == 0) {
            rate = BigDecimal.ZERO; // 或其他默认值如 null
        } else {
            BigDecimal bd = new BigDecimal((double) validOrderCount / totalOrderCount);
            bd = bd.setScale(4, RoundingMode.HALF_UP);
            rate = bd;
        }
        Double orderCompletionRate = rate.doubleValue();
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 查询销量排名top10接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        Map map = new HashMap();
        map.put("status", Orders.COMPLETED);
        map.put("beginTime",LocalDateTime.of(begin,LocalTime.MIN));
        map.put("endTime",LocalDateTime.of(end,LocalTime.MAX));
        List<GoodsSalesDTO> goodsSalesDTOListo = orderMapper.getSumTop10(map);
        goodsSalesDTOListo.forEach(goodsSalesDTO->{
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber());
        });
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        String originalFilename = "运营数据报表模板.xlsx";
        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/" + originalFilename);

        try {
            //基于模板文件创建一个新的Excel文件运营数据报表模板.xlsx
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取日期列表
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        return dateList;
    }
}
