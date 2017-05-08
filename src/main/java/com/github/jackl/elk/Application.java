package com.github.jackl.elk;
import com.github.jackl.elk.core.ProxyFinder;
import com.github.jackl.elk.core.service.CompanyService;
import com.github.jackl.elk.core.util.RedisUtil;
import com.github.jackl.elk.proxy.ProxyHttpClient;
import com.github.jackl.elk.proxy.entity.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by jackl on 17-4-30.
 */
@SpringBootApplication
public class Application implements CommandLineRunner{
    private Logger _logger=LoggerFactory.getLogger(getClass());
    @Autowired
    CompanyService companyService;
    @Autowired
    ProxyFinder proxyFinder;

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
        _logger.info(getProcess());
        //companyService.test();
        proxyFinder.find();
        //ProxyHttpClient.getInstance().startCrawl();

    }



    public String getProcess(){
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return name;
    }


}
