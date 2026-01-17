package com.example.status.service;

import com.example.status.dao.SlaMonitoringDao;
import com.example.status.entity.SlaMonitoringEntity;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SlaMonitoringService {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private SlaMonitoringDao slaMonitoringDao;

    // Metrics
    private final Gauge unresolvedCountGauge;
    private final Gauge breachedCountGauge;
    private final Timer resolutionTimer;

    public SlaMonitoringService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize metrics
        this.unresolvedCountGauge = Gauge.builder("sla.unresolved.count", this, SlaMonitoringService::getUnresolvedCount)
                .description("Number of unresolved SLA records")
                .register(meterRegistry);
                
        this.breachedCountGauge = Gauge.builder("sla.breached.count", this, SlaMonitoringService::getBreachedCount)
                .description("Number of SLA breached records")
                .register(meterRegistry);
                
        this.resolutionTimer = Timer.builder("sla.resolution.time")
                .description("Time taken to resolve SLA records")
                .register(meterRegistry);
    }

    private static final int DEFAULT_SLA_MINUTES = 15;

    public void trackNewMessage(String fileId, String orderId, Integer distributorId, String currentState, String sourceService) {

        SlaMonitoringEntity slaEntity = new SlaMonitoringEntity();
        slaEntity.setFileId(fileId);
        slaEntity.setOrderId(orderId);
        slaEntity.setDistributorId(distributorId);
        slaEntity.setCurrentState(currentState);
        slaEntity.setSourceService(sourceService);
        slaEntity.setReceivedTime(LocalDateTime.now());
        slaEntity.setSlaDeadline(calculateSlaDeadline(sourceService));
        slaEntity.setLastCheckTime(LocalDateTime.now());

        slaMonitoringDao.save(slaEntity);
        log.info("SLA tracking started for {} with deadline: {}", getIdentifier(slaEntity), slaEntity.getSlaDeadline());
    }

    private LocalDateTime calculateSlaDeadline(String sourceService) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline;

        switch (sourceService != null ? sourceService.toLowerCase() : "") {
            case "navparse-ms":
            case "nav-parse-ms":
                // NAV processing cutoff at 17:00
                deadline = now.toLocalDate().atTime(17, 0);
                if (deadline.isBefore(now)) {
                    deadline = deadline.plusDays(1); // Next day if already past
                }
                break;

            case "canonical-transformation-ms":
            case "canonical_transformation_ms":
                // Order processing cutoff at 15:00
                deadline = now.toLocalDate().atTime(15, 0);
                if (deadline.isBefore(now)) {
                    deadline = deadline.plusDays(1); // Next day if already past
                }
                break;

            case "netting-ms":
                // Netting cutoff at 00:00 (midnight)
                deadline = now.toLocalDate().plusDays(1).atStartOfDay();
                break;

            case "position-valuation":
            case "position_valuation":
                // Position valuation at 17:05
                deadline = now.toLocalDate().atTime(17, 5);
                if (deadline.isBefore(now)) {
                    deadline = deadline.plusDays(1); // Next day if already past
                }
                break;

            case "simulator-ms":
                // Simulator cutoff at 17:00
                deadline = now.toLocalDate().atTime(17, 0);
                if (deadline.isBefore(now)) {
                    deadline = deadline.plusDays(1); // Next day if already past
                }
                break;

            default:
                // Default 15-minute SLA for other services
                deadline = now.plusMinutes(DEFAULT_SLA_MINUTES);
                break;
        }

        return deadline;
    }

    public void resolveMessage(String fileId, String orderId, Integer distributorId, String resolvedState) {
        Optional<SlaMonitoringEntity> slaRecord = findActiveRecord(fileId, orderId, distributorId);
        
        if (slaRecord.isPresent()) {
            SlaMonitoringEntity entity = slaRecord.get();
            LocalDateTime resolvedTime = LocalDateTime.now();
            entity.setIsResolved(true);
            entity.setResolvedTime(resolvedTime);
            slaMonitoringDao.save(entity);
            
            // Record resolution time metric
            if (entity.getReceivedTime() != null) {
                Duration resolutionDuration = Duration.between(entity.getReceivedTime(), resolvedTime);
                resolutionTimer.record(resolutionDuration);
            }
            
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

    private Optional<SlaMonitoringEntity> findActiveRecord(String fileId, String orderId, Integer distributorId) {
        if (fileId != null) {
            return slaMonitoringDao.findByFileIdAndCurrentStateAndIsResolvedFalse(fileId, "RECEIVED");
        } else if (orderId != null && distributorId != null) {
            return slaMonitoringDao.findByOrderIdAndDistributorIdAndCurrentStateAndIsResolvedFalse(orderId, distributorId, "RECEIVED");
        }
        return Optional.empty();
    }

    private String getIdentifier(SlaMonitoringEntity entity) {
        if (entity.getFileId() != null) return "FileId:" + entity.getFileId();
        if (entity.getOrderId() != null) return "OrderId:" + entity.getOrderId();
        return "Unknown";
    }

    // Metric methods
    private int getUnresolvedCount() {
        return (int) slaMonitoringDao.countUnresolvedRecords();
    }

    private int getBreachedCount() {
        return (int) slaMonitoringDao.countBreachedRecords();
    }
}