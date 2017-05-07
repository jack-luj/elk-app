package com.github.jackl.elk.core.service;

import com.github.jackl.elk.core.dao.CompanyDao;
import com.github.jackl.elk.core.entity.Company;
import com.github.jackl.elk.core.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by jackl on 2017/5/7.
 */
@Component
public class CompanyService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    CompanyDao companyDao;

    public void test(){
        redisUtil.saveHashString("requests","get","hello",-1);
        companyDao.save(new Company(null,new Date().toString(),String.valueOf(System.currentTimeMillis())));
    }

}
