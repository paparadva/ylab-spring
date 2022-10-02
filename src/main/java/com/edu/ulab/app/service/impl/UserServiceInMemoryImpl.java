package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.exception.EntityDoesNotExistException;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.UserMapper;
import com.edu.ulab.app.service.UserService;
import com.edu.ulab.app.storage.memory.UserInMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("userInMemoryService")
@RequiredArgsConstructor
public class UserServiceInMemoryImpl implements UserService {
    private final UserInMemoryRepository repository;
    private final UserMapper mapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        Person person = mapper.userDtoToUserEntity(userDto);
        Person createdPerson = repository.save(person);
        return mapper.userEntityToUserDto(createdPerson);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        try {
            Person person = mapper.userDtoToUserEntity(userDto);
            Person updatedPerson = repository.update(person);
            return mapper.userEntityToUserDto(updatedPerson);

        } catch (EntityDoesNotExistException ex) {
            throw new NotFoundException(ex);
        }
    }

    @Override
    public UserDto getUserById(Long id) {
        try {
            Person person = repository.getUserById(id).orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
            List<Long> bookIds = person.getBooks().stream()
                    .map(Book::getId)
                    .toList();
            UserDto userDto = mapper.userEntityToUserDto(person);
            userDto.setBookIds(bookIds);
            return userDto;

        } catch (EntityDoesNotExistException ex) {
            throw new NotFoundException(ex);
        }
    }

    @Override
    public void deleteUserById(Long id) {
        repository.deleteUserById(id);
    }
}
