package com.ithema.reggie.dto;


import com.ithema.reggie.entity.Dish;
import com.ithema.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish { // 继承了Dish实体类的所有属性 , 并扩展了以下 几个属性

    private List<DishFlavor> flavors = new ArrayList<>();//接收DishFlavor实体集

    private String categoryName;//接收菜品分类名称

    private Integer copies;//
}
