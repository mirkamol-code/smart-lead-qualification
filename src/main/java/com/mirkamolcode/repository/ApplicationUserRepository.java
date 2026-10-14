package com.mirkamolcode.repository;

import com.mirkamolcode.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, UUID> {
    @Query("SELECT u FROM ApplicationUser u LEFT JOIN FETCH u.appUserRoles WHERE u.username = :username")
    Optional<ApplicationUser> findApplicationUserByUsername(@Param("username") String username);

}
