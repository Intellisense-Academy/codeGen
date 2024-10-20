package com.mcode.llp.codeGen.validators;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class GenValidator {

    public boolean isEntityExists(String entityName) {
        String filePath = "C:\\controller\\entityfile\\" + entityName + ".json";
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }
}