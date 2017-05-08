package com.github.jackl.elk.core.service;

import com.alibaba.fastjson.JSON;
import com.github.jackl.elk.core.dao.CompanyDao;
import com.github.jackl.elk.core.entity.Company;
import com.github.jackl.elk.core.util.RedisUtil;
import com.github.jackl.elk.proxy.entity.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jackl on 2017/5/7.
 */
@Component
public class CompanyService {
    private Logger _logger= LoggerFactory.getLogger(getClass());
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    CompanyDao companyDao;

    public void test(){
        String key="_proxies";
        int count=15;
        List<Proxy> list=new ArrayList<>();
        for (int i = 0; i <count ; i++) {
            Proxy p=new Proxy("8.8.8."+i,1000+i,1000l);
            redisUtil.inList(key,JSON.toJSONString(p));
        }
        long redisLength=redisUtil.lengthList(key);
        _logger.info("成功写入redis的代理:"+redisLength);
        //List read=redisUtil.rangeList(key,0,redisLength-1);
        //_logger.info("从redis读出的代理数:"+read.size());
        //for (int i = 0; i < read.size(); i++) {
         //   _logger.info("读取"+i+" "+JSON.parse((String)read.get(i)));
        //}
       // redisUtil.saveHashString("requests","get","hello",-1);
        //companyDao.save(new Company(null,new Date().toString(),String.valueOf(System.currentTimeMillis())));
    }

}
