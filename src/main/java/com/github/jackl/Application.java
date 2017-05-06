package com.github.jackl;
import com.github.jackl.http.RequestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
      _logger.info(getProcess());
        RequestClient requestClient=new RequestClient();
        String url="http://localhost:8099/api/hello";
        _logger.info("开始请求Url:"+url);
        _logger.info( requestClient.doRequest(url));
        _logger.info("结束请求Url:"+url);

    }

    public String getProcess(){
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return name;
    }


}
