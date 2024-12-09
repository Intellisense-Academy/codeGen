package com.mcode.llp.codeGen.databases;

import com.mcode.llp.codeGen.models.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class GenDAO {
    public List<Map<String, String>> viewFullData;
    private Connection connection;
    private final AppConfig appConfig;

    @Autowired
    public GenDAO(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    private Connection createConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(appConfig.getUrl(), appConfig.getUsername(), appConfig.getPassword());
                System.out.println("Connection created successfully.");
            } catch (SQLException e) {
                System.out.println("Connection failure.");
                e.printStackTrace();
            }
        }
        return connection;
    }

    public void createTable(String createTableSQL) {
        try (Statement statement = createConnection().createStatement()) {
            statement.execute(createTableSQL);
            System.out.println("Table created successfully.");
        } catch (SQLException e) {
            System.out.println("Error creating table.");
            e.printStackTrace();
        }
    }

    public void insertTable(String insertTableSQL) {
        try (PreparedStatement preparedStatement = createConnection().prepareStatement(insertTableSQL)) {
            preparedStatement.executeUpdate();
            System.out.println("Data inserted into the table successfully.");
        } catch (SQLException e) {
            System.out.println("Error inserting into table.");
            e.printStackTrace();
        }
    }

    public void deleteTable(String deleteTableSQL) {
        try (PreparedStatement preparedStatement = createConnection().prepareStatement(deleteTableSQL)) {
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data deleted from the table successfully.");
            } else {
                System.out.println("No data found to delete.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting from table.");
            e.printStackTrace();
        }
    }

    public Map<String, Object> viewDataById(String viewID) {
        Map<String, Object> result = new HashMap<>();
        try (PreparedStatement preparedStatement = createConnection().prepareStatement(viewID)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object columnValue = resultSet.getObject(i);
                        result.put(columnName, columnValue);
                    }
                } else {
                    System.out.println("No data found with the given query.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error viewing data.");
            e.printStackTrace();
        }
        return result;
    }

    public List<Map<String, Object>> viewAllData(String viewData) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try (PreparedStatement preparedStatement = createConnection().prepareStatement(viewData)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object columnValue = resultSet.getObject(i);
                        row.put(columnName, columnValue);
                    }
                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error viewing data.");
            e.printStackTrace();
        }
        return resultList;
    }

    public void updateTable(String updateSQL)  {
        try(PreparedStatement preparedStatement = createConnection().prepareStatement(updateSQL)){
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data Updated from the table successfully.");
            } else {
                System.out.println("No data found to upadate.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating from table.");
            e.printStackTrace();
        }
        }

    public boolean isDataExist(String entityQuery) {
        try (PreparedStatement preparedStatement = createConnection().prepareStatement(entityQuery)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.out.println("Error viewing entities.");
            e.printStackTrace();
        }
        return false;
    }
}
