package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ithema.reggie.common.R;
import com.ithema.reggie.dto.DishDto;
import com.ithema.reggie.entity.Category;
import com.ithema.reggie.entity.Dish;
import com.ithema.reggie.service.CategoryService;
import com.ithema.reggie.service.DishFlavorService;
import com.ithema.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;


    /** 保存菜品信息,和口味数据
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){ // DishDTO继承了Dish的所有属性,并拓展了另外几个属性
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }


    /** 修改菜品时 , 回显一下菜品信息和口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        //通过菜品id获取菜品信息,同时获取口味信息
        return R.success(dishService.getByIdWithFlavor(id));
    }


    /** 修改保存菜品信息,和口味数据
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){ // DishDTO继承了Dish的所有属性,并拓展了另外几个属性
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }




    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        //分页构造器对象
        Page<Dish> pageInfo=new Page<>(page,pageSize);
        //DishDto对象,因为前端要求的数据,多了一个categoryName,这个需要在DishDto里追加,DishDto又继承了Dish
        Page<DishDto> dishDtoPage=new Page<>(page,pageSize);

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        //添加模糊查询条件
        queryWrapper.like(name!=null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //分页
        dishService.page(pageInfo,queryWrapper);

        //为什么要拷贝? 因为当前控制类使用的是 DishService, 所以只能使用 Dish类型的 mapper来查数据库
        //将分页好的 Dish 类型的分页对象 , 拷贝给 DishDto类型的分页对象
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        //下面来处理 records, 前端需要 新的分页records里,有 categoryName这个数据,我们就去数据库查,再修改
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list= records.stream().map((item)->{ //item是 records的单数指代,遍历修改每个record, 每个record都是一个DishDto对象
            DishDto dishDto = new DishDto();
            //先拷贝一下PageInfol里的record也就是Dish的数据,之前拷贝的时候 忽略了records数据
            BeanUtils.copyProperties(item,dishDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //通过分类id查询 分类对象,然后再填充 categoryName
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                dishDto.setCategoryName(category.getName());
            }
            //返回 记录
            return dishDto;

        }).collect(Collectors.toList());


        //设置 新的records
        dishDtoPage.setRecords(list);

        //返回 新的 分页数据
        return R.success(dishDtoPage);
    }


}
