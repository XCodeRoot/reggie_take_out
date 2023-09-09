package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ithema.reggie.common.R;
import com.ithema.reggie.dto.SetmealDto;
import com.ithema.reggie.entity.Category;
import com.ithema.reggie.entity.Dish;
import com.ithema.reggie.entity.Setmeal;
import com.ithema.reggie.service.CategoryService;
import com.ithema.reggie.service.SetmealDishService;
import com.ithema.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/** 套餐管理
 *
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;


    @Autowired
    private SetmealDishService setmealDishService;


    @Autowired
    private CategoryService categoryService;


    /** 添加新的套餐 信息
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        //保存套餐,同时保存 套餐和菜品的关联关系
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }


    /** 删除套餐 (批量和单一删除都行)
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids ){

        log.info("ids={}",ids);
        setmealService.removeWithDish(ids);
        return null;
    }




    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        //新的分页构造器对象
        Page<SetmealDto> setmealDtoPage  = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        //添加模糊查询条件:
        queryWrapper.like(name!=null ,Setmeal::getName,name );
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //封装好分页对象
        setmealService.page(pageInfo,queryWrapper);

        //拷贝分页构造器对象, 但,忽略 records
        //把 pageInfo拷贝到新的 setmealDtoPage
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");

        //取出records,也就是 setmeal对象集合
        List<Setmeal> records = pageInfo.getRecords();

        //前端还需要 categoryName这个属性,但是我们现在的 setmeal对象里,又没有这个属性
        //所以需要使用 SetmealDto对象, 重构pageInfo里的 records 数据,也就是重构每条 setmeal套餐信息成setmealDto对象,再补上categoryName属性
        List<SetmealDto> list = records.stream().map((item) -> {
            //新建Dto对象
            SetmealDto setmealDto = new SetmealDto();
            //拷贝 父类属性
            BeanUtils.copyProperties(item, setmealDto);
            //设置 categoryName , 先根据categoryId查出category,再取出categoryName
            Category category = categoryService.getById(item.getCategoryId());
            String categoryName = category.getName();
            setmealDto.setCategoryName(categoryName);

            //返回
            return setmealDto;
        }).collect(Collectors.toList());//收集返回的setmealDto,然后合并成集合list

        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }
}
