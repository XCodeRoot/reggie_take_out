package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.Category;
import com.ithema.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    /** 新增菜品分类
     *
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){

        if (category!=null) {
            categoryService.save(category);
            return R.success("新增分类成功");
        }
        return R.error("新增分类失败");
    }



    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        //1.构造分页构造器
        Page<Category> pageInfo = new Page(page, pageSize);
        //2.构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        //3.添加排序条件
        queryWrapper.orderByAsc(Category::getSort);
        //4.查询数据库
        categoryService.page(pageInfo,queryWrapper);
        //5.返回分页数据对象
        return R.success(pageInfo);
    }


    /** 根据id 删除分类
     *
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id){
        log.info("删除分类,id为: {}",id);
        categoryService.remove(id);//service里定义的方法
        return R.success("删除分类成功");
    }


    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息: {} ",category);

        categoryService.updateById(category);

        return R.success("修改分类信息成功");
    }


    @GetMapping("/list")
    public R<List<Category>> list(Category category){//返回下拉的菜品分类分类

        //条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper=new LambdaQueryWrapper<>();

        //当不为空时,添加查询条件 : type等值查询
        lambdaQueryWrapper.eq(category.getType()!=null,Category::getType,category.getType());

        //添加排序条件: 先以sort升序排,再以更新时间 降序排
        lambdaQueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        //查询出这些菜品分类
        List<Category> list = categoryService.list(lambdaQueryWrapper);
        //返回结果集给前端
        return R.success(list)  ;
    }

}
