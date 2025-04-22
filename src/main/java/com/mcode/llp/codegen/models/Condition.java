package com.mcode.llp.codegen.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Condition {
    private String field;
    private String operator; // eq, gt, lt, match, etc.
    private Object value;
}

