package com.ithema.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithema.reggie.dto.SetmealDto;
import com.ithema.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /** 新增套餐,同时要保存套餐和菜品的关联关系
     *
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);


    /** 删除套餐, 同时还要删除 套餐关联的 内部菜品信息
     *
     * @param ids
     */
    public void removeWithDish(List<Long> ids);
}
