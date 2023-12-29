package org.c4marathon.assignment.todo.repository;

import org.c4marathon.assignment.todo.entity.TodoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<TodoEntity, Long> {
}
