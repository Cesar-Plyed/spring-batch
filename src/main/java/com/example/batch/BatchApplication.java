package com.example.batch;

import java.util.List;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.batch.model.Product;
import com.example.batch.model.ProductRepository;
import com.example.batch.model.User;
import com.example.batch.model.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class BatchApplication implements CommandLineRunner {

	private final JobOperator jobOperator;
	private final Job importUserJob;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addLong("timestamp", System.currentTimeMillis())
				.toJobParameters();

		log.info("Running import Job");

		jobOperator.run(importUserJob, params);

		List<User> allUsers = userRepository.findAll();
		log.info("""
					Saved users in data base
					--------------------------------------------
				""");
		allUsers.forEach(
				u -> log.info("{} | {} | {} years old| {}",
						u.getFullName(),
						u.getEmail(),
						u.getAge(),
						u.getAgeCategory()));
		log.info("""
					--------------------------------------------
						Successfully processed Users (Wrong email users, it was rejected)
				""");

		// Products
		List<Product> allProducts = productRepository.findAll();
		log.info("""
					Saved this products in data base
					--------------------------------------------
				""");
		allProducts.forEach(
				u -> log.info("{} | {} | {} on stock | {}",
						u.getName(),
						u.getPrice(),
						u.getCategory(),
						u.getStock()));
		log.info("""
					--------------------------------------------
						Successfully processed Products (Wrong item price, it was rejected)
				""");
	}

}
