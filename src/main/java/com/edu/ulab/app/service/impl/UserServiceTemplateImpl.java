package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.exception.ServiceException;
import com.edu.ulab.app.mapper.UserMapper;
import com.edu.ulab.app.service.BookService;
import com.edu.ulab.app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Service("userTemplateService")
@Slf4j
public class UserServiceTemplateImpl implements UserService {

    private final JdbcTemplate jdbcTemplate;
    private final UserMapper mapper;
    private final BookService bookService;

    private static final BeanPropertyRowMapper<Person> personRowMapper = new BeanPropertyRowMapper<>(Person.class);

    public UserServiceTemplateImpl(
            JdbcTemplate jdbcTemplate,
            UserMapper mapper,
            @Qualifier("bookTemplateService") BookService bookService) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
        this.bookService = bookService;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        final String INSERT_SQL = "INSERT INTO PERSON(FULL_NAME, TITLE, AGE) VALUES (?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
                        ps.setString(1, userDto.getFullName());
                        ps.setString(2, userDto.getTitle());
                        ps.setLong(3, userDto.getAge());
                        return ps;
                    },
                    keyHolder);
        } catch (DataAccessException ex) {
            throw new ServiceException(ex);
        }

        userDto.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("Created user with id {}", userDto.getId());
        return userDto;
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        final String UPDATE_SQL = "UPDATE PERSON SET FULL_NAME=?, TITLE=?, AGE=? WHERE ID=?";
        Objects.requireNonNull(userDto.getId());

        int updateCount;
        try {
            queryBookIds(userDto.getId()).forEach(bookService::deleteBookById);

            updateCount = jdbcTemplate.update(UPDATE_SQL,
                    userDto.getFullName(),
                    userDto.getTitle(),
                    userDto.getAge(),
                    userDto.getId());
        } catch (DataAccessException ex) {
            throw new ServiceException(ex);
        }

        if (updateCount > 0) {
            log.info("Updated user with id {}", userDto.getId());
            return userDto;
        } else {
            throw new NotFoundException("User with id " + userDto.getId() + " not found");
        }
    }

    @Override
    public UserDto getUserById(Long id) {
        final String SELECT_USER_SQL = "SELECT ID, FULL_NAME, TITLE, AGE FROM PERSON WHERE ID=?";
        Objects.requireNonNull(id);

        Person user;
        try {
            user = jdbcTemplate.query(SELECT_USER_SQL, personRowMapper, id)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
            log.info("Found user: {}", user);
        } catch (DataAccessException ex) {
            throw new ServiceException(ex);
        }

        UserDto userDto = mapper.userEntityToUserDto(user);

        List<Long> bookIds = queryBookIds(id);
        if (bookIds.size() > 0) {
            log.info("Found related book ids: {}", bookIds);
            userDto.setBookIds(bookIds);
        }

        return userDto;
    }

    @Override
    public void deleteUserById(Long id) {
        final String DELETE_SQL = "DELETE FROM PERSON WHERE ID=?";
        Objects.requireNonNull(id);

        int updateCount;
        try {
            queryBookIds(id).forEach(bookService::deleteBookById);
            updateCount = jdbcTemplate.update(DELETE_SQL, id);
        } catch (DataAccessException ex) {
            throw new ServiceException(ex);
        }

        if (updateCount > 0) {
            log.info("Deleted user with id {}", id);
        } else {
            log.info("User with id {} does not exist - deleted nothing", id);
        }
    }

    private List<Long> queryBookIds(Long userId) {
        final String SELECT_BOOK_IDS_SQL = "SELECT ID FROM BOOK WHERE USER_ID=?";

        try {
            return jdbcTemplate.queryForList(SELECT_BOOK_IDS_SQL, Long.class, userId);
        } catch (DataAccessException ex) {
            throw new ServiceException(ex);
        }
    }
}
