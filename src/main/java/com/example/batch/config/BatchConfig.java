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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.batch.ReportTaskLet;
import com.example.batch.listener.JobCompletionListener;
import com.example.batch.model.Product;
import com.example.batch.model.ProductCSV;
import com.example.batch.model.ProductRepository;
import com.example.batch.model.User;
import com.example.batch.model.UserCSV;
import com.example.batch.processor.ProductProcessor;
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
    private final ProductProcessor productProcessor;
    private final JobCompletionListener jobCompletionListener;
    private final ReportTaskLet reportTaskLetl;

    // Tasklet
    @Bean
    public Step generatedReportedStep() {
        return new StepBuilder("generatedReportedStep", jobRepository)
                .tasklet(reportTaskLetl, platformTransactionManager)
                .build();
    }

    // ProductsCSV
    @Bean
    public FlatFileItemReader<ProductCSV> productCsvReader() {
        return new FlatFileItemReaderBuilder<ProductCSV>()
                .name("productCsvReader")
                .resource(new ClassPathResource("productos.csv"))
                .encoding("UTF-8")
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names("nombre", "precio", "categoria", "stock")
                .fieldSetMapper(fieldSet -> {
                    ProductCSV p = new ProductCSV();
                    p.setName(fieldSet.readString("nombre"));
                    p.setPrice(fieldSet.readDouble("precio"));
                    p.setCategory(fieldSet.readString("categoria"));
                    p.setStock(fieldSet.readInt("stock"));
                    return p;
                })
                .build();
    }

    @Bean
    public RepositoryItemWriter<Product> productWriter(
            ProductRepository productRepository) {
        return new RepositoryItemWriterBuilder<Product>()
                .repository(productRepository)
                .methodName("save")
                .build();
    }

    @Bean
    @Qualifier("readProductsStep")
    public Step readProductsStep(RepositoryItemWriter<Product> productoWriter) {
        return new StepBuilder("leerProductosStep", jobRepository)
                .<ProductCSV, Product>chunk(3)
                .transactionManager(platformTransactionManager)
                .reader(productCsvReader())
                .processor(productProcessor)
                .writer(productoWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(5)
                .build();
    }

    // UsersCSV
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
    @Qualifier("readAndSaveUserStep")
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
    public Job importDataJob(@Qualifier("readAndSaveUserStep") Step readAndSaveUserStep,
            @Qualifier("readProductsStep") Step readProductsStep,
            @Qualifier("generatedReportedStep") Step generatedReportedStep) {
        return new JobBuilder("importDataJob", jobRepository)
                .listener(jobCompletionListener)
                .start(readAndSaveUserStep)
                .next(readProductsStep)
                .next(generatedReportedStep)
                .build();
    }
}
