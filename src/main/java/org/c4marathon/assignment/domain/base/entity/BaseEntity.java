package org.c4marathon.assignment.domain.base.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

	@NotNull
	@CreatedDate
	@Column(name = "created_at", columnDefinition = "DATETIME(6)", updatable = false)
	protected LocalDateTime createdAt;

	@NotNull
	@LastModifiedDate
	@Column(name = "updated_at", columnDefinition = "DATETIME(6)")
	protected LocalDateTime updatedAt;
}
