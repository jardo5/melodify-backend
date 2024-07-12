package com.melodify.Melodify;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MelodifyApplication {
	Dotenv dotenv = Dotenv.configure().load();

	public static void main(String[] args) {
		SpringApplication.run(MelodifyApplication.class, args);
	}

}
