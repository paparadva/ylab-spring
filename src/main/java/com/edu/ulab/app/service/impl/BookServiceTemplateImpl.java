package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("bookTemplateService")
@Slf4j
@RequiredArgsConstructor
public class BookServiceTemplateImpl implements BookService {

    private final JdbcTemplate jdbcTemplate;
    private final BookMapper mapper;

    private static final BeanPropertyRowMapper<Book> bookRowMapper = new BeanPropertyRowMapper<>(Book.class);

    @Override
    public BookDto createBook(BookDto bookDto) {
        final String INSERT_SQL = "INSERT INTO BOOK(TITLE, AUTHOR, PAGE_COUNT, USER_ID) VALUES (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    var ps = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
                    ps.setString(1, bookDto.getTitle());
                    ps.setString(2, bookDto.getAuthor());
                    ps.setLong(3, bookDto.getPageCount());
                    ps.setLong(4, bookDto.getUserId());
                    return ps;
                },
                keyHolder);

        bookDto.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("Created book with id {}", bookDto.getId());
        return bookDto;
    }

    @Override
    public BookDto updateBook(BookDto bookDto) {
        final String UPDATE_SQL = "UPDATE BOOK SET TITLE=?, AUTHOR=?, PAGE_COUNT=?, USER_ID=? WHERE ID=?";

        int updateCount = jdbcTemplate.update(UPDATE_SQL,
                bookDto.getTitle(),
                bookDto.getAuthor(),
                bookDto.getPageCount(),
                bookDto.getUserId(),
                Objects.requireNonNull(bookDto.getId()));

        if (updateCount > 0) {
            log.info("Updated book with id {}", bookDto.getId());
            return bookDto;
        } else {
            throw new NotFoundException("Book with id " + bookDto.getId() + " not found");
        }
    }

    @Override
    public BookDto getBookById(Long id) {
        final String SELECT_SQL = "SELECT TITLE, AUTHOR, PAGE_COUNT, USER_ID, ID FROM BOOK WHERE ID=?";
        Objects.requireNonNull(id);

        Book book = jdbcTemplate.query(SELECT_SQL, bookRowMapper, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Book with id " + id + " not found"));

        log.info("Found book: {}", book);
        return mapper.bookEntityToBookDto(book);
    }

    @Override
    public void deleteBookById(Long id) {
        final String DELETE_SQL = "DELETE FROM BOOK WHERE ID=?";
        int updateCount = jdbcTemplate.update(DELETE_SQL, Objects.requireNonNull(id));

        if (updateCount > 0) {
            log.info("Deleted book with id {}", id);
        } else {
            log.info("Book with id {} does not exist - deleted nothing", id);
        }
    }
}
