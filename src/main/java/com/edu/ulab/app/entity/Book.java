package com.edu.ulab.app.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public final class Book {
    @EqualsAndHashCode.Include
    private Long id;
    private Long userId;
    private String title;
    private String author;
    private long pageCount;
}
