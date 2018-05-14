package com.webyun.samples;

import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class NettyDecoderSampleApplication {

	public static void main(String[] args) throws BeansException, InterruptedException {
		ConfigurableApplicationContext ctx = SpringApplication.run(NettyDecoderSampleApplication.class, args);
		ctx.getBean(ApplicationStartupRunner.class).run();
	}
}
