package com.edu.ulab.app.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
public final class User {
    @EqualsAndHashCode.Include
    private Long id;
    private String fullName;
    private String title;
    private int age;
    private List<Book> books = new ArrayList<>();
}
