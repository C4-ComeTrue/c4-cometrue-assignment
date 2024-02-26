package org.c4marathon.assignment.bankaccount.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

@Configuration
@EnableCaching
public class AccountCacheConfig {

	@Bean
	public CacheManager savingProductCacheManager() {
		return new ConcurrentMapCacheManager("savingProduct");
	}

	/**
	 *
	 * DB에 저장된 일일 충전한도 값을 한 번 읽으면 Redis에 저장하여 관리합니다.
	 * DB에 별도의 테이블로 관리하면 디스크 공간과 인덱스를 위한 메모리 공간이 사용됩니다.
	 * 그렇게 하기 보다는 Redis에서 <PK, 충전한도>로 관리하여 인덱스 테이블과 비슷한 메모리만 사용하고자 했습니다.
	 */
	@Bean
	public RedisTemplate<Long, Long> redisTemplate(RedisConnectionFactory connectionFactory) {
		final RedisTemplate<Long, Long> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new GenericToStringSerializer<>(Long.class));
		template.setValueSerializer(new GenericToStringSerializer<>(Long.class));

		return template;
	}
}
