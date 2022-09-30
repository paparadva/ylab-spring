package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.service.BookService;
import com.edu.ulab.app.storage.memory.BookInMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("bookInMemoryService")
@RequiredArgsConstructor
public class BookServiceInMemoryImpl implements BookService {
    private final BookInMemoryRepository repository;
    private final BookMapper mapper;

    @Override
    public BookDto createBook(BookDto bookDto) {
        Book book = mapper.bookDtoToBookEntity(bookDto);
        Book savedBook = repository.save(book);
        return mapper.bookEntityToBookDto(savedBook);
    }

    @Override
    public BookDto updateBook(BookDto bookDto) {
        Book book = mapper.bookDtoToBookEntity(bookDto);
        Book updatedBook = repository.update(book);
        return mapper.bookEntityToBookDto(updatedBook);
    }

    @Override
    public BookDto getBookById(Long id) {
        return mapper.bookEntityToBookDto(
                repository.getBookById(id)
                        .orElseThrow(() -> new NotFoundException("Book with id " + id + " not found")));
    }

    @Override
    public void deleteBookById(Long id) {
        repository.deleteBookById(id);
    }
}
