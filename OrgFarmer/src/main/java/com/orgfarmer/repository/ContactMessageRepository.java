package com.orgfarmer.repository;

import com.orgfarmer.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findByStatus(String status);
    long countByStatus(String status);
    List<ContactMessage> findByUserId(Long userId);
}