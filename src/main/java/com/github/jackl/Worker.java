package com.github.jackl;

import org.slf4j.LoggerFactory;
import org.apache.log4j.Logger;

/**
 * Created by jackl on 17-4-30.
 */
public class Worker implements Runnable{
    private Logger _logger=Logger.getLogger(Application.class);
    @Override
    public void run() {

        while (true){
            _logger.info("现在时间是："+System.currentTimeMillis());
            try{
            Thread.sleep(3456l);
            }catch (InterruptedException e){
                _logger.error(e.getLocalizedMessage());
            }

        }

    }
}
