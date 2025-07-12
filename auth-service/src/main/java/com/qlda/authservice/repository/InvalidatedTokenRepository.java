package com.qlda.authservice.repository;

import com.qlda.authservice.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    // This interface will inherit methods from JpaRepository for CRUD operations
    // Additional custom query methods can be defined here if needed
}
