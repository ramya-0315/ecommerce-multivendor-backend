package com.ramyastore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ramyastore.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {



}
