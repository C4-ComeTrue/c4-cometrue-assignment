package org.c4marathon.assignment.container;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class ContainerBaseConfig {

	@Container
	private static final MySQLContainer<?> mySQLContainer =
		new MySQLContainer<>("mysql:8.0.33").waitingFor(Wait.forListeningPort());

	@Container
	private static final MongoDBContainer mongoContainer =
		new MongoDBContainer(
			DockerImageName.parse("mongo:4.0.10")).withExposedPorts(27017).waitingFor(Wait.forListeningPort());

	@Container
	private static final GenericContainer<?> redisContainer =
		new GenericContainer<>(DockerImageName.parse("redis:latest"))
			.withExposedPorts(6379).waitingFor(Wait.forListeningPort());

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		// MySQL 설정
		registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mySQLContainer::getUsername);
		registry.add("spring.datasource.password", mySQLContainer::getPassword);
		registry.add("spring.datasource.driver-class-name", mySQLContainer::getDriverClassName);
		registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQL8Dialect");

		// MongoDB 설정
		// registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
		// registry.add("spring.data.mongodb.database", () -> "test");
		System.setProperty("spring.mongo.database", "test");
		System.setProperty("spring.mongo.uri", mongoContainer.getReplicaSetUrl("test"));

		// Redis 설정
		registry.add("spring.data.redis.host", redisContainer::getHost);
		registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
	}
}
