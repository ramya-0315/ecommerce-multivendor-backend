package com.ramyastore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ramyastore.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

}
