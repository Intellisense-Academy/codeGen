package com.mcode.llp.codegen.models;

import lombok.Data;

import java.io.Serializable;
@Data
public class PropertyId implements Serializable {
    private String name;
    private String entity;

    // Default constructor
    public PropertyId() {
    }

    // Parameterized constructor
    public PropertyId(String name, String entity) {
        this.name = name;
        this.entity = entity;
    }
}