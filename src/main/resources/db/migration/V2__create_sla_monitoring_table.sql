CREATE TABLE sla_monitoring (
    id BIGSERIAL PRIMARY KEY,
    file_id VARCHAR(255),
    order_id VARCHAR(255),
    distributor_id INTEGER,
    mqid VARCHAR(255),
    current_state VARCHAR(50) NOT NULL,
    source_service VARCHAR(100) NOT NULL,
    received_time TIMESTAMP NOT NULL,
    sla_deadline TIMESTAMP NOT NULL,
    is_sla_breached BOOLEAN NOT NULL DEFAULT FALSE,
    breach_time TIMESTAMP,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_time TIMESTAMP,
    last_check_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sla_monitoring_sla_deadline ON sla_monitoring(sla_deadline);
CREATE INDEX idx_sla_monitoring_is_resolved ON sla_monitoring(is_resolved);
CREATE INDEX idx_sla_monitoring_is_sla_breached ON sla_monitoring(is_sla_breached);
CREATE INDEX idx_sla_monitoring_file_id ON sla_monitoring(file_id);
CREATE INDEX idx_sla_monitoring_order_id ON sla_monitoring(order_id);
CREATE INDEX idx_sla_monitoring_mqid ON sla_monitoring(mqid);