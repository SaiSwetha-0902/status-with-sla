package com.example.status.dao;

import com.example.status.entity.SlaMonitoringEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlaMonitoringDao extends JpaRepository<SlaMonitoringEntity, Long> {

    @Query("SELECT s FROM SlaMonitoringEntity s WHERE s.isResolved = false AND s.slaDeadline <= :currentTime AND s.isSlaBreached = false AND TIMESTAMPDIFF(MINUTE, s.slaDeadline, :currentTime) >= 15")
    List<SlaMonitoringEntity> findSlaBreachedRecords(LocalDateTime currentTime);

    @Query("SELECT s FROM SlaMonitoringEntity s WHERE s.isResolved = false")
    List<SlaMonitoringEntity> findUnresolvedRecords();

    Optional<SlaMonitoringEntity> findByFileIdAndCurrentStateAndIsResolvedFalse(String fileId, String currentState);
    
    Optional<SlaMonitoringEntity> findByOrderIdAndDistributorIdAndCurrentStateAndIsResolvedFalse(String orderId, Integer distributorId, String currentState);
    
    Optional<SlaMonitoringEntity> findFirstByFileIdAndIsResolvedFalseOrderByReceivedTimeDesc(String fileId);
    
    Optional<SlaMonitoringEntity> findFirstByOrderIdAndDistributorIdAndIsResolvedFalseOrderByReceivedTimeDesc(String orderId, Integer distributorId);
    
    @Query("SELECT COUNT(s) FROM SlaMonitoringEntity s WHERE s.isResolved = false")
    long countUnresolvedRecords();
    
    @Query("SELECT COUNT(s) FROM SlaMonitoringEntity s WHERE s.isSlaBreached = true AND TIMESTAMPDIFF(MINUTE, s.slaDeadline, s.breachTime) >= 15")
    long countBreachedRecords();
}