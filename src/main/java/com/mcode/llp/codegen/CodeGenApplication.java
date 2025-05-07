package com.mcode.llp.codegen;

import com.mcode.llp.codegen.initializer.Initializer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
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
