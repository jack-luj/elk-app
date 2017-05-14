package com.github.jackl.elk.proxy.task;


import com.github.jackl.elk.biz.ZhiHuHttpClient;
import com.github.jackl.elk.biz.entity.Page;
import com.github.jackl.elk.core.service.ProxyService;
import com.github.jackl.elk.core.util.Constants;
import com.github.jackl.elk.proxy.ProxyPool;
import com.github.jackl.elk.proxy.entity.Proxy;
import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 代理检测task
 * 通过访问知乎首页，能否正确响应
 * 将可用代理添加到DelayQueue延时队列中
 */
public class ProxyTestTask implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(ProxyTestTask.class);
    private Proxy proxy;
    private ZhiHuHttpClient zhiHuHttpClient;
    private ProxyService proxyService;
    public ProxyTestTask(Proxy proxy,ZhiHuHttpClient zhiHuHttpClient,ProxyService proxyService){
        this.proxy = proxy;
        this.zhiHuHttpClient = zhiHuHttpClient;
        this.proxyService=proxyService;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        HttpGet request = new HttpGet(Constants.INDEX_URL);
        try {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(Constants.TIMEOUT).
                    setConnectTimeout(Constants.TIMEOUT).
                    setConnectionRequestTimeout(Constants.TIMEOUT).
                    setProxy(new HttpHost(proxy.getIp(), proxy.getPort())).
                    setCookieSpec(CookieSpecs.STANDARD).
                    build();
            request.setConfig(requestConfig);
            Page page = zhiHuHttpClient.getWebPage(request);
            long endTime = System.currentTimeMillis();
            String logStr = Thread.currentThread().getName() + " " + proxy.getProxyStr() +
                    "  executing request " + page.getUrl()  + " response statusCode:" + page.getStatusCode() +
                    "  request cost time:" + (endTime - startTime) + "ms";
            if (page == null || page.getStatusCode() != 200){
                logger.warn(logStr);
                return;
            }
            request.releaseConnection();

            logger.debug(proxy.toString() + "---------" + page.toString());
            if(!ProxyPool.proxySet.contains(proxy)){
                proxy.setSuccessfulTotalTime(endTime - startTime);//请求成功总耗时
                proxy.setLastSuccessfulTime(System.currentTimeMillis());
                logger.debug(proxy.toString() + "----------代理可用--------请求耗时:" + (endTime - startTime) + "ms");
                ProxyPool.lock.writeLock().lock();
                try {
                    if(proxy.getSuccessfulTotalTime()<=10000) {//响应在10s以上的丢弃
                        ProxyPool.proxySet.add(proxy);
                    }
                } finally {
                    ProxyPool.lock.writeLock().unlock();
                }
                ProxyPool.proxyQueue.add(proxy);
            }
        } catch (IOException e) {
            logger.debug("IOException:", e);
        } finally {
            if (request != null){
                request.releaseConnection();
            }
        }
    }
    private String getProxyStr(){
        return proxy.getIp() + ":" + proxy.getPort();
    }
}
