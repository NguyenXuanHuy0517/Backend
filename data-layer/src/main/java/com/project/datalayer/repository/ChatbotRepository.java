package com.project.datalayer.repository;

import com.project.datalayer.entity.ChatbotHistory;

public interface ChatbotRepository extends org.springframework.data.jpa.repository.JpaRepository<ChatbotHistory, Long> {
}
