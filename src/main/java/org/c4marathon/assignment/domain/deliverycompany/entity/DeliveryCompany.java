package org.c4marathon.assignment.domain.deliverycompany.entity;

import org.c4marathon.assignment.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "delivery_company_tbl",
	indexes = {
		@Index(name = "delivery_company_email_idx", columnList = "email", unique = true)
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DeliveryCompany extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "delivery_company_id", unique = true, nullable = false, updatable = false,
		columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "email", columnDefinition = "VARCHAR(50)")
	private String email;

	@Builder
	public DeliveryCompany(String email) {
		this.email = email;
	}
}
