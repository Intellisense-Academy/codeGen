package com.mcode.llp.codegen.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class Schema {
    private String title;
    private String description;
    private String type;
    private String format;
    private String pattern;
    private Map<String, Schema> properties = new HashMap<>();
    private Set<String> required = new HashSet<>();
    private Integer minimum;
    private Integer maximum;
    @JsonProperty("enum")
    private List<Object> enumValues;
    @JsonProperty("readOnly")
    private Boolean readOnly;

}
