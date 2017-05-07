package com.github.jackl.elk.biz.task;


import com.github.jackl.elk.biz.ZhiHuHttpClient;
import com.github.jackl.elk.biz.entity.Page;
import com.github.jackl.elk.core.util.Constants;
import com.github.jackl.elk.core.util.HttpClientUtil;
import com.github.jackl.elk.proxy.ProxyPool;
import com.github.jackl.elk.proxy.entity.Direct;
import com.github.jackl.elk.proxy.entity.Proxy;
import com.github.jackl.elk.proxy.util.ProxyUtil;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * page task
 * 下载网页并解析，具体解析由子类实现
 * 若使用代理，从ProxyPool中取
 * @see ProxyPool
 */
public abstract class AbstractPageTask implements Runnable{
	private static Logger logger = LoggerFactory.getLogger(AbstractPageTask.class);
	protected String url;
	protected HttpRequestBase request;
	protected boolean proxyFlag;//是否通过代理下载
	private Proxy currentProxy;//当前线程使用的代理
	protected static ZhiHuHttpClient zhiHuHttpClient = ZhiHuHttpClient.getInstance();

	public AbstractPageTask(){

	}
	public AbstractPageTask(String url, boolean proxyFlag){
		this.url = url;
		this.proxyFlag = proxyFlag;
	}
	public AbstractPageTask(HttpRequestBase request, boolean proxyFlag){
		this.request = request;
		this.proxyFlag = proxyFlag;
	}
	public void run(){
		long requestStartTime = 0l;
		HttpGet tempRequest = null;
		try {
			Page page = null;
			if(url != null){
				if (proxyFlag){
					tempRequest = new HttpGet(url);
					currentProxy = ProxyPool.proxyQueue.take();
					if(!(currentProxy instanceof Direct)){
						HttpHost proxy = new HttpHost(currentProxy.getIp(), currentProxy.getPort());
						tempRequest.setConfig(HttpClientUtil.getRequestConfigBuilder().setProxy(proxy).build());
					}
					requestStartTime = System.currentTimeMillis();
					page = zhiHuHttpClient.getWebPage(tempRequest);
				}else {
					requestStartTime = System.currentTimeMillis();
					page = zhiHuHttpClient.getWebPage(url);
				}
			} else if(request != null){
				if (proxyFlag){
					currentProxy = ProxyPool.proxyQueue.take();
					if(!(currentProxy instanceof Direct)) {
						HttpHost proxy = new HttpHost(currentProxy.getIp(), currentProxy.getPort());
						request.setConfig(HttpClientUtil.getRequestConfigBuilder().setProxy(proxy).build());
					}
					requestStartTime = System.currentTimeMillis();
					page = zhiHuHttpClient.getWebPage(request);
				}else {
					requestStartTime = System.currentTimeMillis();
					page = zhiHuHttpClient.getWebPage(request);
				}
			}
			long requestEndTime = System.currentTimeMillis();
			page.setProxy(currentProxy);
			int status = page.getStatusCode();
			String logStr = Thread.currentThread().getName() + " " + currentProxy +
					"  executing request " + page.getUrl()  + " response statusCode:" + status +
					"  request cost time:" + (requestEndTime - requestStartTime) + "ms";
			if(status == HttpStatus.SC_OK){
				if (page.getHtml().contains("zhihu")){
					logger.debug(logStr);
					currentProxy.setSuccessfulTimes(currentProxy.getSuccessfulTimes() + 1);
					currentProxy.setSuccessfulTotalTime(currentProxy.getSuccessfulTotalTime() + (requestEndTime - requestStartTime));
					double aTime = (currentProxy.getSuccessfulTotalTime() + 0.0) / currentProxy.getSuccessfulTimes();
					currentProxy.setSuccessfulAverageTime(aTime);
					currentProxy.setLastSuccessfulTime(System.currentTimeMillis());
					handle(page);
				}else {
					/**
					 * 代理异常，没有正确返回目标url
					 */
					logger.warn("proxy exception:" + currentProxy.toString());
				}

			}
			/**
			 * 401--不能通过验证
			 */
			else if(status == 404 || status == 401 ||
					status == 410){
				logger.warn(logStr);
			}
			else {
				logger.error(logStr);
				Thread.sleep(100);
				retry();
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException", e);
		} catch (IOException e) {
            if(currentProxy != null){
                /**
                 * 该代理可用，将该代理继续添加到proxyQueue
                 */
                currentProxy.setFailureTimes(currentProxy.getFailureTimes() + 1);
            }
            if(!zhiHuHttpClient.getDetailListPageThreadPool().isShutdown()){
				retry();
			}
		} finally {
			if (request != null){
				request.releaseConnection();
			}
			if (tempRequest != null){
				tempRequest.releaseConnection();
			}
			if (currentProxy != null && !ProxyUtil.isDiscardProxy(currentProxy)){
				currentProxy.setTimeInterval(Constants.TIME_INTERVAL);
				ProxyPool.proxyQueue.add(currentProxy);
			}
		}
	}

	/**
	 * retry
	 */
	abstract void retry();



	/**
	 * 子类实现page的处理
	 * @param page
	 */
	abstract void handle(Page page);

	private String getProxyStr(Proxy proxy){
		if (proxy == null){
			return "";
		}
		return proxy.getIp() + ":" + proxy.getPort();
	}

}
