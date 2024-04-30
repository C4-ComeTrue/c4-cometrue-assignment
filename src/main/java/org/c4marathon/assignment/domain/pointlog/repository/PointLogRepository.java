package org.c4marathon.assignment.domain.pointlog.repository;

import java.util.List;

import org.c4marathon.assignment.domain.pointlog.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {

	@Query(
		nativeQuery = true,
		value = """
			SELECT *
			FROM point_log pl
			WHERE point_log_id > :id
			ORDER BY point_log_id
			LIMIT :pageSize
			""")
	List<PointLog> findByIdWithPaging(@Param("id") Long id, @Param("pageSize") int pageSize);
}
