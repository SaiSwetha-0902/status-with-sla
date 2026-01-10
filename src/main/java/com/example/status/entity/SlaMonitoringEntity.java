package com.example.status.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sla_monitoring")
public class SlaMonitoringEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id")
    private String fileId;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "distributor_id")
    private Integer distributorId;

    @Column(name = "current_state", nullable = false)
    private String currentState;

    @Column(name = "source_service", nullable = false)
    private String sourceService;

    @Column(name = "received_time", nullable = false)
    private LocalDateTime receivedTime;

    @Column(name = "sla_deadline", nullable = false)
    private LocalDateTime slaDeadline;

    @Column(name = "is_sla_breached", nullable = false)
    private Boolean isSlaBreached = false;

    @Column(name = "breach_time")
    private LocalDateTime breachTime;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    @Column(name = "resolved_time")
    private LocalDateTime resolvedTime;

    @Column(name = "last_check_time")
    private LocalDateTime lastCheckTime;
}