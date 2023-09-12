package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ithema.reggie.common.BaseContext;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.AddressBook;
import com.ithema.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;


    /** 保存地址
     *
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook){
        //取出当前登录用户的id,装进 addressBook里
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info(addressBook.toString());
        //保存到数据库
        addressBookService.save(addressBook);
        return R.success("添加地址成功");
    }


    /** 将当前选择的地址改成默认地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook ){
        //update的条件构造器
        LambdaUpdateWrapper<AddressBook> queryWrapper=new LambdaUpdateWrapper<>();
        //将表里 该用户的 所有地址 的 is_default字段全部改成 0
        // where user_id=当前用户
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        // where is_default=0
        queryWrapper.set(AddressBook::getIsDefault,0);
        // 保存修改
        addressBookService.update(queryWrapper);
        //然后设置当前的 地址 的 is_default字段改成 1
        addressBook.setIsDefault(1);
        //保存修改
        addressBookService.updateById(addressBook);

        return R.success(addressBook);
    }


    /** 根据id查找地址
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id){
        AddressBook addressBook=addressBookService.getById(id);
        if(addressBook!=null){
            return R.success(addressBook);
        }
        return R.error("没有找到该对象");
    }


    /** 查找默认地址
     *
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        //查询 该用户的 默认地址 , 仅一个
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        if(addressBook==null){
            return R.error("没有找到该对象");
        }
        return R.success(addressBook);
    }

    @PutMapping
    public R<AddressBook> update(@RequestBody AddressBook addressBook){
        //根据地址id锁定 数据库里当前地址
        addressBookService.updateById(addressBook);//就是这么简单

        return R.success(addressBook);
    }




    /** 查询指定用户的全部地址信息
     *
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list( AddressBook addressBook  ){
        addressBook.setUserId(BaseContext.getCurrentId());// 这是干什么???
        log.info("addressBook:{}",addressBook);
        //条件构造器query
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        //根据id查询当前用户的所有地址
        return R.success(addressBookService.list(queryWrapper));
    }


}
