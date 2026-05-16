package com.example.batch;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.example.batch.model.Product;
import com.example.batch.model.ProductRepository;
import com.example.batch.model.User;
import com.example.batch.model.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportTaskLet implements Tasklet {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private static final String REPORT_RUTE = "batch-report.txt";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        List<User> users = userRepository.findAll();
        List<Product> products = productRepository.findAll();

        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        try (PrintWriter writer = new PrintWriter(new FileWriter(REPORT_RUTE))) {
            writer.print("++++++++++++++++++++++");
            writer.print(" Report spring batch");
            writer.print(" Date: " + date);
            writer.print("++++++++++++++++++++++");
            writer.println();

            writer.println("--- Procesed users: " + users.size() + " ---");
            users.forEach(u -> writer.printf("  %-30s | %-35s | %s%n",
                    u.getFullName(), u.getEmail(), u.getAgeCategory()));

            writer.println();

            // Seccion de productos
            writer.println("--- Proceced products: " + products.size() + " ---");
            products.forEach(p -> writer.printf("  %-35s | $%10.2f | %s%n",
                    p.getName(), p.getPrice(),
                    p.isAvailable() ? "DISPONIBLE" : "SIN STOCK"));

            writer.println();
            writer.println("========================================");
            writer.println("   FIN DEL REPORTE");
            writer.println("========================================");

            log.info("Report generated in: {}", REPORT_RUTE);

        }
        return RepeatStatus.FINISHED;
    }

}
