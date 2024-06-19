package org.c4marathon.assignment.common.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {"org.c4marathon.assignment.*"},
	entityManagerFactoryRef = "mysqlEntityManagerFactory",
	transactionManagerRef = "mysqlTransactionManager")
@EnableTransactionManagement
public class JpaConfig {
	// TODO config설정 전부 해줘서 mongodb txManager랑 분리하여 사용하도록 수정 필요
	@Value("${spring.jpa.hibernate.ddl-auto}")
	private String ddlAuto;

	@Value("${spring.datasource.driver-class-name}")
	private String driverClassName;

	@Value("${spring.datasource.url}")
	private String url;

	@Value("${spring.datasource.username}")
	private String username;

	@Value("${spring.datasource.password}")
	private String password;

	@Primary
	@Bean
	public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setDataSource(mysqlDatasource()); //Datasource 설정
		entityManagerFactoryBean.setPackagesToScan("org.c4marathon.assignment.*.entity"); // MySQL Entity 패키지 경로

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(true);
		entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter); // hibernate 등록

		Map<String, Object> properties = new HashMap<>();
		properties.put("hibernate.hbm2ddl.auto", ddlAuto);
		properties.put("hibernate.show_sql", "true");
		properties.put("hibernate.format_sql", "true");
		entityManagerFactoryBean.setJpaPropertyMap(properties);

		return entityManagerFactoryBean;
	}

	@Primary
	@Bean
	public DataSource mysqlDatasource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);

		return dataSource;
	}

	@Primary
	@Bean
	public JpaTransactionManager mysqlTransactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(mysqlEntityManagerFactory().getObject());

		return transactionManager;
	}
}
