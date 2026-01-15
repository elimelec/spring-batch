CREATE TABLE IF NOT EXISTS data_record (
    id BIGINT PRIMARY KEY,
    uuid VARCHAR(255),
    data VARCHAR(1000),
    category VARCHAR(100),
    amount DECIMAL(10,2),
    timestamp TIMESTAMP,
    status INT,
    description VARCHAR(500),
    metric1 DOUBLE,
    metric2 DOUBLE,
    metric3 DOUBLE
);

CREATE INDEX IF NOT EXISTS idx_data_record_category ON data_record(category);
CREATE INDEX IF NOT EXISTS idx_data_record_status ON data_record(status);
CREATE INDEX IF NOT EXISTS idx_data_record_timestamp ON data_record(timestamp);