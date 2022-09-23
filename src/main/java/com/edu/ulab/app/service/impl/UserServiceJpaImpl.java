package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.UserMapper;
import com.edu.ulab.app.service.UserService;
import com.edu.ulab.app.storage.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service("userJpaService")
@RequiredArgsConstructor
public class UserServiceJpaImpl implements UserService {
    private final UserJpaRepository repository;
    private final UserMapper mapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        Person person = mapper.userDtoToUserEntity(userDto);
        Person savedPerson = repository.save(person);
        log.info("Created user jpa entity: {}", savedPerson);
        return mapper.userEntityToUserDto(savedPerson);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        Person person = mapper.userDtoToUserEntity(userDto);
        if (repository.existsById(person.getId())) {
            Person savedPerson = repository.save(person);
            log.info("Saved user jpa entity: {}", savedPerson);
            return mapper.userEntityToUserDto(savedPerson);
        } else {
            throw new NotFoundException("User with id " + person.getId() + " not found");
        }
    }

    @Override
    public UserDto getUserById(Long id) {
        Person person = repository.findById(id).orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
        log.info("Retrieved user jpa entity: {}", person);
        List<Long> bookIds = person.getBooks()
                .stream()
                .filter(Objects::nonNull)
                .peek(book -> log.info("Retrieved book: {}", book))
                .map(Book::getId)
                .toList();
        UserDto userDto = mapper.userEntityToUserDto(person);
        userDto.setBookIds(bookIds);
        return userDto;
    }

    @Override
    public void deleteUserById(Long id) {
        repository.deleteById(id);
    }
}