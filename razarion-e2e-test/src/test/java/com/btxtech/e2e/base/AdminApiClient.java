package com.btxtech.e2e.base;

import com.btxtech.e2e.config.WebDriverConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AdminApiClient {

    private static final String ADMIN_EMAIL = "admin@admin.com";
    private static final String ADMIN_PASSWORD = "1234";

    private final String baseUrl;
    private final HttpClient httpClient;
    private String jwtToken;

    public AdminApiClient() {
        this.baseUrl = WebDriverConfig.getBaseUrl();
        this.httpClient = HttpClient.newHttpClient();
    }

    private void authenticate() {
        String credentials = Base64.getEncoder().encodeToString((ADMIN_EMAIL + ":" + ADMIN_PASSWORD).getBytes());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/rest/user/auth"))
                .header("Authorization", "Basic " + credentials)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Auth failed: " + response.statusCode() + " " + response.body());
            }
            jwtToken = response.body().trim();
            System.out.println("[E2E] Admin authenticated successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to authenticate as admin", e);
        }
    }

    public void deleteAllHumanBases() {
        authenticate();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/rest/editor/user-mgmt/get-user-backend-infos"))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Get users failed: " + response.statusCode() + " " + response.body());
            }
            String json = response.body();

            // Extract userIds that have a non-null baseId
            // Simple regex parsing to avoid adding a JSON library dependency
            Pattern userPattern = Pattern.compile("\\{[^}]*\"userId\"\\s*:\\s*\"([^\"]+)\"[^}]*\"baseId\"\\s*:\\s*(\\d+)[^}]*\\}");
            Matcher matcher = userPattern.matcher(json);
            java.util.List<String> userIdsWithBases = new java.util.ArrayList<>();
            while (matcher.find()) {
                userIdsWithBases.add(matcher.group(1));
            }
            // Also try reversed field order
            Pattern userPattern2 = Pattern.compile("\\{[^}]*\"baseId\"\\s*:\\s*(\\d+)[^}]*\"userId\"\\s*:\\s*\"([^\"]+)\"[^}]*\\}");
            Matcher matcher2 = userPattern2.matcher(json);
            while (matcher2.find()) {
                String userId = matcher2.group(2);
                if (!userIdsWithBases.contains(userId)) {
                    userIdsWithBases.add(userId);
                }
            }

            if (userIdsWithBases.isEmpty()) {
                System.out.println("[E2E] No human bases found to delete");
                return;
            }

            System.out.println("[E2E] Deleting bases for " + userIdsWithBases.size() + " users: " + userIdsWithBases);

            String userIdsJson = userIdsWithBases.stream()
                    .map(id -> "\"" + id + "\"")
                    .collect(Collectors.joining(",", "[", "]"));

            HttpRequest deleteRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/rest/editor/user-mgmt/delete-users-bases"))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(userIdsJson))
                    .build();

            HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
            if (deleteResponse.statusCode() != 200) {
                throw new RuntimeException("Delete bases failed: " + deleteResponse.statusCode() + " " + deleteResponse.body());
            }
            System.out.println("[E2E] Successfully deleted all human bases");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete human bases", e);
        }
    }

    public void restartPlanetWarm() {
        authenticate();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/rest/planet-mgmt-controller/restartPlanetWarm"))
                .header("Authorization", "Bearer " + jwtToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Restart planet warm failed: " + response.statusCode() + " " + response.body());
            }
            System.out.println("[E2E] Planet warm restart completed");
            // Wait for planet to initialize
            Thread.sleep(5000);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to restart planet warm", e);
        }
    }
}
