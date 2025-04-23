package com.mcode.llp.codegen.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class SearchQuery {
    private String indexName;
    private List<String> fieldsToReturn;
    private List<ConditionGroup> conditionGroups;
}
