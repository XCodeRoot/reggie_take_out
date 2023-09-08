package com.ithema.reggie.controller;


import com.ithema.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController   {

    @Value("${reggie.path}")
    private String basePath;//从yaml里装配指定的属性


    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //file是一个临时文件,需要手动转存,否则本次请求后,会被删除
        log.info(file.toString());

        //获取原始文件名
        String originalFilename = file.getOriginalFilename();
        //获取文件名后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用UUID生成随机文件名,防止重复
        String fileName = UUID.randomUUID().toString() + suffix;

        //创建一个目录,存放这个文件
        File dir = new File(basePath);
        //判断是否已经有该目录了
        if (!dir.exists()){
            dir.mkdirs();//目录不存在就创建
        }


        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(fileName);
    }



    @GetMapping("/download")
    public void download( String name, HttpServletResponse response  ){

        try {
            //输入流 , 通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));//路径+文件名
            //输出流,通过输出流,将文件写回浏览器,在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            //设置响应文件的类型
            response.setContentType("image/jpeg");
            //写回浏览器
            int len=0;
            byte[] bytes = new byte[2048];
            while((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
