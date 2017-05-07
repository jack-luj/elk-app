package com.github.jackl.elk.biz.task;



import com.github.jackl.elk.biz.ZhiHuHttpClient;
import com.github.jackl.elk.biz.entity.Page;
import com.github.jackl.elk.biz.entity.User;
import com.github.jackl.elk.biz.parser.ZhiHuUserListPageParser;
import com.github.jackl.elk.core.parser.ListPageParser;
import com.github.jackl.elk.core.util.Config;
import com.github.jackl.elk.core.util.Md5Util;
import com.github.jackl.elk.core.util.SimpleInvocationHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.jackl.elk.biz.ZhiHuHttpClient.parseUserCount;
import static com.github.jackl.elk.core.util.Constants.USER_FOLLOWEES_URL;


public class DetailListPageTask extends AbstractPageTask{
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(DetailListPageTask.class);
    private static ListPageParser proxyUserListPageParser;
    /**
     * Thread-数据库连接
     */
    private static Map<Thread, Connection> connectionMap = new ConcurrentHashMap<>();
    static {
        proxyUserListPageParser = getProxyUserListPageParser();
    }


    public DetailListPageTask(HttpRequestBase request, boolean proxyFlag) {
        super(request, proxyFlag);
    }

    /**
     * 代理类
     * @return
     */
    private static ListPageParser getProxyUserListPageParser(){
        ListPageParser userListPageParser = ZhiHuUserListPageParser.getInstance();
        InvocationHandler invocationHandler = new SimpleInvocationHandler(userListPageParser);
        ListPageParser proxyUserListPageParser = (ListPageParser) Proxy.newProxyInstance(userListPageParser.getClass().getClassLoader(),
                userListPageParser.getClass().getInterfaces(), invocationHandler);
        return proxyUserListPageParser;
    }

    @Override
    void retry() {
        zhiHuHttpClient.getDetailListPageThreadPool().execute(new DetailListPageTask(request, Config.isProxy));
    }

    @Override
    void handle(Page page) {
        List<User> list = proxyUserListPageParser.parseListPage(page);
        for(User u : list){
            logger.info("解析用户成功:" + u.toString());

            if(!Config.dbEnable || zhiHuHttpClient.getDetailListPageThreadPool().getActiveCount() == 1){
                parseUserCount.incrementAndGet();
                for (int j = 0; j < u.getFollowees() / 20; j++){
                    String nextUrl = String.format(USER_FOLLOWEES_URL, u.getUserToken(), j * 20);
                    HttpGet request = new HttpGet(nextUrl);
                    request.setHeader("authorization", "oauth " + ZhiHuHttpClient.getAuthorization());
                    zhiHuHttpClient.getDetailListPageThreadPool().execute(new DetailListPageTask(request, true));
                }
            }
        }
    }



    public static Map<Thread, Connection> getConnectionMap() {
        return connectionMap;
    }

}
