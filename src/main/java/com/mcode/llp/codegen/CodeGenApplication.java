package com.mcode.llp.codegen;

import com.mcode.llp.codegen.initializer.SuperUserInitializer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CodeGenApplication implements ApplicationRunner {


	private final SuperUserInitializer superUserInitializer;

	public CodeGenApplication(SuperUserInitializer superUserInitializer) {
		this.superUserInitializer = superUserInitializer;
	}

	public static void main(String[] args) {
		SpringApplication.run(CodeGenApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) {
		superUserInitializer.initializeOpenSearch();
	}

	// CORS Configuration
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				// Apply CORS globally to all endpoints
				registry.addMapping("/**")  // Allow all endpoints
						.allowedOrigins("http://localhost:4200")  // Allow the Angular app
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Allowed HTTP methods
						.allowedHeaders("*")  // Allow all headers
						.allowCredentials(true);  // Allow credentials (cookies, etc.)
			}
		};
	}
}
