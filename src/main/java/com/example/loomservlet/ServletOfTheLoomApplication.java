package com.example.loomservlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ServletOfTheLoomApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServletOfTheLoomApplication.class, args);
	}

	@RestController
	static class MyController {

		final JdbcTemplate jdbcTemplate;

		MyController(JdbcTemplate jdbcTemplate) {
			this.jdbcTemplate = jdbcTemplate;
		}

		@GetMapping("/")
		String getValue() throws InterruptedException {

			// Simulate a blocking call for one second. The thread should be put aside for about a second.
			Thread.sleep(1000);
			return "OK";
		}

		@GetMapping("/sql")
		String getFromDatabase() {

			// Simulate blocking I/O where the server side controls the timeout. The thread should be put aside for about a second.
			return jdbcTemplate.queryForList("select pg_sleep(1);").toString();
		}
	}

}
