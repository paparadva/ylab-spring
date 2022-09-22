package com.edu.ulab.app.storage;

import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.entity.User;
import com.edu.ulab.app.exception.EntityDoesNotExistException;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class Storage implements UserRepository, BookRepository {
    private static final String USER_ALREADY_SAVED = "User has already been saved";
    private static final String BOOK_ALREADY_SAVED = "Book has already been saved";
    private static final String USER_NEEDS_ID_MESSAGE = "Operation requires a user id to be provided";
    private static final String BOOK_NEEDS_ID_MESSAGE = "Operation requires a book id to be provided";
    private static final String BOOK_ALREADY_ASSOCIATED = "Book is already associated with a user";

    private final Map<Long, User> userData = new HashMap<>();
    private final Map<Long, Book> bookData = new HashMap<>();
    private long nextUserId = 1;
    private long nextBookId = 1;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public User save(@NonNull User user) {
        if (user.getId() != null) {
            throw new IllegalArgumentException(USER_ALREADY_SAVED);
        }

        lock.writeLock().lock();
        try {

            user.setId(nextUserId++);
            userData.put(user.getId(), user);

            user.getBooks().forEach(book -> {
                if (book.getUserId() != null) {
                    throw new IllegalArgumentException(BOOK_ALREADY_ASSOCIATED);
                }
                book.setUserId(user.getId());

                save(book);
            });

            return user;

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public User update(@NonNull User user) {
        Objects.requireNonNull(user.getId(), USER_NEEDS_ID_MESSAGE);

        lock.writeLock().lock();
        try {

            if (userData.get(user.getId()) == null) {
                throw new EntityDoesNotExistException("User with id " + user.getId() + " does not exist");
            }
            return userData.merge(user.getId(), user, (oldUser, newUser) -> {
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
    public User saveOrUpdate(@NonNull User user) {
        return user.getId() == null ? save(user) : update(user);
    }

    @Override
    public Optional<User> getUserById(@NonNull Long id) {
        lock.readLock().lock();
        try {

            Optional<User> userOpt = Optional.ofNullable(userData.get(id));
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

            User deletedUser = userData.remove(id);
            if (deletedUser != null) {
                getAllBooksByUserId(deletedUser.getId())
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
