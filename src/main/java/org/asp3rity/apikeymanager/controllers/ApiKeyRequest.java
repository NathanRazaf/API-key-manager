package org.asp3rity.apikeymanager.controllers;

import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 * @param URL The base URL of the API
 * @param path The path to the API key request endpoint
 * @param keyPath The path to the API key in the JSON response
 * @param ownerPath The path to the owner of the API key in the JSON response
 * @param createdAtPath The path to the creation date of the API key in the JSON response
 * @param dateFormat The date format of the creation date of the API key (optional)
 * @param requestBody The JSON body to send in the request
 */
public record ApiKeyRequest(String URL, String path, String keyPath, String ownerPath, String createdAtPath,
                            String dateFormat, JsonNode requestBody) {

    private static final String PLACEHOLDER_PATH = "placeholder";

    // Compact constructor to handle null dateFormat
    public ApiKeyRequest {
        if (dateFormat == null) {
            dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"; // Default ISO 8601 format
        }
    }

    public ApiKeyRequest(String URL, String path, String keyPath, String ownerPath, String createdAtPath,
                         JsonNode requestBody) {
        this(URL, path, keyPath, ownerPath, createdAtPath, null, requestBody);
    }

    // Constructor for raw requests where only URL, path and requestBody are needed
    public ApiKeyRequest(String URL, String path, JsonNode requestBody) {
        this(URL, path, PLACEHOLDER_PATH, PLACEHOLDER_PATH, PLACEHOLDER_PATH, null, requestBody);
    }
}