package com.example.status.controller;

import com.example.status.dao.SlaMonitoringDao;
import com.example.status.entity.SlaMonitoringEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sla")
@Tag(name = "SLA Monitoring", description = "SLA monitoring and tracking endpoints")
public class SlaController {

    @Autowired
    private SlaMonitoringDao slaMonitoringDao;

    @GetMapping("/breached")
    @Operation(summary = "Get SLA breached records", description = "Returns all records that have breached the 15-minute SLA")
    public List<SlaMonitoringEntity> getSlaBreachedRecords() {
        return slaMonitoringDao.findSlaBreachedRecords(LocalDateTime.now());
    }

    @GetMapping("/unresolved")
    @Operation(summary = "Get unresolved records", description = "Returns all records that are still pending resolution")
    public List<SlaMonitoringEntity> getUnresolvedRecords() {
        return slaMonitoringDao.findUnresolvedRecords();
    }

    @GetMapping("/all")
    @Operation(summary = "Get all SLA records", description = "Returns all SLA monitoring records")
    public List<SlaMonitoringEntity> getAllSlaRecords() {
        return slaMonitoringDao.findAll();
    }
}