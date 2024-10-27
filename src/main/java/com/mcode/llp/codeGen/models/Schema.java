package com.mcode.llp.codeGen.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Schema {
    String title;
    String type;
    Map<String, Schema> properties = new HashMap<>();
    Set<String> required = new HashSet<>();
    Integer minimum;
    Integer maximum;

    public Schema() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Schema> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Schema> properties) {
        this.properties = properties;
    }

    public Set<String> getRequired() {
        return required;
    }

    public void setRequired(Set<String> required) {
        this.required = required;
    }

    public void setMinimum(Integer minimum) {
        this.minimum= minimum;
    }

   public Integer getMinimum() {
        return minimum;
    }

    public void setMaximum(Integer maximum) {
        this.maximum= maximum;
    }

    public Integer getMaximum() {
        return maximum;
    }
}
