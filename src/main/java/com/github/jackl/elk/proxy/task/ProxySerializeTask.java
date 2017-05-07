package com.github.jackl.elk.proxy.task;


import com.github.jackl.elk.biz.ZhiHuHttpClient;
import com.github.jackl.elk.core.util.Config;
import com.github.jackl.elk.core.util.HttpClientUtil;
import com.github.jackl.elk.proxy.ProxyPool;
import com.github.jackl.elk.proxy.entity.Proxy;
import com.github.jackl.elk.proxy.util.ProxyUtil;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代理序列化
 */
public class ProxySerializeTask implements Runnable{
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ProxyPageTask.class);
    @Override
    public void run() {
        while (!ZhiHuHttpClient.isStop){
            try {
                Thread.sleep(1000 * 60 * 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Proxy[] proxyArray = null;
            ProxyPool.lock.readLock().lock();
            try {
                proxyArray = new Proxy[ProxyPool.proxySet.size()];
                int i = 0;
                for (Proxy p : ProxyPool.proxySet){
                    if (!ProxyUtil.isDiscardProxy(p)){
                        proxyArray[i++] = p;
                    }
                }
            } finally {
                ProxyPool.lock.readLock().unlock();
            }

            HttpClientUtil.serializeObject(proxyArray, Config.proxyPath);
            logger.info("成功序列化" + proxyArray.length + "个代理");
        }
    }
}
