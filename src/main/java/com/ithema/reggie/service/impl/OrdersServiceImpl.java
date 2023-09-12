package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.common.BaseContext;
import com.ithema.reggie.common.CustomException;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.*;
import com.ithema.reggie.mapper.OrdersMapper;
import com.ithema.reggie.service.*;
import com.ithema.reggie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Override
    public R<String> submit(Orders orders) {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();
        //根据用户id,查数据库里的 购物车信息
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if(shoppingCarts==null||shoppingCarts.size()==0){
            throw new CustomException("购物车为空,不能下单");
        }
        //查询用户数据
        User user = userService.getById(userId);
        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        //根据地址id,查出地址信息
        AddressBook addressBook = addressBookService.getById(addressBookId);
        //如果地址信息为空,就不能下单
        if(addressBook==null){
            throw new CustomException("用户地址为空,不能下单");
        }
    //===============================填充订单明细表==========================================
        //生成订单id
        long orderId = IdWorker.getId();
        //原子整型,保证多线程的情况下,维持 累加的 原子性
        AtomicInteger amount=new AtomicInteger(0);//累加购物车的商品金额

        //将购物车里的所有数据遍历一遍,然后填充进 订单明细表里
        List<OrderDetail> orderDetailList = shoppingCarts.stream().map((item) -> {
            //新建订单明细对象
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            //返回 单个 订单明细对象
            return orderDetail;

        }).collect(Collectors.toList());//收集所有订单明细对象,组合成list

    //=================================填充订单表========================================
        //填充订单信息
        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId));
        orders.setStatus(2);
        orders.setUserId(userId);
        orders.setAddressBookId(addressBookId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setAmount(new BigDecimal(amount.get()));//计算总金额
        orders.setPhone(addressBook.getPhone());
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(
                (addressBook.getProvinceName() == null ? "":addressBook.getProvinceName())+
                        (addressBook.getCityName() == null ? "":addressBook.getCityName())+
                        (addressBook.getDistrictName() == null ? "":addressBook.getDistrictName())+
                        (addressBook.getDetail() == null ? "":addressBook.getDetail())
        );

        //向订单表插 一条数据
        this.save(orders);
        //向订单明细表,插多条 订单明细数据
        orderDetailService.saveBatch(orderDetailList);
        //清空购物车
        shoppingCartService.remove(queryWrapper);//使用刚开始用的queryWrapper
        return null;
    }
}
