package org.asp3rity.apikeymanager.controllers;

import java.util.Date;


/**
 * @param key The API key
 * @param owner The owner of the API key
 * @param createdAt The creation date of the API key
 */
public record ApiKey(String key, String owner, Date createdAt) {

    @Override
    public String toString() {
        return "ApiKey{" +
                "key='" + key + '\'' +
                ", owner='" + owner + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
