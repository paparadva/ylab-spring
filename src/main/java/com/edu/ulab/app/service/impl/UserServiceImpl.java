package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.entity.User;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.UserMapper;
import com.edu.ulab.app.service.UserService;
import com.edu.ulab.app.storage.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = mapper.userDtoToUserEntity(userDto);
        User createdUser = repository.save(user);
        return mapper.userEntityToUserDto(createdUser);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User user = mapper.userDtoToUserEntity(userDto);
        User updatedUser = repository.update(user);
        return mapper.userEntityToUserDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = repository.getUserById(id).orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
        List<Long> bookIds = user.getBooks().stream()
                .map(Book::getId)
                .toList();
        UserDto userDto = mapper.userEntityToUserDto(user);
        userDto.setBookIds(bookIds);
        return userDto;
    }

    @Override
    public void deleteUserById(Long id) {
        repository.deleteUserById(id);
    }
}
