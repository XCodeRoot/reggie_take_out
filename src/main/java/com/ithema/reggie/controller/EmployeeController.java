package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.Employee;
import com.ithema.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    /** 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login") //HttpServletRequest 用于创建session,将用户id保存到session里
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面表单提供的 password 进行md5加密处理
        String password = employee.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据用户名查询数据库,因为用户名唯一性
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());//添加查询条件:等值查询
        Employee emp = employeeService.getOne(queryWrapper);//查询一条数据
        //3.没查询到,就返回失败
        if(emp==null){
            return R.error("登录失败");
        }
        //4.如果查询到就比较密码
        if(!emp.getPassword().equals(password)){
            //如果比较失败,就返回错误
            return R.error("密码错误");
        }
        //5.比对禁用状态,返回员工已禁用结果
        if (emp.getStatus()==0){
            return R.error("该员工账号被禁用");
        }

        //6.保存 员工id到session
        request.getSession().setAttribute("employee",emp.getId());

        //7.返回整个emp对象给前端
        return R.success(emp);
    }


    /** 员工 退出登录状态
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }



    @PostMapping// restfull风格,post就是保存Employee对象到数据库
    public R<String> save(HttpServletRequest request , @RequestBody Employee employee){

        log.info("新增员工信息",employee.toString());
        //1.设置初始密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));//加密初始密码

        //设置 创建时间和更新时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //取出登录者的 员工id
        Long empId = (Long)request.getSession().getAttribute("employee");

        //设置 创建人和更新人
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        //保存到数据库
        employeeService.save(employee);

        return R.success("新员工添加成功");

    }


    /**
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page>  page(int page,int pageSize,String name){//前端发送的请求,把这些参数拼接在请求路径的问号后面了

        log.info("page={} , pageSize={}, name={} ",page,pageSize,name);

        //1.构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //2.构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //3.添加过滤条件 :  like条件,什么 like 什么 , 添加条件是:name不为空
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //4.添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //5.执行查询
        employeeService.page(pageInfo,queryWrapper);
        //5.返回分页数据给前端,进行解析
        return R.success(pageInfo);
    }



    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody  Employee employee){
        log.info(employee.toString());

        //从session中取出 ,当前登录的员工id
        Long empId = (Long)request.getSession().getAttribute("employee");
        //设置更新操作人
        employee.setUpdateUser(empId);
        //设置更新时间
        employee.setUpdateTime(LocalDateTime.now());

        //保存 修改到数据库
        employeeService.updateById(employee);

        //返回成功
        return R.success("员工信息修改成功");
    }


    /** 编辑员工信息时 , 根据id回显员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息");
        Employee employee = employeeService.getById(id);//编辑时 , 回显员工信息
        if (employee!=null){
            return R.success(employee);//返回给前端 , 让前端回显信息
        }
        return R.error("没有查询到该员工信息");
    }

}
