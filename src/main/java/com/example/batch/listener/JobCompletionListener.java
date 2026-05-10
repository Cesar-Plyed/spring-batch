package com.example.batch.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

import com.example.batch.model.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCompletionListener implements JobExecutionListener {
    private final UserRepository userRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("""
                ==========================================
                     INICIANDO JOB: Importar Usuarios
                ==========================================
                 """);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            long total = userRepository.count();
            long youngAdult = userRepository.countByAgeCategory("YOUNG ADULT");
            long adult = userRepository.countByAgeCategory("ADULT");
            long elderly = userRepository.countByAgeCategory("ELDERLY");

            log.info("""
                    \n
                    ==========================================
                         INICIANDO JOB: Importar Usuarios
                    ------------------------------------------
                         Total saved Users: {}
                         Young Adult (18 - 29): {}
                         Adult (30 - 49): {}
                         Elderly (50 - 64): {}
                    ------------------------------------------
                         Duration: {} ms
                    ==========================================
                     """, total, youngAdult, adult, elderly,
                    jobExecution.getEndTime() != null && jobExecution.getStartTime() != null
                            ? java.time.Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime())
                                    .toMillis()
                            : "N/A");
        } else {
            log.error("Filed Job with {} status", jobExecution.getStatus());
            jobExecution.getAllFailureExceptions().forEach(
                    ex -> log.error("Error {}", ex.getMessage()));
        }
    }
}
