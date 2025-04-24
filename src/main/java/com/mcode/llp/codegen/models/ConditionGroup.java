package com.mcode.llp.codegen.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConditionGroup  {
    private String type;
    private List<Condition> conditions;
}
