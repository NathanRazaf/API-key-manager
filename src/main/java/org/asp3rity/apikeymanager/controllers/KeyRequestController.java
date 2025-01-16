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

import java.util.Date;

import static org.asp3rity.apikeymanager.helpers.JsonPropParser.getDateFromPath;
import static org.asp3rity.apikeymanager.helpers.JsonPropParser.getStringFromPath;

@RestController

@RequestMapping("/api/request")
public class KeyRequestController {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(KeyRequestController.class);

    // POST body-response template
    @PostMapping("/new")
    public ResponseEntity<ApiKey> requestApiKey(@RequestBody ApiKeyRequest request) {
        try {
            logger.info("Received request: {}", request);  // Log the incoming request
            // Make the request to the API
            // Setup the request
            ResponseEntity<JsonNode> response = setupApiKeyRequest(request);

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

    @PostMapping("/new/raw")
    public ResponseEntity<JsonNode> requestApiKeyRaw(@RequestBody ApiKeyRequest request) {
        try {
            logger.info("Received raw request: {}", request);

            ResponseEntity<JsonNode> response = setupApiKeyRequest(request);

            logger.info("Received raw response: {}", response.getBody());

            // Return the raw response
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());
        } catch (Exception e) {
            logger.error("Error in raw request: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    private ResponseEntity<JsonNode> setupApiKeyRequest(@RequestBody ApiKeyRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<JsonNode> requestEntity = new HttpEntity<>(request.requestBody(), headers);

        logger.info("Sending request to: {}", request.URL() + request.path());
        logger.info("With body: {}", request.requestBody());

        return restTemplate.exchange(
                request.URL() + request.path(),
                HttpMethod.POST,
                requestEntity,
                JsonNode.class
        );
    }
}


