package com.mcode.llp.codegen.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchQuery {
    private String indexName;
    private List<String> fieldsToReturn;
    private List<ConditionGroup> conditionGroups;
}
