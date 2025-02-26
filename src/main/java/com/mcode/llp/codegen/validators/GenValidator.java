package com.mcode.llp.codegen.validators;

import com.mcode.llp.codegen.managers.QueryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class GenValidator {
    private final QueryManager queryManager;

    @Autowired
    public GenValidator(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    public boolean isEntityExists(String entityName) {
        return queryManager.isDataExist(entityName);
    }
}