package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonToSqlParser {

    private static StringBuilder sqlQueryBuilder = new StringBuilder();

    public static void main(String[] args) throws IOException {
        String jsonString = new String(Files.readAllBytes(Path.of("sql.JSON")));

        String sqlQuery = parseJsonToSql(jsonString);
        System.out.println(sqlQuery);
    }

    public static String parseJsonToSql(String jsonString) {
        JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);
        
        String tableName = jsonObject.get("table_name").getAsString();
        sqlQueryBuilder.append("SELECT ");
        parseColumns(jsonObject.get("columns").getAsJsonArray());
        sqlQueryBuilder.append(" FROM ").append(tableName);
        sqlQueryBuilder.append(" WHERE ");
        parseConditions(jsonObject.get("where_conditions").getAsJsonObject());

        return sqlQueryBuilder.toString();
    }

    private static void parseColumns(JsonArray columnsArray) {
        List<String> columns = new ArrayList<>();
        for (JsonElement column : columnsArray) {
            JsonObject columnObj = column.getAsJsonObject();
            String columnName = columnObj.get("column").getAsString();
            String alias = columnObj.get("alias").getAsString();
            columns.add(columnName + " AS " + alias);
        }
        sqlQueryBuilder.append(String.join(", ", columns));
    }

    private static void parseConditions(JsonObject conditionsObject) {
        String logicOperator = conditionsObject.get("logic_operator").getAsString();
        JsonArray conditionsArray = conditionsObject.get("conditions").getAsJsonArray();

        List<String> conditions = new ArrayList<>();
        for (JsonElement condition : conditionsArray) {
            String conditionSql = parseCondition(condition.getAsJsonObject());
            conditions.add(conditionSql);
        }
        sqlQueryBuilder.append(String.join(" " + logicOperator.toUpperCase() + " ", conditions));
    }

    private static String parseCondition(JsonObject conditionObject) {
        if (conditionObject.get("conditions") != null && conditionObject.get("conditions").getAsJsonArray().size() > 0) {
            String logicOperator = conditionObject.get("logic_operator").getAsString();
            JsonArray conditionsArray = conditionObject.get("conditions").getAsJsonArray();
            List<String> conditions = new ArrayList<>();
            for (JsonElement condition : conditionsArray) {
                String conditionSql = parseCondition(condition.getAsJsonObject());
                conditions.add(conditionSql);
            }
            return "(" + String.join(" " + logicOperator.toUpperCase() + " ", conditions) + ")";
        } else {
            String column = conditionObject.get("column").getAsString();
            String operator = conditionObject.get("operator").getAsString();
            String value = conditionObject.get("value").getAsString();
            String type = conditionObject.get("type").getAsString();

            StringBuilder conditionSql = new StringBuilder();
            conditionSql.append(column).append(" ").append(operator).append(" ");

            if (type.equals("string")) {
                conditionSql.append("'").append(value).append("'");
            } else {
                conditionSql.append(value);
            }

            return conditionSql.toString();
        }
    }
}