package com.example.batch.config;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.batch.listener.JobCompletionListener;
import com.example.batch.model.User;
import com.example.batch.model.UserCSV;
import com.example.batch.processor.UserProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final UserProcessor userProcessor;
    private final JobCompletionListener jobCompletionListener;

    @Bean
    public FlatFileItemReader<UserCSV> csvReader() {
        return new FlatFileItemReaderBuilder<UserCSV>()
                .name("userCsvReader")
                .resource(new ClassPathResource("usuarios.csv"))
                .encoding("UTF-8")
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names("nombre", "apellido", "email", "edad")
                .fieldSetMapper(fieldSet -> {
                    UserCSV user = new UserCSV();
                    user.setName(fieldSet.readString("nombre"));
                    user.setLastname(fieldSet.readString("apellido"));
                    user.setEmail(fieldSet.readString("email"));
                    user.setAge(fieldSet.readInt("edad"));
                    return user;
                })
                .build();
    }

    @Bean
    public RepositoryItemWriter<User> dbWriter(
            com.example.batch.model.UserRepository userRepository) {
        return new RepositoryItemWriterBuilder<User>()
                .repository(userRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public Step readAndSaveUserStep(
            RepositoryItemWriter<User> dbWriter) {
        return new StepBuilder("readAndSaveUserStep", jobRepository)
                .<UserCSV, User>chunk(3)
                .transactionManager(platformTransactionManager)
                .reader(csvReader())
                .processor(userProcessor)
                .writer(dbWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(5)
                .build();
    }

    @Bean
    public Job importUsersJob(Step readAndSaveUserStep) {
        return new JobBuilder("importUsersJob", jobRepository)
                .listener(jobCompletionListener)
                .start(readAndSaveUserStep)
                .build();
    }
}
