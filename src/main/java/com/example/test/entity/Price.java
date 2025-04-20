package com.example.test.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chainName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_no", referencedColumnName = "materialNo")
    private Product product;

    private BigDecimal regularPricePerUnit;
}
