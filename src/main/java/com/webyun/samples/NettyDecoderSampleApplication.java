package com.webyun.samples;

import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import com.webyun.samples.tcp.TCPServer;

@Configuration
@SpringBootApplication
public class NettyDecoderSampleApplication {

	public static void main(String[] args) throws BeansException, InterruptedException {
		ConfigurableApplicationContext ctx = SpringApplication.run(NettyDecoderSampleApplication.class, args);
		ctx.getBean(TCPServer.class).start();
	}
}
