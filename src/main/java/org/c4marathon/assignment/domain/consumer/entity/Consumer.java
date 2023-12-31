package org.c4marathon.assignment.domain.consumer.entity;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consumer_tbl")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Consumer extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "consumer_id", unique = true, nullable = false, updatable = false, columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "balance", columnDefinition = "BIGINT DEFAULT 0")
	private Long balance;

	@NotNull
	@Column(name = "email", columnDefinition = "VARCHAR(50)", unique = true, updatable = false)
	private String email;

	@NotNull
	@Column(name = "address", columnDefinition = "VARCHAR(100)")
	private String address;

	@NotNull
	@Column(name = "is_deleted", columnDefinition = "BIT DEFAULT 0")
	private Boolean isDeleted;

	@Builder
	public Consumer(String email, String address) {
		this.email = email;
		this.address = address;
		this.balance = 0L;
		this.isDeleted = false;
	}
}
