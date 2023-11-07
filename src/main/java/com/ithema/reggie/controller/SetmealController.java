package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ithema.reggie.common.R;
import com.ithema.reggie.dto.DishDto;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @CacheEvict(value = "setmeal",key="#setmealDto.categoryId +'_'+#setmealDto.status")
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
//    @CacheEvict(value = "setmeal",allEntries = true)//如果删除了套餐,就立马清空所有套餐缓存
    @CacheEvict(value = "setmeal",key="#setmeal.categoryId +'_'+#setmeal.status")
    public R<String> delete(@RequestParam("ids") List<Long> ids ){

        log.info("ids={}",ids);
        setmealService.removeWithDish(ids);
        return R.success("删除套餐成功");
    }


    /** 在前端页面,显示 套餐的列表,及其对象信息
     *
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    //有就从缓存拿,没有就查数据库,然后再缓存
    @Cacheable(value = "setmeal",key="#setmeal.categoryId +'_'+#setmeal.status")
    public R<List<Setmeal>> list( Setmeal setmeal){//前端传了categoryId 和 status两个
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        //根据 categoryId和status 查出 多个 setmeal对象
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        //返回给前端
        return R.success(list);
    }

    /** 修改套餐时 , 回显一下套餐信息
     *
     * @param id
     * @return
     */
//    @GetMapping("/{id}")
//    public R<Setmeal> get(@PathVariable Long id){
//
//        //错了
//        return R.success(setmealService.getById(id));
//    }

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
