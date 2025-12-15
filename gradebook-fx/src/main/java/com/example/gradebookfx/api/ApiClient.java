package com.example.gradebookfx.api;

import com.example.gradebookfx.api.dto.GradeDto;
import com.example.gradebookfx.config.AppConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class ApiClient {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final String baseUrl;

    public ApiClient() {
        this(AppConfig.apiBaseUrl());
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    public List<GradeDto> listGrades() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri("/api/grades"))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(resp);

        return om.readValue(resp.body(), new TypeReference<>() {});
    }

    public GradeDto createGrade(GradeDto dto) throws IOException, InterruptedException {
        String body = om.writeValueAsString(dto);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri("/api/grades"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(resp);

        return om.readValue(resp.body(), GradeDto.class);
    }

    public GradeDto updateGrade(long id, GradeDto dto) throws IOException, InterruptedException {
        String body = om.writeValueAsString(dto);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri("/api/grades/" + id))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(resp);

        return om.readValue(resp.body(), GradeDto.class);
    }

    public void deleteGrade(long id) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri("/api/grades/" + id))
                .timeout(Duration.ofSeconds(10))
                .DELETE()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(resp);
    }

    private URI uri(String path) {
        return URI.create(baseUrl + path);
    }

    private static void ensure2xx(HttpResponse<String> resp) throws IOException {
        int code = resp.statusCode();
        if (code >= 200 && code < 300) return;

        // если твой сервер отдаёт body с ошибками — покажем его в сообщении
        String msg = "HTTP " + code + ": " + (resp.body() == null ? "" : resp.body());
        throw new IOException(msg);
    }

    private static String trimTrailingSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
