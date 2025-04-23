package com.mcode.llp.codegen.convertors;

import com.mcode.llp.codegen.models.Condition;
import com.mcode.llp.codegen.models.ConditionGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class QueryGeneratorTest {

    private QueryGenerator queryGenerator;

    @BeforeEach
    void setUp() {
        queryGenerator = new QueryGenerator();
    }

    @Test
    void testBuildComplexQueryWithEqOperator() {
        Condition condition = new Condition("field1", "eq", "value1");
        ConditionGroup group = new ConditionGroup("or", List.of(condition));
        String tenantId = "tenant1";

        String query = queryGenerator.buildComplexQuery(List.of(group), tenantId);
        assertNotNull(query);
        assertTrue(query.contains("\"term\":{\"field1\":\"value1\"}"));
    }

    @Test
    void testBuildComplexQueryWithMatchOperator() {
        Condition condition = new Condition("field1", "match", "value1");
        ConditionGroup group = new ConditionGroup("or", List.of(condition));
        String tenantId = "tenant1";

        String query = queryGenerator.buildComplexQuery(List.of(group), tenantId);
        assertNotNull(query);
        assertTrue(query.contains("\"match\":{\"field1\":\"value1\"}"));
    }



    @ParameterizedTest
    @CsvSource({
            "'gt', 10, '\"range\":{\"field1\":{\"gt\":\"10\"}}'",
            "'lt', 10, '\"range\":{\"field1\":{\"lt\":\"10\"}}'",
            "'gte', 10, '\"range\":{\"field1\":{\"gte\":\"10\"}}'",
            "'lte', 10, '\"range\":{\"field1\":{\"lte\":\"10\"}}'",
            "'invalidOp', 10, 'EXCEPTION'"
    })
    void testBuildComplexQueryWithRangeOperators(String operator, int value, String expectedQuery) {
        Condition condition = new Condition("field1", operator, value);
        ConditionGroup group = new ConditionGroup("or", List.of(condition));
        String tenantId = "tenant1";

        if ("EXCEPTION".equals(expectedQuery)) {
            Executable executable = () -> queryGenerator.buildComplexQuery(List.of(group), tenantId);
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
            assertEquals("Invalid condition: " + operator, exception.getMessage());
        } else {
            String query = queryGenerator.buildComplexQuery(List.of(group), tenantId);
            assertNotNull(query);
            assertTrue(query.contains(expectedQuery));
        }
    }

    @Test
    void testBuildComplexQueryWithMultipleConditionsInGroup() {
        Condition condition1 = new Condition("field1", "eq", "value1");
        Condition condition2 = new Condition("field2", "match", "value2");
        ConditionGroup group = new ConditionGroup("or", List.of(condition1, condition2));
        String tenantId = "tenant1";

        String query = queryGenerator.buildComplexQuery(List.of(group), tenantId);
        assertNotNull(query);
        assertTrue(query.contains("\"term\":{\"field1\":\"value1\"}"));
        assertTrue(query.contains("\"match\":{\"field2\":\"value2\"}"));
    }

    @Test
    void testBuildComplexQueryWithMultipleGroups() {
        Condition condition1 = new Condition("field1", "eq", "value1");
        ConditionGroup group1 = new ConditionGroup("or", List.of(condition1));

        Condition condition2 = new Condition("field2", "match", "value2");
        ConditionGroup group2 = new ConditionGroup("and", List.of(condition2));

        String tenantId = "tenant1";

        String query = queryGenerator.buildComplexQuery(List.of(group1, group2), tenantId);
        assertNotNull(query);
        assertTrue(query.contains("\"term\":{\"field1\":\"value1\"}"));
        assertTrue(query.contains("\"match\":{\"field2\":\"value2\"}"));
    }

    @Test
    void testBuildComplexQueryWithEmptyGroup() {
        ConditionGroup emptyGroup = new ConditionGroup("or", List.of());
        String tenantId = "tenant1";

        String query = queryGenerator.buildComplexQuery(List.of(emptyGroup), tenantId);
        assertNotNull(query);
        assertTrue(query.contains("\"minimum_should_match\":1"));
    }

    @Test
    void testBuildComplexQueryWithEmptyGroups() {
        String tenantId = "tenant1";
        String query = queryGenerator.buildComplexQuery(List.of(), tenantId);
        assertNotNull(query);
        assertTrue(query.contains("\"filter\":[{\"term\":{\"tenant.keyword\":\"tenant1\"}}]"));
    }

}
