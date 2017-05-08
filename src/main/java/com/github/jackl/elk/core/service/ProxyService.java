package com.github.jackl.elk.core.service;

import com.alibaba.fastjson.JSON;
import com.github.jackl.elk.core.util.RedisUtil;
import com.github.jackl.elk.proxy.entity.Proxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jackl on 2017/5/8.
 */
@Component
public class ProxyService {
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
}
