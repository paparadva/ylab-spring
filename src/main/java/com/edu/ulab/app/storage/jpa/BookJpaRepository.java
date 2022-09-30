package com.edu.ulab.app.storage.jpa;

import com.edu.ulab.app.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookJpaRepository extends JpaRepository<Book, Long> {
}
