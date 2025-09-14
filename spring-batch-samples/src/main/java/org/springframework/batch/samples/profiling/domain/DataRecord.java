package org.springframework.batch.samples.profiling.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DataRecord implements Serializable {

	private Long id;

	private String uuid;

	private String data;

	private String category;

	private BigDecimal amount;

	private LocalDateTime timestamp;

	private Integer status;

	private String description;

	private byte[] payload;

	private Double metric1;

	private Double metric2;

	private Double metric3;

	public DataRecord() {
	}

	public DataRecord(Long id) {
		this.id = id;
		this.uuid = UUID.randomUUID().toString();
		this.data = generateData(id);
		this.category = "CAT-" + (id % 100);
		this.amount = BigDecimal.valueOf(Math.random() * 10000);
		this.timestamp = LocalDateTime.now();
		this.status = (int) (id % 5);
		this.description = "Description for record " + id + " with various attributes and metadata";
		this.payload = new byte[1024]; // 1KB payload
		this.metric1 = Math.random() * 1000;
		this.metric2 = Math.random() * 1000;
		this.metric3 = Math.random() * 1000;
	}

	private String generateData(Long id) {
		StringBuilder sb = new StringBuilder();
		sb.append("DATA-").append(id).append("-");
		for (int i = 0; i < 10; i++) {
			sb.append(UUID.randomUUID().toString().substring(0, 8)).append("-");
		}
		return sb.toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	public Double getMetric1() {
		return metric1;
	}

	public void setMetric1(Double metric1) {
		this.metric1 = metric1;
	}

	public Double getMetric2() {
		return metric2;
	}

	public void setMetric2(Double metric2) {
		this.metric2 = metric2;
	}

	public Double getMetric3() {
		return metric3;
	}

	public void setMetric3(Double metric3) {
		this.metric3 = metric3;
	}

	@Override
	public String toString() {
		return "DataRecord{id=" + id + ", uuid='" + uuid + "', category='" + category + "', amount=" + amount
				+ ", status=" + status + "}";
	}

}