package org.c4marathon.assignment.common.config;

import java.time.Clock;
import java.time.ZoneId;

import org.c4marathon.assignment.common.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

	@Bean
	public Clock clock() {
		return Clock.system(ZoneId.of(Constants.zoneId));
	}
}
