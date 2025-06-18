package com.sunelection.repository;

import com.sunelection.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findByParty(String party);
    List<Candidate> findByNameContainingIgnoreCase(String name);
} 