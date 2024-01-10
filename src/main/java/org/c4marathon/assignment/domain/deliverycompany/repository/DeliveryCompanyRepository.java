package org.c4marathon.assignment.domain.deliverycompany.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeliveryCompanyRepository extends JpaRepository<DeliveryCompany, Long> {

	Boolean existsByEmail(String email);

	Optional<DeliveryCompany> findByEmail(String email);

	@Query(
		nativeQuery = true,
		value = """
			SELECT dc.created_at, dc.delivery_company_id, dc.updated_at, dc.email
			FROM delivery_company_tbl dc LEFT JOIN delivery_tbl d ON dc.delivery_company_id = d.delivery_company_id
			GROUP BY dc.delivery_company_id
			ORDER BY COUNT(d.delivery_id) ASC
			LIMIT 1
			""")
	Optional<DeliveryCompany> findMinimumCountOfDelivery();
}
