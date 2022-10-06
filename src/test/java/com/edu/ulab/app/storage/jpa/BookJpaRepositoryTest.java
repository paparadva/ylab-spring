package com.edu.ulab.app.storage.jpa;

import com.edu.ulab.app.config.SystemJpaTest;
import com.edu.ulab.app.entity.Book;
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
 * Тесты репозитория {@link BookJpaRepository}.
 */
@SystemJpaTest
public class BookJpaRepositoryTest {
    @Autowired
    BookJpaRepository bookRepository;
    @Autowired
    UserJpaRepository userRepository;

    @BeforeEach
    void setUp() {
        SQLStatementCountValidator.reset();
    }

    @DisplayName("Сохранить книгу и автора. Число select должно равняться 1")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void saveBook_thenAssertDmlCount() {
        //Given

        Person person = new Person();
        person.setAge(111);
        person.setTitle("reader");
        person.setFullName("Test Test");
        person.setPreferredGenre("fantasy");

        Person savedPerson = userRepository.save(person);

        Book book = new Book();
        book.setAuthor("Test Author");
        book.setTitle("test");
        book.setPageCount(1000);
        book.setUserId(savedPerson.getId());
//        book.setPerson(savedPerson);

        //When
        Book result = bookRepository.save(book);

        //Then
        assertThat(result.getPageCount()).isEqualTo(1000);
        assertThat(result.getTitle()).isEqualTo("test");
        assertSelectCount(2);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Обновить книгу")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void updateBook_thenAssertDmlCount() {
        //Given

        Person person = new Person();
        person.setAge(111);
        person.setTitle("Test reader");
        person.setFullName("Test Test");
        person.setPreferredGenre("fantasy");
        Person savedPerson = userRepository.save(person);

        Book book = new Book();
        book.setAuthor("Test Author");
        book.setTitle("test");
        book.setPageCount(1000);
        book.setUserId(savedPerson.getId());
        Book savedBook = bookRepository.saveAndFlush(book);

        //When
        savedBook.setAuthor("new author");
        savedBook.setTitle("new title");
        savedBook.setPageCount(2000);
        Book result = bookRepository.saveAndFlush(savedBook);

        //Then
        assertThat(savedBook.getAuthor()).isEqualTo("new author");
        assertThat(savedBook.getTitle()).isEqualTo("new title");
        assertThat(savedBook.getPageCount()).isEqualTo(2000);

        assertSelectCount(2);
        assertInsertCount(2);
        assertUpdateCount(1);
        assertDeleteCount(0);
    }

    @DisplayName("Получить книгу по id")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void getBookById_thenAssertDmlCount() {
        //Given

        Person person = new Person();
        person.setAge(111);
        person.setTitle("Test reader");
        person.setFullName("Test Test");
        person.setPreferredGenre("fantasy");
        Person savedPerson = userRepository.save(person);

        Book book = new Book();
        book.setAuthor("Test Author");
        book.setTitle("test");
        book.setPageCount(1000);
        book.setUserId(savedPerson.getId());
        Book savedBook = bookRepository.saveAndFlush(book);
        long bookId = savedBook.getId();

        //When
        Optional<Book> resultOpt = bookRepository.findById(bookId);

        //Then
        assertThat(resultOpt.isPresent()).isTrue();
        assertThat(resultOpt.get().getId()).isEqualTo(bookId);

        assertSelectCount(2);
        assertInsertCount(2);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Получить все книги")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void getAllBooks_thenAssertDmlCount() {
        //Given

        Person person = new Person();
        person.setAge(111);
        person.setTitle("Test reader");
        person.setFullName("Test Test");
        person.setPreferredGenre("fantasy");
        Person savedPerson = userRepository.save(person);

        Book book = new Book();
        book.setAuthor("Test Author");
        book.setTitle("test");
        book.setPageCount(1000);
        book.setUserId(savedPerson.getId());
        bookRepository.save(book);


        book = new Book();
        book.setAuthor("Another Test Author");
        book.setTitle("another test");
        book.setPageCount(1000);
        book.setUserId(savedPerson.getId());
        bookRepository.save(book);
        bookRepository.flush();

        //When
        List<Book> bookList = bookRepository.findAll();

        //Then
        assertThat(bookList.size()).isEqualTo(2);

        assertSelectCount(4);
        assertInsertCount(3);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Удалить книгу")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void deleteBook_thenAssertDmlCount() {
        //Given

        Person person = new Person();
        person.setAge(111);
        person.setTitle("Test reader");
        person.setFullName("Test Test");
        person.setPreferredGenre("fantasy");
        Person savedPerson = userRepository.save(person);

        Book book = new Book();
        book.setAuthor("Test Author");
        book.setTitle("test");
        book.setPageCount(1000);
        book.setUserId(savedPerson.getId());
        Book savedBook = bookRepository.saveAndFlush(book);
        long bookId = savedBook.getId();

        //When
        bookRepository.deleteById(bookId);
        bookRepository.flush();

        //Then
        assertSelectCount(2);
        assertInsertCount(2);
        assertUpdateCount(0);
        assertDeleteCount(1);
    }

    @DisplayName("Сохранить книгу без заполненных полей. Должно завершиться с ошибкой")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void insertEmptyBook_assertException() {
        Book book = new Book();

        assertThatThrownBy(() -> bookRepository.save(book))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
