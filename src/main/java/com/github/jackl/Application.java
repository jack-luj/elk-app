package com.github.jackl;
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
        Thread mqttWorker = new Thread(new Worker());
        mqttWorker.setName("log-worker");
        mqttWorker.start();

    }

    public String getProcess(){
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return name;
    }


}
