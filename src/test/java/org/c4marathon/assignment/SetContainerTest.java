package org.c4marathon.assignment;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import com.redis.testcontainers.RedisContainer;

public class SetContainerTest {

	private static final String REDIS_IMAGE = "redis:latest";
	private static final int REDIS_PORT = 6379;

	@Container
	private static RedisContainer redis = new RedisContainer(DockerImageName.parse(REDIS_IMAGE)).withExposedPorts(
		REDIS_PORT);

	@DynamicPropertySource
	private static void redisProperties(DynamicPropertyRegistry registry) {
		redis.start();
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", redis::getFirstMappedPort);
	}

}
