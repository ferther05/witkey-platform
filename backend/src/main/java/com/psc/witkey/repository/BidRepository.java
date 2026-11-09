package com.psc.witkey.repository;

import com.psc.witkey.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    Optional<Bid> findByTaskIdAndWitkeyId(Long taskId, Long witkeyId);

    List<Bid> findByWitkeyIdOrderByCreatedAtDesc(Long witkeyId);

    long countByTaskIdAndStatus(Long taskId, String status);

    long countByTaskId(Long taskId);
}
