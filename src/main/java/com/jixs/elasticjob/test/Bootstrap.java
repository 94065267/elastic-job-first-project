package com.jixs.elasticjob.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 启动类
 *
 * @author jixs_bj
 * @date 2019/4/26
 */
@SpringBootApplication
public class Bootstrap {
    public static void main(String args[]) throws InterruptedException {
        new SpringApplicationBuilder(Bootstrap.class).web(false).run(args);
        Thread.currentThread().join();
    }
}
