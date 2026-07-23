package com.example.secondhandbackend.repository;

import com.example.secondhandbackend.entity.User;
import com.example.secondhandbackend.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    List<User> findByRole(Role role);
}