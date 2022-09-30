package com.edu.ulab.app.storage.memory;

import com.edu.ulab.app.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookInMemoryRepository {
    Book save(Book book);
    Book update(Book book);
    Book saveOrUpdate(Book book);
    Optional<Book> getBookById(Long id);
    List<Book> getAllBooksByUserId(Long id);
    void deleteBookById(Long id);
}
