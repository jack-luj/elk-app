package com.github.jackl.elk.biz;


import com.github.jackl.elk.biz.task.DetailListPageTask;
import com.github.jackl.elk.core.httpclient.AbstractHttpClient;
import com.github.jackl.elk.core.httpclient.IHttpClient;
import com.github.jackl.elk.core.util.*;
import com.github.jackl.elk.proxy.ProxyHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class ZhiHuHttpClient extends AbstractHttpClient implements IHttpClient {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ZhiHuHttpClient.class);
    private volatile static ZhiHuHttpClient instance;
    /**
     * 统计用户数量
     */
    public static AtomicInteger parseUserCount = new AtomicInteger(0);
    private static long startTime = System.currentTimeMillis();
    public static volatile boolean isStop = false;

    @Autowired
    private ProxyHttpClient proxyHttpClient;

    /**
     * 详情页下载线程池
     */
    private ThreadPoolExecutor detailPageThreadPool;
    /**
     * 列表页下载线程池
     */
    private ThreadPoolExecutor listPageThreadPool;
    /**
     * 详情列表页下载线程池
     */
    private ThreadPoolExecutor detailListPageThreadPool;
    /**
     * request　header
     * 获取列表页时，必须带上
     */
    private static String authorization;
    private ZhiHuHttpClient() {
        initHttpClient();
        intiThreadPool();
    }
    /**
     * 初始化HttpClient
     */
    @Override
    public void initHttpClient() {
        authorization = initAuthorization();

    }

    /**
     * 初始化线程池
     */
    private void intiThreadPool(){
//        detailPageThreadPool = new SimpleThreadPoolExecutor(Config.downloadThreadSize,
//                Config.downloadThreadSize,
//                0L, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<Runnable>(),
//                "detailPageThreadPool");
//        listPageThreadPool = new SimpleThreadPoolExecutor(50, 80,
//                0L, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<Runnable>(5000),
//                new ThreadPoolExecutor.DiscardPolicy(), "listPageThreadPool");
//        new Thread(new ThreadPoolMonitor(detailPageThreadPool, "DetailPageDownloadThreadPool")).start();
//        new Thread(new ThreadPoolMonitor(listPageThreadPool, "ListPageDownloadThreadPool")).start();
        detailListPageThreadPool = new SimpleThreadPoolExecutor(Config.downloadThreadSize,
                Config.downloadThreadSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(2000),
                new ThreadPoolExecutor.DiscardPolicy(),
                "detailListPageThreadPool");
        new Thread(new ThreadPoolMonitor(detailListPageThreadPool, "DetailListPageThreadPool")).start();

    }
    public void startCrawl(String url){
//        detailPageThreadPool.execute(new DetailPageTask(url, Config.isProxy));
        manageHttpClient();
    }

    @Override
    public void startCrawl() {
        String startToken = Config.startUserToken;
        String startUrl = String.format(Constants.USER_FOLLOWEES_URL, startToken, 0);
        HttpGet request = new HttpGet(startUrl);
        request.setHeader("authorization", "oauth " + ZhiHuHttpClient.getAuthorization());
        detailListPageThreadPool.execute(new DetailListPageTask(request, Config.isProxy));
        manageHttpClient();
    }

    /**
     * 初始化authorization
     * @return
     */
    private String initAuthorization(){
        String content = null;
        try {
            content = HttpClientUtil.getWebPage(Config.startURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Pattern pattern = Pattern.compile("https://static\\.zhihu\\.com/heifetz/main\\.app\\.([0-9]|[a-z])*\\.js");
        Matcher matcher = pattern.matcher(content);
        String jsSrc = null;
        if (matcher.find()){
            jsSrc = matcher.group(0);
        } else {
            throw new RuntimeException("not find javascript url");
        }
        String jsContent = null;
        try {
            jsContent = HttpClientUtil.getWebPage(jsSrc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pattern = Pattern.compile("CLIENT_ALIAS=\"(([0-9]|[a-z])*)\"");
        matcher = pattern.matcher(jsContent);
        if (matcher.find()){
            String authorization = matcher.group(1);
            return authorization;
        }
        throw new RuntimeException("not get authorization");
    }
    public static String getAuthorization(){
        return authorization;
    }
    /**
     * 管理知乎客户端
     * 关闭整个爬虫
     */
    public void manageHttpClient(){
        while (true) {
            /**
             * 下载网页数
             */
            long downloadPageCount = detailListPageThreadPool.getTaskCount();

            if (downloadPageCount >= Config.downloadPageCount &&
                    !detailListPageThreadPool.isShutdown()) {
                isStop = true;
                ThreadPoolMonitor.isStopMonitor = true;
                detailListPageThreadPool.shutdown();
            }
            if(detailListPageThreadPool.isTerminated()){
                //关闭数据库连接
                Map<Thread, Connection> map = DetailListPageTask.getConnectionMap();
                for(Connection cn : map.values()){
                    try {
                        if (cn != null && !cn.isClosed()){
                            cn.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                //关闭代理检测线程池
                proxyHttpClient.getProxyTestThreadExecutor().shutdownNow();
                //关闭代理下载页线程池
                proxyHttpClient.getProxyDownloadThreadExecutor().shutdownNow();

                break;
            }
            double costTime = (System.currentTimeMillis() - startTime) / 1000.0;//单位s
            logger.debug("抓取速率：" + parseUserCount.get() / costTime + "个/s");
//            logger.info("downloadFailureProxyPageSet size:" + ProxyHttpClient.downloadFailureProxyPageSet.size());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public ThreadPoolExecutor getDetailPageThreadPool() {
        return detailPageThreadPool;
    }

    public ThreadPoolExecutor getListPageThreadPool() {
        return listPageThreadPool;
    }
    public ThreadPoolExecutor getDetailListPageThreadPool() {
        return detailListPageThreadPool;
    }
}
