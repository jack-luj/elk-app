package com.github.jackl.elk;
import com.github.jackl.elk.http.RequestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.lang.management.ManagementFactory;


/**
 * Created by jackl on 17-4-30.
 */
@SpringBootApplication
public class Application implements CommandLineRunner{
    private Logger _logger=LoggerFactory.getLogger(getClass());
    @Autowired
    RequestClient requestClient;

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
      _logger.info(getProcess());
        String url="https://www.baidu.com/";
        _logger.info("开始请求Url:"+url);
        _logger.info( requestClient.doGet(url,null));
        _logger.info("结束请求Url:"+url);

    }

    public String getProcess(){
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return name;
    }


}
