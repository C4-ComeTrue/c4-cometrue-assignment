package org.c4marathon.assignment.todo.repository;

import org.c4marathon.assignment.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {

}
