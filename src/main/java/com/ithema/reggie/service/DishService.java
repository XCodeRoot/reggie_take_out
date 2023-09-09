package com.ithema.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithema.reggie.dto.DishDto;
import com.ithema.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    public void saveWithFlavor(DishDto dishDto);

    public DishDto getByIdWithFlavor(Long id);//通过菜品id获取菜品信息,同时获取口味信息

    public void updateWithFlavor(DishDto dishDto);
}
