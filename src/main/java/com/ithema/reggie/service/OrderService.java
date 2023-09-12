package com.ithema.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.Orders;



public interface OrderService extends IService<Orders> {
    R<String> submit(Orders orders);
}
