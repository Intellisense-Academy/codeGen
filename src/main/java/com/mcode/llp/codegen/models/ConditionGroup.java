package com.mcode.llp.codegen.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class ConditionGroup  {
    private String type;
    private List<Condition> conditions;
}
