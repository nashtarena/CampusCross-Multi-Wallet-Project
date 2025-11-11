package com.campuscross.fx_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class FxServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FxServiceApplication.class, args);
	}

	@Autowired
	private RedisConnectionFactory connectionFactory;

	@PostConstruct
	public void checkRedisConnection() {
		try (RedisConnection connection = connectionFactory.getConnection()) {
			String result = connection.ping();
			System.out.println("\n\n*** REDIS CONNECTION SUCCESS: " + result + " ***\n\n");
		} catch (Exception e) {
			// Look for the specific message here!
			System.err.println("\n\n!!! REDIS CONNECTION FAILED: " + e.getMessage() + " !!!\n\n");
		}
	}

}
