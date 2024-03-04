package org.c4marathon.assignment.bankaccount.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class AccountCacheConfig {

	@Bean
	public CacheManager savingProductCacheManager() {
		return new ConcurrentMapCacheManager("savingProduct");
	}
}
