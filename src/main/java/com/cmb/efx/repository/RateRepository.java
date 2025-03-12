package com.cmb.efx.repository;

import com.cmb.efx.entity.Rates;
import com.cmb.efx.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RateRepository extends JpaRepository<Rates, Long> {

    Page<Rates> findByApproveDateBetween(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
    List<Rates> findByStatus(Status status);
    @Modifying
    @Transactional
    @Query("UPDATE Rates r SET r.status = :newStatus WHERE r.status = :currentStatus")
    int updateStatusByStatus(@Param("currentStatus") Status currentStatus, @Param("newStatus") Status newStatus);
}
