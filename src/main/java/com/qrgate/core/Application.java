package com.qrgate.core;

import com.qrgate.core.model.User;
import com.qrgate.core.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner seedData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!userRepository.existsByUsername("admin")) {
				User admin = new User("admin", passwordEncoder.encode("admin123"), "admin@qrgate.com", "System Admin");
				admin.setRole("ROLE_ADMIN");
				userRepository.save(admin);
			}
			if (!userRepository.existsByUsername("user")) {
				User user = new User("user", passwordEncoder.encode("user123"), "user@qrgate.com", "Test User");
				userRepository.save(user);
			}
		};
	}

}
