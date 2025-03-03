package org.c4marathon.assignment.domain;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity {

	@CreatedDate
	@Column(name = "created_at", nullable = false, columnDefinition = "datetime")
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false, columnDefinition = "datetime")
	private Instant updatedAt;

	@Column(name = "deleted_at", columnDefinition = "datetime")
	private Instant deletedAt;

	protected BaseEntity(Instant createdAt, Instant updatedAt, Instant deletedAt) {
		this.createdAt = createdAt != null ? createdAt : Instant.now();
		this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
		this.deletedAt = deletedAt;
	}
}
