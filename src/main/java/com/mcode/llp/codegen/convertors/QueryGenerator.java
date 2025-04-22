package com.mcode.llp.codegen.convertors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcode.llp.codegen.models.Condition;
import com.mcode.llp.codegen.models.ConditionGroup;

import java.util.List;

public class QueryGenerator {

    private final ObjectMapper mapper = new ObjectMapper();

    public String buildComplexQuery(List<ConditionGroup> groups, String tenantId) {
        ObjectNode root = mapper.createObjectNode();

        ObjectNode query = root.putObject("query");
        ObjectNode boolNode = query.putObject("bool");
        // Add tenant filter at the top level
        ArrayNode filterArray = boolNode.putArray("filter");

        // Add the tenant filter (using .keyword to ensure exact match if needed)
        ObjectNode tenantFilter = mapper.createObjectNode();
        tenantFilter.putObject("term").put("tenant.keyword", tenantId);
        filterArray.add(tenantFilter);
        ArrayNode shouldArray = boolNode.putArray("should");

        if (!groups.isEmpty()) {
            boolNode.put("minimum_should_match", 1);
        }

        for (ConditionGroup group : groups) {
            ObjectNode groupBool = mapper.createObjectNode();
            ObjectNode groupInnerBool = groupBool.putObject("bool");

            ArrayNode conditionArray = "or".equalsIgnoreCase(group.getType())
                    ? groupInnerBool.putArray("should")
                    : groupInnerBool.putArray("must");

            for (Condition condition : group.getConditions()) {
                ObjectNode clause = mapper.createObjectNode();
                switch (condition.getOperator().toLowerCase()) {
                    case "eq":
                        clause.putObject("term").put(condition.getField(), condition.getValue().toString());
                        break;
                    case "match":
                        clause.putObject("match").put(condition.getField(), condition.getValue().toString());
                        break;
                    case "gt","lt","gte","lte":
                        clause.putObject("range")
                                .putObject(condition.getField())
                                .put(condition.getOperator(), condition.getValue().toString());
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid condition: " + condition.getOperator());
                }
                conditionArray.add(clause);
            }

            shouldArray.add(groupBool);
        }
        return root.toString();
    }
}
