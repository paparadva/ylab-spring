package com.edu.ulab.app.service;

import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.exception.ServiceException;
import com.edu.ulab.app.mapper.UserMapper;
import com.edu.ulab.app.service.impl.UserServiceJpaImpl;
import com.edu.ulab.app.storage.jpa.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Тестирование функционала {@link com.edu.ulab.app.service.impl.UserServiceJpaImpl}.
 */
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing user functionality.")
public class UserServiceJpaImplTest {
    @InjectMocks
    UserServiceJpaImpl userService;

    @Mock
    UserJpaRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Test
    @DisplayName("Создание пользователя. Должно пройти успешно.")
    void savePerson_Test() {
        //given

        UserDto userDto = new UserDto();
        userDto.setAge(11);
        userDto.setFullName("test name");
        userDto.setTitle("test title");
        userDto.setPreferredGenre("fantasy");

        Person person  = new Person();
        person.setFullName("test name");
        person.setAge(11);
        person.setTitle("test title");
        person.setPreferredGenre("fantasy");

        Person savedPerson  = new Person();
        savedPerson.setId(1L);
        savedPerson.setFullName("test name");
        savedPerson.setAge(11);
        savedPerson.setTitle("test title");
        savedPerson.setPreferredGenre("fantasy");

        UserDto result = new UserDto();
        result.setId(1L);
        result.setAge(11);
        result.setFullName("test name");
        result.setTitle("test title");
        result.setPreferredGenre("fantasy");


        //when

        when(userMapper.userDtoToUserEntity(userDto)).thenReturn(person);
        when(userRepository.save(person)).thenReturn(savedPerson);
        when(userMapper.userEntityToUserDto(savedPerson)).thenReturn(result);


        //then

        UserDto userDtoResult = userService.createUser(userDto);
        assertEquals(1L, userDtoResult.getId());
    }

    @Test
    @DisplayName("Обновление пользователя. Должно пройти успешно.")
    void updatePerson_Test() {
        //given

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setAge(11);
        userDto.setFullName("test name");
        userDto.setTitle("test title");
        userDto.setPreferredGenre("fantasy");

        Person person  = new Person();
        person.setId(1L);
        person.setFullName("test name");
        person.setAge(11);
        person.setTitle("test title");
        person.setPreferredGenre("fantasy");

        Person savedPerson  = new Person();
        savedPerson.setId(1L);
        savedPerson.setFullName("test name");
        savedPerson.setAge(11);
        savedPerson.setTitle("test title");
        savedPerson.setPreferredGenre("fantasy");

        UserDto result = new UserDto();
        result.setId(1L);
        result.setAge(11);
        result.setFullName("test name");
        result.setTitle("test title");
        result.setPreferredGenre("fantasy");


        //when

        when(userMapper.userDtoToUserEntity(userDto)).thenReturn(person);
        when(userRepository.existsById(savedPerson.getId())).thenReturn(true);
        when(userRepository.save(person)).thenReturn(savedPerson);
        when(userMapper.userEntityToUserDto(savedPerson)).thenReturn(result);


        //then

        UserDto userDtoResult = userService.updateUser(userDto);
        assertEquals(1L, userDtoResult.getId());
    }

    @Test
    @DisplayName("Обновление пользователя. Должно пройти успешно.")
    void getById_Test() {
        //given

        long searchUserId = 1L;

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setAge(11);
        userDto.setFullName("test name");
        userDto.setTitle("test title");
        userDto.setPreferredGenre("fantasy");

        Person person  = new Person();
        person.setId(1L);
        person.setFullName("test name");
        person.setAge(11);
        person.setTitle("test title");
        person.setPreferredGenre("fantasy");
        person.setBooks(List.of());

        UserDto result = new UserDto();
        result.setId(1L);
        result.setAge(11);
        result.setFullName("test name");
        result.setTitle("test title");
        result.setPreferredGenre("fantasy");


        //when

        when(userRepository.findById(searchUserId)).thenReturn(Optional.of(person));
        when(userMapper.userEntityToUserDto(person)).thenReturn(result);


        //then

        UserDto userDtoResult = userService.getUserById(searchUserId);
        assertEquals(searchUserId, userDtoResult.getId());
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя по id. Должно выбросить ошибку сервиса.")
    void deleteBook_notFound_Test() {
        //given
        long userId = 1L;

        //when
        doThrow(new EmptyResultDataAccessException(1)).when(userRepository).deleteById(userId);

        //then
        assertThatThrownBy(() -> userService.deleteUserById(userId))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("User with id 1 does not exist");
    }
}
