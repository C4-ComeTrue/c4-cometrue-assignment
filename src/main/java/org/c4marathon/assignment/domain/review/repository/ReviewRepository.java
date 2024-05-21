package org.c4marathon.assignment.domain.review.repository;

import org.c4marathon.assignment.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	boolean existsByConsumerIdAndProductId(Long consumerId, Long productId);
}
