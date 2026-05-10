package com.example.batch;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.batch.model.User;
import com.example.batch.model.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@SpringBootTest
class BatchApplicationTests {

	@Autowired
	private JobOperator jobOperator;
	
	@Autowired
    private Job importUsuariosJob;

    @Autowired
    private UserRepository userRepository;

	@Test
	void theJobMustBeCompletedSuccessfully() throws Exception {
		JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
		
		JobExecution execution = jobOperator.run(importUsuariosJob, params);

		assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
	}

	@Test
    void debeDescartarUsuariosConEmailInvalido() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis() + 1)
                .toJobParameters();

        jobOperator.run(importUsuariosJob, params);

        List<User> usuarios = userRepository.findAll();

        assertThat(usuarios).hasSizeLessThanOrEqualTo(10);

        usuarios.forEach(u -> {
            assertThat(u.getEmail()).contains("@");
            assertThat(u.isActive()).isTrue();
            assertThat(u.getAgeCategory()).isNotNull();
        });
    }

	@Test
    void debeAsignarCategoriaDeEdadCorrectamente() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis() + 2)
                .toJobParameters();

        jobOperator.run(importUsuariosJob, params);

        // Verificar que las categorías son correctas
        List<User> jovenes = userRepository.findByAgeCategory("JOVEN");
        jovenes.forEach(u -> {
            assertThat(u.getAge()).isGreaterThanOrEqualTo(18);
            assertThat(u.getAge()).isLessThan(30);
        });

        List<User> adultos = userRepository.findByAgeCategory("ADULTO");
        adultos.forEach(u -> {
            assertThat(u.getAge()).isGreaterThanOrEqualTo(30);
            assertThat(u.getAge()).isLessThan(50);
        });
    }

}
