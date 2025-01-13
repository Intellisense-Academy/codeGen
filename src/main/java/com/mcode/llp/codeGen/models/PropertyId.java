package com.mcode.llp.codeGen.models;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
@Data
public class PropertyId implements Serializable {
    private String name;
    private String entity;

    // Default constructor
    public PropertyId() {
    }

    // Parameterized constructor
    public PropertyId(String name, String type) {
        this.name = name;
        this.entity = entity;
    }


}
//     Override equals and hashCode
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        PropertyId that = (PropertyId) o;
//        return Objects.equals(name, that.name) && Objects.equals(entity, that.entity);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(name, entity);
//    }}
//