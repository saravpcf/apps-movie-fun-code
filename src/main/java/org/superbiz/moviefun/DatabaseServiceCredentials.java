package org.superbiz.moviefun;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;

public class DatabaseServiceCredentials {

    private final String vcapServices;

    public DatabaseServiceCredentials(String vcapServices) {
        this.vcapServices = vcapServices;
    }

    public String jdbcUrl(String name, String type) {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode root;

        try {
            root = objectMapper.readTree(vcapServices);
        } catch (IOException e) {
            throw new IllegalStateException("No VCAP_SERVICES found", e);
        }

        JsonNode services = root.path(type);

        for (JsonNode service : services) {
            if (Objects.equals(service.get("name").asText(), name)) {
                return service.get("credentials").get("jdbcUrl").asText();
            }
        }

        throw new IllegalStateException("No "+ name + " found in VCAP_SERVICES");
    }
}