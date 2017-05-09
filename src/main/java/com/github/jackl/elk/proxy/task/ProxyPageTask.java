package com.github.jackl.elk.proxy.task;


import com.github.jackl.elk.biz.ZhiHuHttpClient;
import com.github.jackl.elk.biz.entity.Page;
import com.github.jackl.elk.core.service.ProxyService;
import com.github.jackl.elk.core.util.Config;
import com.github.jackl.elk.core.util.Constants;
import com.github.jackl.elk.core.util.HttpClientUtil;
import com.github.jackl.elk.proxy.ProxyHttpClient;
import com.github.jackl.elk.proxy.ProxyListPageParser;
import com.github.jackl.elk.proxy.ProxyPool;
import com.github.jackl.elk.proxy.entity.Direct;
import com.github.jackl.elk.proxy.entity.Proxy;
import com.github.jackl.elk.proxy.site.ProxyListPageParserFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.github.jackl.elk.proxy.ProxyPool.proxyQueue;
/**
 * 下载代理网页并解析
 * 若下载失败，通过代理去下载代理网页
 */
public class ProxyPageTask implements Runnable{
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(ProxyPageTask.class);
	protected String url;
	private boolean proxyFlag;//是否通过代理下载
	private Proxy currentProxy;//当前线程使用的代理

	protected ProxyHttpClient proxyHttpClient;
	private ZhiHuHttpClient zhiHuHttpClient;
	private ProxyService proxyService;

	public ProxyPageTask(String url, boolean proxyFlag, ProxyHttpClient proxyHttpClient, ZhiHuHttpClient zhiHuHttpClient, ProxyService proxyService){
		this.url = url;
		this.proxyFlag = proxyFlag;
		this.proxyHttpClient=proxyHttpClient;
		this.zhiHuHttpClient=zhiHuHttpClient;
		this.proxyService=proxyService;

	}
	public void run(){
		long requestStartTime = System.currentTimeMillis();
		HttpGet tempRequest = null;
		try {
			Page page = null;
			if (proxyFlag){
				tempRequest = new HttpGet(url);
				currentProxy = proxyQueue.take();
				if(!(currentProxy instanceof Direct)){
					HttpHost proxy = new HttpHost(currentProxy.getIp(), currentProxy.getPort());
					tempRequest.setConfig(HttpClientUtil.getRequestConfigBuilder().setProxy(proxy).build());
				}
				page = proxyHttpClient.getWebPage(tempRequest);
			}else {
				page = proxyHttpClient.getWebPage(url);
			}
			page.setProxy(currentProxy);
			int status = page.getStatusCode();
			long requestEndTime = System.currentTimeMillis();
			String logStr = Thread.currentThread().getName() + " " + getProxyStr(currentProxy) +
					"  executing request " + page.getUrl()  + " response statusCode:" + status +
					"  request cost time:" + (requestEndTime - requestStartTime) + "ms";
			if(status == HttpStatus.SC_OK){
				logger.debug(logStr);
				handle(page);
			} else {
				logger.error(logStr);
				Thread.sleep(100);
				retry();
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException", e);
		} catch (IOException e) {
			retry();
		} finally {
			if(currentProxy != null){
				currentProxy.setTimeInterval(Constants.TIME_INTERVAL);
				proxyQueue.add(currentProxy);
			}
			if (tempRequest != null){
				tempRequest.releaseConnection();
			}
		}
	}

	/**
	 * retry
	 */
	public void retry(){
		proxyHttpClient.getProxyDownloadThreadExecutor().execute(new ProxyPageTask(url, Config.isProxy,proxyHttpClient,zhiHuHttpClient,proxyService));
	}

	public void handle(Page page){
		ProxyListPageParser parser = ProxyListPageParserFactory.
				getProxyListPageParser(ProxyPool.proxyMap.get(url));
		List<Proxy> proxyList = parser.parse(page.getHtml());
		for(Proxy p : proxyList){
			if(!zhiHuHttpClient.getDetailListPageThreadPool().isTerminated()){
				if (!ProxyPool.proxySet.contains(p.getProxyStr())){
					proxyHttpClient.getProxyTestThreadExecutor().execute(new ProxyTestTask(p,zhiHuHttpClient,proxyService));
				}
			}
		}
	}

	private String getProxyStr(Proxy proxy){
		if (proxy == null){
			return "";
		}
		return proxy.getIp() + ":" + proxy.getPort();
	}
}
