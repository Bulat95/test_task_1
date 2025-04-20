package com.example.test.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer materialNo;

    private String materialDescRus;

    private Integer l3ProductCategoryCode;

    private String l3ProductCategoryName;
}