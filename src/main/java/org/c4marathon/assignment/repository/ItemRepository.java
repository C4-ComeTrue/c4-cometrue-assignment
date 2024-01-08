package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}
