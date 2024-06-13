package org.c4marathon.assignment.settlement.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.SessionSynchronization;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class SettlementMongoConfig extends AbstractMongoClientConfiguration {

	@Value("${spring.mongo.database}")
	private String databaseName;

	@Value("${spring.mongo.username}")
	private String username;

	@Value("${spring.mongo.password}")
	private String password;

	@Value("${spring.mongo.host}")
	private String host;

	@Value("${spring.mongo.port}")
	private String port;

	@Bean
	public MongoTransactionManager mongoTransactionManager(
		@Qualifier("mongoDbFactory") MongoDatabaseFactory mongoDatabaseFactory) {
		return new MongoTransactionManager(mongoDatabaseFactory);
	}

	@Override
	protected String getDatabaseName() {
		return databaseName;
	}

	@Override
	public MongoDatabaseFactory mongoDbFactory() {
		return super.mongoDbFactory();
	}

	@Override
	public MongoClient mongoClient() {
		String uri = "mongodb://" + username + ":" + password + "@" + host + ":" + port
			+ "/" + databaseName + "?authSource=admin&socketTimeoutMS=1500";
		ConnectionString connectionString = new ConnectionString(uri);

		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
			.applyToConnectionPoolSettings(builder -> builder
				.maxConnectionIdleTime(10, TimeUnit.SECONDS))
			.applyConnectionString(connectionString)
			.build();

		return MongoClients.create(mongoClientSettings);
	}

	@Bean
	public MongoTemplate mongoTemplate() {
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient(), databaseName);
		mongoTemplate.setSessionSynchronization(SessionSynchronization.ALWAYS);
		mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION); // 삽입에 실패하면 예외가 발생하도록 한다.

		return mongoTemplate;
	}
}
