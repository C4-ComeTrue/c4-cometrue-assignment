package org.c4marathon.assignment.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class JavaMailSenderConfig {
	@Value("${mail.username}")
	private String username;
	@Value("${mail.password}")
	private String password;
	@Value("${mail.debug}")
	private String debug;

	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		mailSender.setHost("smtp.naver.com");
		mailSender.setPort(465);
		mailSender.setUsername(username);
		mailSender.setPassword(password);
		mailSender.setJavaMailProperties(getMailProperties());

		return mailSender;
	}


	private Properties getMailProperties() {
		Properties properties = new Properties();
		properties.setProperty("mail.transport.protocol", "smtp");
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.starttls.enable", "true");
		properties.setProperty("mail.debug", debug);
		properties.setProperty("mail.smtp.ssl.trust","smtp.naver.com");
		properties.setProperty("mail.smtp.ssl.enable","true");
		return properties;
	}
}
