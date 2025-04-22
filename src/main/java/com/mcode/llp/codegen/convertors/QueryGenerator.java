package com.mcode.llp.codegen.convertors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcode.llp.codegen.models.Condition;
import com.mcode.llp.codegen.models.ConditionGroup;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
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
                        ObjectNode termNode = clause.putObject("term");
                        termNode.put(condition.getField(), condition.getValue().toString());
                        break;
                    case "match":
                        ObjectNode matchNode = clause.putObject("match");
                        matchNode.put(condition.getField(), condition.getValue().toString());
                        break;
                    case "gt","lt","gte","lte":
                        ObjectNode rangeNode = clause.putObject("range");
                        ObjectNode fieldNode = rangeNode.putObject(condition.getField());
                        fieldNode.put(condition.getOperator(), condition.getValue().toString());
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
