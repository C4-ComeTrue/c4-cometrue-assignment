package org.c4marathon.assignment.domain.seller.entity;

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
@Table(name = "seller_tbl")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Seller extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "seller_id", unique = true, nullable = false, updatable = false, columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "email", columnDefinition = "VARCHAR(50)", unique = true, updatable = false)
	private String email;

	@NotNull
	@Column(name = "password", columnDefinition = "VARCHAR(100)")
	private String password;

	@NotNull
	@Column(name = "is_deleted", columnDefinition = "TINYINT DEFAULT 0")
	private Boolean isDeleted;

	@Builder
	public Seller(String email, String password) {
		this.email = email;
		this.password = password;
	}
}
