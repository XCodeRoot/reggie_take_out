package com.ithema.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.ithema.reggie.common.BaseContext;
import com.ithema.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/** 原生的 过滤器 Filter ,需要在主程序 进行 组件扫描
 *
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;


        //1.获取请求的URI
        String requestURI = request.getRequestURI();

        log.info("拦截到请求: {}",requestURI);


        //定义不需要过滤的请求路径
        String[] urls=new String[]{
                "/backend/**",
                "/front/**",
                "/employee/login",
                "/employee/logout",
                "/common/**",
                "/user/login",
                "/user/sendMsg"
        };
        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3.如果不需要处理,则直接放行就好了
        if(check){
            log.info("放行请求: {}",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        //4-1.如果不放行,就检查登录状态,登录了 就放行
        if(request.getSession().getAttribute("employee")!=null){
            log.info("用户已登录,用户id为: {}",request.getSession().getAttribute("employee"));

            //从session中取出当前登录的员工的id
            Long empId = (Long) request.getSession().getAttribute("employee");
            //将empId 保存到当前线程中
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        //4-2.判断移动端用户登录状态
        if(request.getSession().getAttribute("user")!=null){
            log.info("用户已登录,用户id为: {}",request.getSession().getAttribute("user"));

            //从session中取出当前登录的员工的id
            Long userId = (Long) request.getSession().getAttribute("user");
            //将empId 保存到当前线程中
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }



        //5.如果未登录则返回未登录结果,通过输出流的方式,想客户端页面响应数据
        log.info("用户未登录");

        //因为backend页面里的  request.js里,标注了要接收json类型的错误msg
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;


    }



    /** 判断本次请求是否需要放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){

        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);//是否匹配上
            if(match){//如果匹配上,返回,然后放行该请求
                return true;
            }
        }
        return false;
    }
}
