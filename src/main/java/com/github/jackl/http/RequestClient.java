package com.github.jackl.http;

import com.alibaba.fastjson.JSON;
import com.github.jackl.util.HttpClientUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jackl on 17-5-2.
 */
public class RequestClient {
    public String  doRequest(String url) throws IOException{
        Map params=new HashMap<String,String>();
        params.put("name","jackl");
        params.put("time",new Date().toLocaleString());

        String postUrl="http://localhost:8099/api/post";
        return  doPost(postUrl,params);

    }


    public String  doPost(String url,Map params) throws IOException{
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.addHeader("Content-Type","application/json");
        post.addHeader(new BasicHeader("Cookie",""));
        post.setEntity(new StringEntity(JSON.toJSONString(params), Charset.forName("UTF-8")));
        HttpClientContext httpClientContext = HttpClientContext.create();

        HttpResponse response = client.execute(post,httpClientContext);
        System.out.println("cookies:"+httpClientContext.getCookieStore().getCookies());
        CookieStore cookieStore=httpClientContext.getCookieStore();
        BasicClientCookie cookie = new BasicClientCookie("token","123456798");
        cookie.setVersion(0);
        cookie.setDomain("localhost");
        cookie.setPath("/api");
        cookieStore.addCookie(cookie);
        httpClientContext.setCookieStore(cookieStore);
        response = client.execute(post,httpClientContext);


        String content = EntityUtils.toString(response.getEntity(),"UTF-8");
        client.close();
        return content;
    }


}
