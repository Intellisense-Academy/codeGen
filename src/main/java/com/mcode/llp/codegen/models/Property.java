package com.mcode.llp.codegen.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@Data
@IdClass(PropertyId.class)
public class Property {
    @Id
    private String name;
    @Id
    private String entity;
    @Column(nullable = false)
    private String type;
    private boolean required;
    private Integer minimum;
    private Integer maximum;
}
