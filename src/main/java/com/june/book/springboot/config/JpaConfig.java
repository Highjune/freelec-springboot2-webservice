package com.june.book.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing //JPA Auditing 활성화 (Application클래스에 있는 @SpringBootApplication 과 분리 시키려고 만듬)
public class JpaConfig {
}
