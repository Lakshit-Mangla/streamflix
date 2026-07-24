package com.streamflix.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamflix.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Verifies a Google "Sign In With Google" ID token by asking Google's own
 * tokeninfo endpoint whether it's genuine. This avoids pulling in Google's
 * full client SDK for what is, for a project this size, a single HTTP call.
 * Google explicitly documents this endpoint as valid for low-volume,
 * server-side verification (see: Google Identity docs).
 */
@Component
@RequiredArgsConstructor
public class GoogleTokenVerifier {

    @Value("${app.google.client-id:}")
    private String expectedClientId;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public record GoogleUser(String email, String name, boolean emailVerified) {}

    public GoogleUser verify(String idToken) {
        if (expectedClientId == null || expectedClientId.isBlank()) {
            throw new InvalidCredentialsException(
                    "Google Sign-In isn't configured on this server yet. Set app.google.client-id (or the " +
                    "GOOGLE_CLIENT_ID environment variable) to your Google OAuth Client ID.");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new InvalidCredentialsException("Google rejected this sign-in token. Please try again.");
            }

            JsonNode node = objectMapper.readTree(response.body());

            String audience = node.path("aud").asText("");
            if (!expectedClientId.equals(audience)) {
                throw new InvalidCredentialsException(
                        "This sign-in token wasn't issued for this app (client ID mismatch). Check that the " +
                        "Client ID in the frontend matches app.google.client-id on the server.");
            }

            boolean emailVerified = "true".equals(node.path("email_verified").asText());
            String email = node.path("email").asText(null);
            String name = node.path("name").asText(null);

            if (email == null) {
                throw new InvalidCredentialsException("Google didn't return an email address for this account.");
            }

            return new GoogleUser(email, name != null ? name : email, emailVerified);

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidCredentialsException("Couldn't verify this Google sign-in right now. Please try again.");
        }
    }
}
