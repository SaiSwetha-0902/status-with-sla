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

    @Query("SELECT s FROM SlaMonitoringEntity s WHERE s.isResolved = false AND s.slaDeadline <= :currentTime AND s.isSlaBreached = false")
    List<SlaMonitoringEntity> findSlaBreachedRecords(LocalDateTime currentTime);

    @Query("SELECT s FROM SlaMonitoringEntity s WHERE s.isResolved = false")
    List<SlaMonitoringEntity> findUnresolvedRecords();

    Optional<SlaMonitoringEntity> findByFileIdAndCurrentStateAndIsResolvedFalse(String fileId, String currentState);
    
    Optional<SlaMonitoringEntity> findByOrderIdAndDistributorIdAndCurrentStateAndIsResolvedFalse(String orderId, Integer distributorId, String currentState);
    
    Optional<SlaMonitoringEntity> findByMqidAndCurrentStateAndIsResolvedFalse(String mqid, String currentState);
}