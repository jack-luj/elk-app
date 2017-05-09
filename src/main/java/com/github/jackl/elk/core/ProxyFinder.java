package com.github.jackl.elk.core;

import com.github.jackl.elk.biz.ZhiHuHttpClient;
import com.github.jackl.elk.core.service.ProxyService;
import com.github.jackl.elk.core.util.RedisUtil;
import com.github.jackl.elk.proxy.ProxyHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jackl on 17-4-30.
 */
@Component
public class ProxyFinder {
    private Logger _logger=LoggerFactory.getLogger(getClass());
    @Autowired
    private ProxyHttpClient proxyHttpClient;
    @Autowired
    private ZhiHuHttpClient zhiHuHttpClient;
    @Autowired
    private ProxyService proxyService;
    @Autowired
    RedisUtil redisUtil;
    public void find(){
        proxyHttpClient.init();
        proxyHttpClient.startCrawl(proxyHttpClient,zhiHuHttpClient,proxyService);
    }
}
