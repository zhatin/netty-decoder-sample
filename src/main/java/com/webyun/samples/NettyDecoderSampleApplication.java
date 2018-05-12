package com.webyun.samples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class NettyDecoderSampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(NettyDecoderSampleApplication.class, args);
	}
}
