package com.github.jackl.elk.proxy;


import com.github.jackl.elk.biz.ZhiHuHttpClient;
import com.github.jackl.elk.biz.entity.Page;
import com.github.jackl.elk.core.httpclient.AbstractHttpClient;
import com.github.jackl.elk.core.service.ProxyService;
import com.github.jackl.elk.core.util.*;
import com.github.jackl.elk.proxy.entity.Proxy;
import com.github.jackl.elk.proxy.task.ProxyPageTask;
import com.github.jackl.elk.proxy.task.ProxySerializeTask;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ProxyHttpClient extends AbstractHttpClient {
    private static final Logger logger = Logger.getLogger(ProxyHttpClient.class);
    public static Set<Page> downloadFailureProxyPageSet = new HashSet<>(ProxyPool.proxyMap.size());


    /**
     * 代理测试线程池
     */
    private ThreadPoolExecutor proxyTestThreadExecutor;
    /**
     * 代理网站下载线程池
     */
    private ThreadPoolExecutor proxyDownloadThreadExecutor;
    public ProxyHttpClient(){
        initThreadPool();
        initProxy();
    }
    /**
     * 初始化线程池
     */
    private void initThreadPool(){
        proxyTestThreadExecutor = new SimpleThreadPoolExecutor(100, 100,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10000),
                new ThreadPoolExecutor.DiscardPolicy(),
                "proxyTestThreadExecutor");
        proxyDownloadThreadExecutor = new SimpleThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), "" +
                "proxyDownloadThreadExecutor");
        new Thread(new ThreadPoolMonitor(proxyTestThreadExecutor, "ProxyTestThreadPool")).start();
        new Thread(new ThreadPoolMonitor(proxyDownloadThreadExecutor, "ProxyDownloadThreadExecutor")).start();
    }

    /**
     * 初始化proxy
     *
     */
    private void initProxy(){
        Proxy[] proxyArray = null;
        try {
            proxyArray = (Proxy[]) HttpClientUtil.deserializeObject(Config.proxyPath);
            int usableProxyCount = 0;
            for (Proxy p : proxyArray){
                if (p == null){
                    continue;
                }
                p.setTimeInterval(Constants.TIME_INTERVAL);
                p.setFailureTimes(0);
                p.setSuccessfulTimes(0);
                long nowTime = System.currentTimeMillis();
                if (nowTime - p.getLastSuccessfulTime() < 1000 * 60 *60){
                    //上次成功离现在少于一小时
                    ProxyPool.proxyQueue.add(p);
                    ProxyPool.proxySet.add(p);
                    usableProxyCount++;
                }
            }
            logger.info("反序列化proxy成功，" + proxyArray.length + "个代理,可用代理" + usableProxyCount + "个");
        } catch (Exception e) {
            logger.warn("反序列化proxy失败");
        }
    }
    /**
     * 抓取代理
     */
    public void startCrawl(ProxyHttpClient proxyHttpClient, ZhiHuHttpClient zhiHuHttpClient, ProxyService proxyService){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    for (String url : ProxyPool.proxyMap.keySet()){
                        /**
                         * 首次本机直接下载代理页面
                         */
                        proxyDownloadThreadExecutor.execute(new ProxyPageTask(url, false,proxyHttpClient,zhiHuHttpClient,proxyService));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(1000 * 60 * 60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        new Thread(new ProxySerializeTask()).start();
    }
    public ThreadPoolExecutor getProxyTestThreadExecutor() {
        return proxyTestThreadExecutor;
    }

    public ThreadPoolExecutor getProxyDownloadThreadExecutor() {
        return proxyDownloadThreadExecutor;
    }
}