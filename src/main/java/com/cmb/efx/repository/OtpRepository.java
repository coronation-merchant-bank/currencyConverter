package com.cmb.efx.repository;

import com.cmb.efx.entity.Otp;
import org.springframework.data.history.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long>{
    List<Otp> findByIdentifier(String identifier);
}
