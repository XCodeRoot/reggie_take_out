package com.ithema.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithema.reggie.entity.Category;

public interface CategoryService extends IService<Category> {

    public void remove(Long id);//移除分类
}
