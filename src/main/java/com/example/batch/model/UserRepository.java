package com.example.batch.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByActive(boolean active);

    List<User> findByAgeCategory(String ageCategory);

    long countByAgeCategory(String ageCategory);
}
