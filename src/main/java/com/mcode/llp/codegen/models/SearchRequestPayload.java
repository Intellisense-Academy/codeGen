package com.mcode.llp.codegen.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class SearchRequestPayload {
    private SearchQuery mainQuery;
    private String relation;
    private String connectedKey;
    private SearchQuery relatedQuery;
    private List<AggregationSpec> aggregations;


}
