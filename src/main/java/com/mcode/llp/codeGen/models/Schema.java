package com.mcode.llp.codeGen.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class Schema extends CommanProperty {
    String title;
    String type;
    Map<String, Schema> properties = new HashMap<>();
    Set<String> required = new HashSet<>();


    public Schema() {
    }

}
