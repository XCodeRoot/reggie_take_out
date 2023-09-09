package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.common.CustomException;
import com.ithema.reggie.dto.SetmealDto;
import com.ithema.reggie.entity.Setmeal;
import com.ithema.reggie.entity.SetmealDish;
import com.ithema.reggie.mapper.SetmealMapper;
import com.ithema.reggie.service.SetmealDishService;
import com.ithema.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {


    @Autowired
    private SetmealDishService setmealDishService;


    /** 新增套餐,同时要保存套餐和菜品的关联关系
     *
     * @param setmealDto
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);//因为SetmealDto继承了Setmeal,所以有setmeal的所有属性,根据表的映射关系,就可以保存进表里了

        //保存套餐和菜品的关联信息,操作 setmeal_dish ,执行insert操作,
        // 实际上就是 setmeal表保存套餐外显信息,setmeal_dish保存套餐内部的所有菜品信息

        //取出dto里,前端提交的 套餐里的所有菜品信息,这些菜品对象,是没有 setmealId 的 ,需要我们手动注入
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes= setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());//因为mbp框架,自动有回显功能,插入新数据后,雪花算法生成id,会回显到插入前的对象中
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);

    }

    /** 删除套餐, 同时还要删除 套餐关联的 内部菜品信息
     *
     * @param ids
     */
    @Transactional
    @Override
    public void removeWithDish(List<Long> ids) {
        //查询当前套餐状态,如果是在售,则不能删除
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        //查询条件: in关键字匹配(1,2,3)这样的 , 然后 status=1
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        //count *
        int count = this.count(queryWrapper);
        if(count>0  ){
            //说明查出来了,但是 正在售卖不能删除
            //抛一个自定义异常,在全局异常处理器,捕捉一下
            throw new CustomException("当前套餐正在售卖,不能删除");
        }

        //如果不在售卖,就可以删
        //先删  套餐表的数据
        this.removeByIds(ids);//批量删除

        //再删除 套餐菜品关系表的数据
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper);
    }
}
