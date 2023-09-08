package com.ithema.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler { //自定义元数据处理器,实现mbp指定的 元数据处理器接口



    @Override
    public void insertFill(MetaObject metaObject) {//执行插入操作时,会获取元数据,并对元数据进行 公共字段填充
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString());



        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());

    }

    @Override
    public void updateFill(MetaObject metaObject) {//执行更新操作时,会获取元数据,并对元数据进行 公共字段填充
        log.info("公共字段自动填充[update]...");
        log.info(metaObject.toString());


        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
