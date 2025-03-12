package com.cmb.efx.repository;

import com.cmb.efx.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

public interface TransactionRepository extends JpaRepository<Transactions, Long> {


}
