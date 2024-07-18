package com.melodify.Melodify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Logger;

@SpringBootApplication
public class MelodifyApplication {
	public static void main(String[] args) {
		//Log MongoDB connection
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		SpringApplication.run(MelodifyApplication.class, args);
	}

}
