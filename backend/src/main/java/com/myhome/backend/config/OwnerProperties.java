package com.myhome.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The single site owner. The password is stored only as an encoded hash
 * ({bcrypt}..., or a raw $2a/$2b/$2y bcrypt string); {noop} is accepted in local/test only.
 */
@ConfigurationProperties(prefix = "owner")
public record OwnerProperties(String username, String passwordHash) {
}
