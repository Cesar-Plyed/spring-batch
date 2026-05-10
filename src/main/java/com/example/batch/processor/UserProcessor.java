package com.example.batch.processor;

import java.time.LocalDateTime;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.example.batch.model.User;
import com.example.batch.model.UserCSV;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserProcessor implements ItemProcessor<UserCSV, User> {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Override
    public User process(UserCSV csvItem) throws Exception {
        log.debug("""
                    Processing User:
                        name: {},
                        lastname: {}
                """, csvItem.getName(), csvItem.getLastname());

        if (!hasValidEmail(csvItem.getEmail())) {
            log.warn("""
                    Invalid Email, ruling this user out :
                        name: {},
                        age: {}
                    """, csvItem.getName(), csvItem.getLastname());
            return null;
        }

        if (csvItem.getAge() == null || csvItem.getAge() < 0 || csvItem.getAge() > 120) {
            log.warn("""
                        Invalid Age, ruling this user out :
                        name: {},
                        age: {}
                    """,
                    csvItem.getName(), csvItem.getAge());
            return null;
        }

        User user = new User();

        user.setName(capitalize(csvItem.getName()));
        user.setLastname(capitalize(csvItem.getLastname()));
        user.setEmail(csvItem.getEmail().toLowerCase().trim());
        user.setAge(csvItem.getAge());

        user.setFullName(
                capitalize(csvItem.getName() + " " + capitalize(csvItem.getLastname())));

        user.setAgeCategory(determineCategory(csvItem.getAge()));

        user.setDateProcessor(LocalDateTime.now());

        user.setActive(true);

        log.info("""
                User Processed:
                    name: {},
                    age: {}
                """, csvItem.getName(), csvItem.getAge());

        return user;
    }

    private boolean hasValidEmail(String email) {
        if (email == null || email.isBlank())
            return false;
        return email.trim().matches(EMAIL_REGEX);
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank())
            return text;
        return text.trim().substring(0, 1).toUpperCase() +
                text.trim().substring(1).toLowerCase();
    }

    private String determineCategory(int age) {

        if (age < 18)
            return "MINOR";
        else if (age < 30)
            return "YOUNG ADULT";
        else if (age < 50)
            return "ADULT";
        else if (age < 65)
            return "ELDERLY";
        else
            return "SENIOR";

    }
}
