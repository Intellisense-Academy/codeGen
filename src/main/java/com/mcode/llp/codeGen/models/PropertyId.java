package com.mcode.llp.codeGen.models;

import java.io.Serializable;
import java.util.Objects;

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

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
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