package org.c4marathon.assignment.container;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@Configuration
public class RedisTestContainers {

	private static final String REDIS_DOCKER_IMAGE = "redis:latest";
	private static final int REDIS_PORT = 6379;

	static {
		GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(
			DockerImageName.parse(REDIS_DOCKER_IMAGE)).withExposedPorts(REDIS_PORT).withReuse(true);

		REDIS_CONTAINER.start();

		System.setProperty("spring.data.redis.host", REDIS_CONTAINER.getHost());
		System.setProperty("spring.data.redis.port", REDIS_CONTAINER.getMappedPort(REDIS_PORT).toString());

	}
}
