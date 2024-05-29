package org.c4marathon.assignment.domain.product.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.c4marathon.assignment.domain.product.dto.request.ProductSearchRequest;
import org.c4marathon.assignment.domain.product.dto.response.ProductSearchEntry;

@Mapper
public interface ProductMapper {
	List<ProductSearchEntry> selectByCondition(ProductSearchRequest request);
}
