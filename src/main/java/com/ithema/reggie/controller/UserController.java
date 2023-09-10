package com.ithema.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ithema.reggie.common.R;
import com.ithema.reggie.entity.User;
import com.ithema.reggie.service.UserService;
import com.ithema.reggie.utils.SMSUtils;
import com.ithema.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            //生成随机4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);
            //调用阿里云api完成短信发送
            //SMSUtils.sendMessage("","",phone,code);
            //将验证码保存到session中
            session.setAttribute(phone,code);

            return R.success("手机验证码短信发送成功");
        }
        return R.error("手机验证码短信发送成功");
    }


    /** 移动端用户登录
     *
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //获取session中的验证码
        Object codeInSession = session.getAttribute(phone);
        //比对验证码
        if(codeInSession!=null&&codeInSession.equals(code)){
            //判断手机号是否存在
            //不存在就保存到数据库,创建新用户

            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user==null){
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败");

    }
}
