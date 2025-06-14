package com.mcode.llp.codegen;

import com.mcode.llp.codegen.initializer.Initializer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
public class CodeGenApplication implements ApplicationRunner{

	private final Initializer initializer;

	public CodeGenApplication(Initializer initializer) {
		this.initializer = initializer;
	}

	public static void main(String[] args) {
		SpringApplication.run(CodeGenApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) {
		initializer.superUserInitialize();
		initializer.permissionInitialize();
		initializer.notificationInitialize();
	}

}
