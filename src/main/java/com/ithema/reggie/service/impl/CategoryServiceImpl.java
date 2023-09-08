package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.common.CustomException;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.Category;
import com.ithema.reggie.entity.Dish;
import com.ithema.reggie.entity.Setmeal;
import com.ithema.reggie.mapper.CategoryMapper;
import com.ithema.reggie.service.CategoryService;
import com.ithema.reggie.service.DishService;
import com.ithema.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;


    @Autowired
    private SetmealService setmealService;



    /** 根据id,删除分类,删除前,检查是否有菜品关联
     *
     * @param id
     */
    @Override
    public void remove(Long id) {
        //1.查询Dish菜品 是否关联了该分类,根据 该分类的id 进行查询
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);

        //2.查询当前分类是否关联了菜品,
        if(count1>0){
            //3.如果关联,则抛出一个业务异常
            throw new CustomException("当前分类下关联了菜品,无法删除");
        }

        //4.查询Setmeal套餐 是否关联了该分类,根据 该分类的id 进行查询
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);

        //5.查询当前分类是否关联了套餐,
        if(count2>0){
            //6.如果关联,则抛出一个业务异常
            throw new CustomException("当前分类下关联了套餐,无法删除");
        }
        //7.删除分类
        super.removeById(id);
    }




}
