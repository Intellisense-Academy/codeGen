package com.mcode.llp.codegen.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class Condition {
    private String field;
    private String operator; // eq, gt, lt, match, etc.
    private Object value;
}

