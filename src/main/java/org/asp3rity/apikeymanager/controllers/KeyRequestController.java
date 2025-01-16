package org.asp3rity.apikeymanager.controllers;

import org.asp3rity.apikeymanager.helpers.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController

@RequestMapping("/api/request")
public class KeyRequestController {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(KeyRequestController.class);

    private Result<?> getJsonPropFromPath(JsonNode root, String path) {
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

    private Result<String> getStringFromPath(JsonNode root, String path) {
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

    private Result<Date> getDateFromPath(JsonNode root, String path, String datePattern) {
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

    private Result<Date> getDateFromPath(JsonNode root, String path) {
        return getDateFromPath(root, path, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }
    // POST body-response template
    @PostMapping("/new")
    public ResponseEntity<ApiKey> requestApiKey(@RequestBody ApiKeyRequest request) {
        try {
            logger.info("Received request: {}", request);  // Log the incoming request
            // Make the request to the API
            // Setup the request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create the request entity with the external body
            HttpEntity<JsonNode> requestEntity = new HttpEntity<>(request.requestBody(), headers);

            logger.info("Sending request to: {}", request.URL() + request.path());
            logger.info("With body: {}", request.requestBody());

            // Make the actual HTTP request to the external API
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    request.URL() + request.path(),
                    HttpMethod.POST,
                    requestEntity,
                    JsonNode.class
            );

            logger.info("Received response: {}", response.getBody());

            // Extract the data from the response
            JsonNode responseBody = response.getBody();
            Result<String> keyResult = getStringFromPath(responseBody, request.keyPath());
            Result<String> ownerResult = getStringFromPath(responseBody, request.ownerPath());
            Result<Date> createdAtResult = getDateFromPath(responseBody, request.createdAtPath(), request.dateFormat());

            // Check if the data was extracted successfully
            if (keyResult.value() == null || ownerResult.value() == null ||
                    createdAtResult.value() == null) {
                logger.error("Could not extract data from response");
                if (keyResult.value() == null) {
                    logger.error("Key: {}", keyResult.message());
                }
                if (ownerResult.value() == null) {
                    logger.error("Owner: {}", ownerResult.message());
                }
                if (createdAtResult.value() == null) {
                    logger.error("Created At: {}", createdAtResult.message());
                }
                return ResponseEntity.badRequest().build();
            }

            // Create the API key object
            ApiKey apiKey = new ApiKey(keyResult.value(), ownerResult.value(),
                    createdAtResult.value());

            // Return the API key object
            return ResponseEntity.ok(apiKey);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


