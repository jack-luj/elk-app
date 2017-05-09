package com.github.jackl.elk.core.service;

import com.alibaba.fastjson.JSON;
import com.github.jackl.elk.core.util.RedisUtil;
import com.github.jackl.elk.proxy.entity.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackl on 2017/5/8.
 */
@Component
public class ProxyService {
    private Logger _logger= LoggerFactory.getLogger(getClass());
    String findProxiesKey="_find_proxies";
    String availableProxiesKey="_available_proxies";
    @Autowired
    RedisUtil redisUtil;

    /**
     * 保存所有搜集到的代理
     * @param p
     */
    public void addProxy(Proxy p){
        redisUtil.inList(findProxiesKey, JSON.toJSONString(p));
    }

    /**
     * 保存所有搜集到的代理
     * @param p
     */
    public void addAvailableProxy(Proxy p){
        redisUtil.inList(availableProxiesKey, JSON.toJSONString(p));
    }

    public void cleanAvailableProxy(){
        redisUtil.delete(availableProxiesKey);
    }
    /**
     * 可用数目
     * @return
     */
    public long lenAvailableProxy(){
        return redisUtil.lengthList(availableProxiesKey);
    }

    public List<Proxy> loadAvailableProxy(){
        List<Proxy> proxies=new ArrayList<>();
        long total=lenAvailableProxy();
        List saved=redisUtil.rangeList(availableProxiesKey,0,total-1);
        for (int i = 0; i < saved.size(); i++) {
            String json=(String)saved.get(i);
            _logger.info("i:"+json);
            Proxy p=JSON.parseObject(json,Proxy.class);
            proxies.add(p);
        }
        return proxies;
    }

}
