package com.totoro.repository;

import com.totoro.domain.EmailQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long> {
}
