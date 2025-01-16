package org.asp3rity.apikeymanager.helpers;

import com.fasterxml.jackson.databind.JsonNode;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonPropParser {
    public static Result<?> getJsonPropFromPath(JsonNode root, String path) {
        String[] pathParts = path.split("/");
        JsonNode currNode = root;
        for (String part : pathParts) {
            if (currNode == null) {
                return new Result<>(null, "Path not found");
            }
            currNode = currNode.get(part);
        }
        return new Result<>(currNode, "Success");
    }

    public static Result<String> getStringFromPath(JsonNode root, String path) {
        Result<?> result = getJsonPropFromPath(root, path);
        if (result.value() == null) {
            return new Result<>(null, result.message());
        }

        JsonNode node = (JsonNode) result.value();
        if (node.isTextual()) {
            String value = node.asText();
            return new Result<>(value, "Success");
        }

        return new Result<>(null, "Value is not a string. Found type: " + node.getNodeType());
    }

    public static Result<Date> getDateFromPath(JsonNode root, String path, String datePattern) {
        Result<?> result = getJsonPropFromPath(root, path);
        if (result.value() == null) {
            return new Result<>(null, result.message());
        }
        JsonNode node = (JsonNode) result.value();
        if (node.isTextual()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
                Date date = dateFormat.parse(node.asText());
                return new Result<>(date, "Success");
            } catch (Exception e) {
                return new Result<>(null, "Could not parse date: " + e.getMessage());
            }
        }
        return new Result<>(null, "Value is not a date string");
    }

    public static Result<Date> getDateFromPath(JsonNode root, String path) {
        return getDateFromPath(root, path, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }
}
