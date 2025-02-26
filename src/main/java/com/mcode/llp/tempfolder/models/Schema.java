package com.mcode.llp.codegen.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class Schema {
    String title;
    String type;
    Map<String, Schema> properties = new HashMap<>();
    Set<String> required = new HashSet<>();
    Integer minimum;
    Integer maximum;

}
