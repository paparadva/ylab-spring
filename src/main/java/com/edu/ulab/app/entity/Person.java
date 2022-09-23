package com.edu.ulab.app.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public final class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    private String fullName;
    private String title;
    private int age;
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Book> books = new ArrayList<>();
}
