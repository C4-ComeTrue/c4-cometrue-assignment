package org.c4marathon.assignment.domain.review.entity;

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
	name = "review_tbl",
	indexes = {
		@Index(name = "consumer_id_product_id_idx", columnList = "consumer_id, product_id")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_id", columnDefinition = "BIGINT")
	private Long id;

	@NotNull
	@Column(name = "consumer_id", columnDefinition = "BIGINT")
	private Long consumerId;

	@NotNull
	@Column(name = "product_id", columnDefinition = "BIGINT")
	private Long productId;

	@NotNull
	@Column(name = "score", columnDefinition = "INTEGER default 3")
	private int score;

	@Column(name = "comment", columnDefinition = "VARCHAR(100)")
	private String comment;

	@Builder
	public Review(
		Long consumerId,
		Long productId,
		int score,
		String comment
	) {
		this.consumerId = consumerId;
		this.productId = productId;
		this.score = score;
		this.comment = comment;
	}
}
