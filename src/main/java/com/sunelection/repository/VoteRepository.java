package com.sunelection.repository;

import com.sunelection.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    @Query("SELECT COUNT(v) FROM Vote v")
    long countTotalVotes();

    boolean existsByUserId(Long userId);
    List<Vote> findByUserId(Long userId);
    List<Vote> findByCandidateId(Long candidateId);
} 