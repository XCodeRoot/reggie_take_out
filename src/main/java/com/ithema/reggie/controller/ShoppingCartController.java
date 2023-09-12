package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ithema.reggie.common.BaseContext;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.ShoppingCart;
import com.ithema.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    /** 添加菜品到购物车
     *
     *
     * @param shoppingCart
     * @return R<ShoppingCart> 因为需要回显给前端,所以要返回添加进购物车的对象,这样前端就加进购物车里
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        Long userId = BaseContext.getCurrentId();//获取userId
        shoppingCart.setUserId(userId);

        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper=new LambdaQueryWrapper<>();
        // where user_id=?
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        //查询当前菜品 或者 套餐 ,是否在购物车中 ( 判断是否是第一次添加)
        if(shoppingCart.getDishId()!=null){
            //如果是添加菜品, 根据 dish_id  查是否存在
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else{
            //如果是添加套餐 ,根据 setmeal_id 和 user_id 查是否存在
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //查询当前菜品或者套餐是否 已经存在在购物车里
        ShoppingCart cartServiceOne = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);
        if(cartServiceOne!=null){
            //如果存在,则 数量加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            //保存到数据库
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //如果不在购物车,则新建购物车,保存到数据库
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //统一操作cartServiceOne
            cartServiceOne=shoppingCart;//因为保存到数据库,会使用雪花算法生成id,回显到 java对象shoppingCart里,我们再回显给前端
        }

        return R.success(cartServiceOne);//因为保存到数据库,会使用雪花算法生成id,回显到 java对象shoppingCart里,我们再回显给前端
    }


    /** 查询购物车信息list
     *
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        //where user_id=?
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        //排序,以创建时间,降序
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        //返回list
        return R.success(list);
    }


    @DeleteMapping("clean")
    public R<String> clean(){

        //根据 用户id ,删除该用户所有的购物车项
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId() );

        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }

}
