package com.ithema.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithema.reggie.dto.DishDto;
import com.ithema.reggie.entity.Dish;
import com.ithema.reggie.entity.DishFlavor;
import com.ithema.reggie.mapper.DishMapper;
import com.ithema.reggie.service.DishFlavorService;
import com.ithema.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;


    //新增菜品,操作两张表, dish 和 dish_flavor


    /** 新增菜品,同时保存口味数据(有哪些可供选择的甜度,忌口,辣度 等)
     *
     * @param dishDto
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {

        //保存菜品的基本信息到 菜品表 dish
        save(dishDto);//因为已经继承了dish属性,所以直接传dto就行了

        //菜品id , 在json转实体类的时候就已经完成了id的自动生成,我们只需要取出来就行了
        Long dishId = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{ // stream流的方式 来操作map, 将所有flavors里的 dishId 赋值
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());//先转为map最终又转为集合list

        //保存口味数据到 口味表 dish_flavor
        dishFlavorService.saveBatch(flavors);
    }


    /** 通过菜品id获取菜品信息,同时获取口味信息
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询dish菜品信息,从dish表查
        Dish dish = this.getById(id);

        //拷贝dish到dto里 , 将两个 对象全部装进 DTO里
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);


        //查询口味信息,从 dish_flavor查
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);

        //设置dto里的 List<DishFlavor> 属性
        dishDto.setFlavors(list);


        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表的基本信息
        this.updateById(dishDto);//因为 多态, 有继承关系,所以可以这样
        //删除数据库原有的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //取出前端传来的口味信息
        List<DishFlavor> flavors = dishDto.getFlavors();

        //添加当前的口味信息
        flavors=flavors.stream().map((item)->{//遍历前端传来的每条flavor,手动设置口味信息的 dish_id
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());//收集返回的item,然后组成集合

        dishFlavorService.saveBatch(flavors);
    }
}
