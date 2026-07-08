package com.drawpin.repository;

import com.drawpin.domain.entity.Creator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreatorRepository extends JpaRepository<Creator, UUID> {
    
    Optional<Creator> findByUserId(UUID userId);

    @Query("SELECT c FROM Creator c JOIN FETCH c.user u WHERE u.handle = :handle")
    Optional<Creator> findByUserHandle(@Param("handle") String handle);

    boolean existsByUserId(UUID userId);
}
