package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.Employee;
import com.ithema.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

}
