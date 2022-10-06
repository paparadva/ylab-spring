package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.exception.ServiceException;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.service.BookService;
import com.edu.ulab.app.storage.jpa.BookJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service("bookJpaService")
@RequiredArgsConstructor
public class BookServiceJpaImpl implements BookService {
    private final BookJpaRepository repository;
    private final BookMapper mapper;

    @Override
    public BookDto createBook(BookDto bookDto) {
        try {
            Book book = mapper.bookDtoToBookEntity(bookDto);
            Book savedBook = repository.save(book);
            log.info("Created book jpa entity: {}", savedBook);
            return mapper.bookEntityToBookDto(savedBook);

        } catch (DataAccessException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public BookDto updateBook(BookDto bookDto) {
        try {
            Book book = mapper.bookDtoToBookEntity(bookDto);
            if (repository.existsById(book.getId())) {
                Book savedBook = repository.save(book);
                log.info("Updated book jpa entity: {}", savedBook);
                return mapper.bookEntityToBookDto(savedBook);
            } else {
                throw new NotFoundException("Book with id " + book.getId() + " not found");
            }

        } catch (DataAccessException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public BookDto getBookById(Long id) {
        try {
            return mapper.bookEntityToBookDto(
                    repository.findById(id)
                            .orElseThrow(() -> new NotFoundException("Book with id " + id + " not found")));

        } catch (DataAccessException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public void deleteBookById(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new NotFoundException("Book with id " + id + " does not exist - deleted nothing");
        } catch (DataAccessException ex) {
            throw new ServiceException(ex);
        }
    }

}
