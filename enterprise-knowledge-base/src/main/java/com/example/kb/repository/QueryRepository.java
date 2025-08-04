package com.example.kb.repository;

import com.example.kb.model.QueryEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryRepository extends JpaRepository<QueryEntity, Long> {

    List<QueryEntity> findByUserIdOrderByQueryTimeDesc(String userId, PageRequest pageRequest);
}