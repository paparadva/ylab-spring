package com.edu.ulab.app.storage.memory;

import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.exception.EntityDoesNotExistException;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class Storage implements UserInMemoryRepository, BookInMemoryRepository {
    private static final String USER_ALREADY_SAVED = "User has already been saved";
    private static final String BOOK_ALREADY_SAVED = "Book has already been saved";
    private static final String USER_NEEDS_ID_MESSAGE = "Operation requires a user id to be provided";
    private static final String BOOK_NEEDS_ID_MESSAGE = "Operation requires a book id to be provided";
    private static final String BOOK_ALREADY_ASSOCIATED = "Book is already associated with a user";

    private final Map<Long, Person> userData = new HashMap<>();
    private final Map<Long, Book> bookData = new HashMap<>();
    private long nextUserId = 1;
    private long nextBookId = 1;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Person save(@NonNull Person person) {
        if (person.getId() != null) {
            throw new IllegalArgumentException(USER_ALREADY_SAVED);
        }

        lock.writeLock().lock();
        try {

            person.setId(nextUserId++);
            userData.put(person.getId(), person);

            person.getBooks().forEach(book -> {
                if (book.getUserId() != null) {
                    throw new IllegalArgumentException(BOOK_ALREADY_ASSOCIATED);
                }
                book.setUserId(person.getId());

                save(book);
            });

            return person;

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Person update(@NonNull Person person) {
        Objects.requireNonNull(person.getId(), USER_NEEDS_ID_MESSAGE);

        lock.writeLock().lock();
        try {

            if (userData.get(person.getId()) == null) {
                throw new EntityDoesNotExistException("User with id " + person.getId() + " does not exist");
            }
            return userData.merge(person.getId(), person, (oldUser, newUser) -> {
                Set<Book> orphanBooks = new HashSet<>(getAllBooksByUserId(oldUser.getId()));
                orphanBooks.removeAll(new HashSet<>(newUser.getBooks()));
                orphanBooks.forEach(book -> bookData.get(book.getId()).setUserId(null));

                newUser.getBooks().forEach(this::saveOrUpdate);
                return newUser;
            });

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Person saveOrUpdate(@NonNull Person person) {
        return person.getId() == null ? save(person) : update(person);
    }

    @Override
    public Optional<Person> getUserById(@NonNull Long id) {
        lock.readLock().lock();
        try {

            Optional<Person> userOpt = Optional.ofNullable(userData.get(id));
            userOpt.ifPresent(user -> user.setBooks(getAllBooksByUserId(id)));
            return userOpt;

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deleteUserById(@NonNull Long id) {
        lock.writeLock().lock();
        try {

            Person deletedPerson = userData.remove(id);
            if (deletedPerson != null) {
                getAllBooksByUserId(deletedPerson.getId())
                        .stream()
                        .map(Book::getId)
                        .forEach(bookData::remove);
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Book save(@NonNull Book book) {
        if (book.getId() != null) {
            throw new IllegalArgumentException(BOOK_ALREADY_SAVED);
        }

        lock.writeLock().lock();
        try {

            book.setId(nextBookId++);
            bookData.put(book.getId(), book);

            return book;

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Book update(@NonNull Book book) {
        Objects.requireNonNull(book.getId(), BOOK_NEEDS_ID_MESSAGE);

        lock.writeLock().lock();
        try {

            if (bookData.get(book.getId()) == null) {
                throw new EntityDoesNotExistException("Book with id " + book.getId() + " does not exist");
            }
            bookData.put(book.getId(), book);
            return book;

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Book saveOrUpdate(@NonNull Book book) {
        return book.getId() == null ? save(book) : update(book);
    }

    @Override
    public Optional<Book> getBookById(@NonNull Long id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(bookData.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Book> getAllBooksByUserId(@NonNull Long id) {
        lock.readLock().lock();
        try {

            return bookData.values()
                    .stream()
                    .filter(book -> book != null && Objects.equals(book.getUserId(), id))
                    .toList();

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deleteBookById(@NonNull Long id) {
        lock.writeLock().lock();
        try {
            bookData.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
