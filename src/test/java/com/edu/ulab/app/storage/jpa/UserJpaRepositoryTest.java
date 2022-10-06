package com.edu.ulab.app.storage.jpa;

import com.edu.ulab.app.config.SystemJpaTest;
import com.edu.ulab.app.entity.Person;
import com.vladmihalcea.sql.SQLStatementCountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static com.vladmihalcea.sql.SQLStatementCountValidator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тесты репозитория {@link UserJpaRepository}.
 */
@SystemJpaTest
public class UserJpaRepositoryTest {
    @Autowired
    UserJpaRepository userRepository;

    @BeforeEach
    void setUp() {
        SQLStatementCountValidator.reset();
    }

    @DisplayName("Сохранить пользователя. Число select должно равняться 1")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void insertPerson_thenAssertDmlCount() {
        //Given
        Person person = new Person();
        person.setAge(111);
        person.setTitle("reader");
        person.setFullName("Test Test");
        person.setPreferredGenre("fantasy");

        //When
        Person result = userRepository.save(person);

        //Then
        assertThat(result.getAge()).isEqualTo(111);
        assertSelectCount(1);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Обновить пользователя. Должно выполниться по одной операции select, insert и update")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void updatePerson_thenAssertDmlCount() {
        //Given
        Person person = new Person();
        person.setAge(111);
        person.setTitle("old reader");
        person.setFullName("old full name");
        person.setPreferredGenre("old genre");
        Person savedPerson = userRepository.save(person);
        long savedPersonId = savedPerson.getId();

        //When
        savedPerson.setAge(90);
        savedPerson.setTitle("updated reader");
        savedPerson.setFullName("updated full name");
        savedPerson.setPreferredGenre("updated genre");
        Person result = userRepository.save(savedPerson);
        userRepository.flush();

        //Then
        assertThat(result.getId()).isEqualTo(savedPersonId);
        assertThat(result.getAge()).isEqualTo(90);
        assertThat(result.getTitle()).isEqualTo("updated reader");
        assertThat(result.getFullName()).isEqualTo("updated full name");
        assertThat(result.getPreferredGenre()).isEqualTo("updated genre");

        assertSelectCount(1);
        assertInsertCount(1);
        assertUpdateCount(1);
        assertDeleteCount(0);
    }

    @DisplayName("Получить пользователя по id. Число select должно равняться 1")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void getPerson_thenAssertDmlCount() {
        //Given
        Person person = new Person();
        person.setAge(111);
        person.setTitle("old reader");
        person.setFullName("old full name");
        person.setPreferredGenre("old genre");
        Person savedPerson = userRepository.save(person);
        long savedPersonId = savedPerson.getId();

        //When
        Optional<Person> resultOpt = userRepository.findById(savedPersonId);

        //Then
        assertThat(resultOpt.isPresent()).isTrue();
        Person result = resultOpt.get();
        assertThat(result.getId()).isEqualTo(savedPersonId);
        assertThat(result.getAge()).isEqualTo(111);
        assertThat(result.getTitle()).isEqualTo("old reader");
        assertThat(result.getFullName()).isEqualTo("old full name");
        assertThat(result.getPreferredGenre()).isEqualTo("old genre");

        assertSelectCount(1);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Получить всех пользователей")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void getPersonList_thenAssertDmlCount() {
        //Given
        Person person1 = new Person();
        person1.setAge(111);
        person1.setTitle("reader1");
        person1.setFullName("name1");
        person1.setPreferredGenre("genre1");
        userRepository.save(person1);

        Person person2 = new Person();
        person2.setAge(112);
        person2.setTitle("reader2");
        person2.setFullName("name2");
        person2.setPreferredGenre("genre2");
        userRepository.save(person2);

        //When
        List<Person> personList = userRepository.findAll();

        //Then
        assertThat(personList.size()).isEqualTo(2);

        assertSelectCount(3);
        assertInsertCount(2);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Удалить пользователя")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void deletePerson_thenAssertDmlCount() {
        //Given
        Person person1 = new Person();
        person1.setAge(111);
        person1.setTitle("reader1");
        person1.setFullName("name1");
        person1.setPreferredGenre("genre1");
        Person savedPerson = userRepository.saveAndFlush(person1);

        //When
        userRepository.delete(savedPerson);
        userRepository.flush();

        //Then
        assertSelectCount(1);
        assertInsertCount(1);
        assertUpdateCount(0);
        assertDeleteCount(1);
    }

    @DisplayName("Сохранить пользователя с пустыми полями. Должно завершиться ошибкой")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void insertEmptyPerson_assertException() {
        Person person = new Person();

        assertThatThrownBy(() -> userRepository.save(person))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
