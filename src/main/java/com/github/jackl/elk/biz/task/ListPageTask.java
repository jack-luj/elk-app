package com.github.jackl.elk.biz.task;



import com.github.jackl.elk.biz.entity.Page;
import com.github.jackl.elk.core.util.Config;
import com.github.jackl.elk.core.util.Constants;
import com.jayway.jsonpath.JsonPath;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.List;

/**
 * 知乎用户关注列表页task
 * 下载成功解析出用户token，去重,构造用户详情url，获，添加到DetailPageDownloadThreadPool
 */
public class ListPageTask extends AbstractPageTask {

    public ListPageTask(HttpRequestBase request, boolean proxyFlag) {
        super(request, proxyFlag);
    }


    @Override
    void retry() {
        zhiHuHttpClient.getListPageThreadPool().execute(new ListPageTask(request, Config.isProxy));
    }

    @Override
    void handle(Page page) {
        /**
         * "我关注的人"列表页
         */
        List<String> urlTokenList = JsonPath.parse(page.getHtml()).read("$.data..url_token");
        for (String s : urlTokenList){
            if (s == null){
                continue;
            }
            handleUserToken(s);
        }
    }
    private void handleUserToken(String userToken){
        String url = Constants.INDEX_URL + "/people/" + userToken + "/following";
        if(!Config.dbEnable){
            zhiHuHttpClient.getDetailPageThreadPool().execute(new DetailPageTask(url, Config.isProxy));
            return ;
        }
//        boolean existUserFlag = ZhiHuDAO.isExistUser(userToken);
        boolean existUserFlag =false;
        while (zhiHuHttpClient.getDetailPageThreadPool().getQueue().size() > 1000){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!existUserFlag || zhiHuHttpClient.getDetailPageThreadPool().getActiveCount() == 0){
            /**
             * 防止互相等待，导致死锁
             */
            zhiHuHttpClient.getDetailPageThreadPool().execute(new DetailPageTask(url, Config.isProxy));

        }
    }
}
