package com.cmb.efx.repository;

import com.cmb.efx.entity.GlobalVolumeLimit;
import com.cmb.efx.entity.Rates;
import com.cmb.efx.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VolumeRepository extends JpaRepository<GlobalVolumeLimit, Long>, JpaSpecificationExecutor<GlobalVolumeLimit> {

    Page<GlobalVolumeLimit> findByCreatedAtBetween(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
    @Query("SELECT v FROM GlobalVolumeLimit v WHERE v.authorizer IS NOT NULL AND v.endDate < :now AND v.status = 'APPROVED'")
    List<GlobalVolumeLimit> findApprovedVolumesBefore(@Param("now") LocalDateTime now);

    List<GlobalVolumeLimit> findByCurrencyTypeAndStatus(String currencyType, Status status);
}
