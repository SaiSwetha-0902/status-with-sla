package com.example.status.service;

import com.example.status.dao.SlaMonitoringDao;
import com.example.status.entity.SlaMonitoringEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SlaMonitoringService {

    public SlaMonitoringService() {
        System.out.println("start");
    }

    @Autowired
    private SlaMonitoringDao slaMonitoringDao;

    private static final int SLA_MINUTES = 15;

    public void trackNewMessage(String fileId, String orderId, Integer distributorId, String mqid, 
                               String currentState, String sourceService) {
        
        SlaMonitoringEntity slaEntity = new SlaMonitoringEntity();
        slaEntity.setFileId(fileId);
        slaEntity.setOrderId(orderId);
        slaEntity.setDistributorId(distributorId);
        slaEntity.setMqid(mqid);
        slaEntity.setCurrentState(currentState);
        slaEntity.setSourceService(sourceService);
        slaEntity.setReceivedTime(LocalDateTime.now());
        slaEntity.setSlaDeadline(LocalDateTime.now().plusMinutes(SLA_MINUTES));
        slaEntity.setLastCheckTime(LocalDateTime.now());

        slaMonitoringDao.save(slaEntity);
        log.info("SLA tracking started for {} with deadline: {}", getIdentifier(slaEntity), slaEntity.getSlaDeadline());
    }

    public void resolveMessage(String fileId, String orderId, Integer distributorId, String mqid, String resolvedState) {
        Optional<SlaMonitoringEntity> slaRecord = findActiveRecord(fileId, orderId, distributorId, mqid);
        
        if (slaRecord.isPresent()) {
            SlaMonitoringEntity entity = slaRecord.get();
            entity.setIsResolved(true);
            entity.setResolvedTime(LocalDateTime.now());
            slaMonitoringDao.save(entity);
            
            log.info("SLA resolved for {} at state: {}", getIdentifier(entity), resolvedState);
        }
    }

    @Scheduled(fixedRate = 60000) 
    public void checkSlaCompliance() {
        LocalDateTime currentTime = LocalDateTime.now();
        
       
        List<SlaMonitoringEntity> breachedRecords = slaMonitoringDao.findSlaBreachedRecords(currentTime);
        
        for (SlaMonitoringEntity record : breachedRecords) {
            record.setIsSlaBreached(true);
            record.setBreachTime(currentTime);
            record.setLastCheckTime(currentTime);
            slaMonitoringDao.save(record);
            
            log.warn("SLA BREACH DETECTED for {} - State: {} - Deadline: {} - Current: {}", 
                    getIdentifier(record), record.getCurrentState(), 
                    record.getSlaDeadline(), currentTime);
        }

        List<SlaMonitoringEntity> unresolvedRecords = slaMonitoringDao.findUnresolvedRecords();
        for (SlaMonitoringEntity record : unresolvedRecords) {
            if (!record.getIsSlaBreached()) {
                record.setLastCheckTime(currentTime);
                slaMonitoringDao.save(record);
            }
        }

        if (!breachedRecords.isEmpty()) {
            log.info("SLA Check completed - {} breaches detected out of {} unresolved records", 
                    breachedRecords.size(), unresolvedRecords.size());
        }
    }

    private Optional<SlaMonitoringEntity> findActiveRecord(String fileId, String orderId, Integer distributorId, String mqid) {
        if (fileId != null) {
            return slaMonitoringDao.findByFileIdAndCurrentStateAndIsResolvedFalse(fileId, "RECEIVED");
        } else if (mqid != null) {
            return slaMonitoringDao.findByMqidAndCurrentStateAndIsResolvedFalse(mqid, "RECEIVED");
        } else if (orderId != null && distributorId != null) {
            return slaMonitoringDao.findByOrderIdAndDistributorIdAndCurrentStateAndIsResolvedFalse(orderId, distributorId, "RECEIVED");
        }
        return Optional.empty();
    }

    private String getIdentifier(SlaMonitoringEntity entity) {
        if (entity.getFileId() != null) return "FileId:" + entity.getFileId();
        if (entity.getMqid() != null) return "MqId:" + entity.getMqid();
        if (entity.getOrderId() != null) return "OrderId:" + entity.getOrderId();
        return "Unknown";
    }
}