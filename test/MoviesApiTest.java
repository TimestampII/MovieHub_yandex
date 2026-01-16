import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import model.Movie;
import server.MoviesServer;

//TDD-тесты для MovieHub API
public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer();
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @AfterAll
    static void afterAll() {
        if (server != null) {
            server.stop();
        }
    }

    @BeforeEach
    void setUp() {
        server.getStore().clear();
    }

    //Вспомогательные методы для HTTP запросов

    //Отправляет GET запрос на указанный путь
    private HttpResponse<String> sendGet(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .GET()
                .build();

        return client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    // Отправляет POST запрос с JSON телом
    private HttpResponse<String> sendPost(String path, String jsonBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    // Отправляет POST запрос с кастомным Content-Type
    private HttpResponse<String> sendPost(String path, String jsonBody, String contentType) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    // Отправляет DELETE запрос
    private HttpResponse<String> sendDelete(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .DELETE()
                .build();

        return client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    // Проверяет, что ответ содержит корректный Content-Type для JSON
    private void assertJsonContentType(HttpResponse<String> response) {
        assertEquals("application/json; charset=UTF-8",
                response.headers().firstValue("Content-Type").orElse(""),
                "Content-Type должен быть application/json; charset=UTF-8");
    }

    //GET /movies

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpResponse<String> resp = sendGet("/movies");

        assertEquals(200, resp.statusCode());
        assertJsonContentType(resp);
        assertEquals("[]", resp.body().trim());
    }

    @Test
    void getMovies_withMovies_returnsArray() throws Exception {
        server.getStore().add("Inception", 2010);
        server.getStore().add("The Matrix", 1999);

        HttpResponse<String> resp = sendGet("/movies");

        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("Inception"));
        assertTrue(resp.body().contains("The Matrix"));
    }

    // POST /movies

    @Test
    void postMovie_withValidData_returns201() throws Exception {
        String json = "{\"title\":\"Interstellar\",\"year\":2014}";

        HttpResponse<String> resp = sendPost("/movies", json);

        assertEquals(201, resp.statusCode());
        assertTrue(resp.body().contains("Interstellar"));
        assertTrue(resp.body().contains("\"id\":"));
    }

    @Test
    void postMovie_withEmptyTitle_returns422() throws Exception {
        String json = "{\"title\":\"\",\"year\":2014}";

        HttpResponse<String> resp = sendPost("/movies", json);

        assertEquals(422, resp.statusCode());
        assertTrue(resp.body().contains("название не должно быть пустым"));
    }

    @Test
    void postMovie_withLongTitle_returns422() throws Exception {
        String longTitle = "a".repeat(101);
        String json = String.format("{\"title\":\"%s\",\"year\":2014}", longTitle);

        HttpResponse<String> resp = sendPost("/movies", json);

        assertEquals(422, resp.statusCode());
        assertTrue(resp.body().contains("название не должно превышать 100 символов"));
    }

    @Test
    void postMovie_withInvalidYear_returns422() throws Exception {
        String json = "{\"title\":\"Old Movie\",\"year\":1800}";

        HttpResponse<String> resp = sendPost("/movies", json);

        assertEquals(422, resp.statusCode());
        assertTrue(resp.body().contains("год должен быть между 1888"));
    }

    @Test
    void postMovie_withWrongContentType_returns415() throws Exception {
        String json = "{\"title\":\"Test\",\"year\":2020}";

        HttpResponse<String> resp = sendPost("/movies", json, "text/plain");

        assertEquals(415, resp.statusCode());
    }

    // GET /movies/{id}

    @Test
    void getMovieById_whenExists_returns200() throws Exception {
        Movie movie = server.getStore().add("Pulp Fiction", 1994);

        HttpResponse<String> resp = sendGet("/movies/" + movie.getId());

        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("Pulp Fiction"));
    }

    @Test
    void getMovieById_whenNotFound_returns404() throws Exception {
        HttpResponse<String> resp = sendGet("/movies/999");

        assertEquals(404, resp.statusCode());
        assertTrue(resp.body().contains("Фильм не найден"));
    }

    @Test
    void getMovieById_withInvalidId_returns400() throws Exception {
        HttpResponse<String> resp = sendGet("/movies/abc");

        assertEquals(400, resp.statusCode());
        assertTrue(resp.body().contains("Некорректный ID"));
    }

    // DELETE /movies/{id}

    @Test
    void deleteMovie_whenExists_returns204() throws Exception {
        Movie movie = server.getStore().add("Fight Club", 1999);

        HttpResponse<String> resp = sendDelete("/movies/" + movie.getId());

        assertEquals(204, resp.statusCode());
        assertFalse(server.getStore().findById(movie.getId()).isPresent());
    }

    @Test
    void deleteMovie_whenNotFound_returns404() throws Exception {
        HttpResponse<String> resp = sendDelete("/movies/999");

        assertEquals(404, resp.statusCode());
    }

    // GET /movies?year=YYYY

    @Test
    void getMoviesByYear_returnsFiltered() throws Exception {
        server.getStore().add("Movie 1999", 1999);
        server.getStore().add("Movie 2000", 2000);
        server.getStore().add("Another 1999", 1999);

        HttpResponse<String> resp = sendGet("/movies?year=1999");

        assertEquals(200, resp.statusCode());
        String body = resp.body();
        assertTrue(body.contains("Movie 1999"));
        assertTrue(body.contains("Another 1999"));
        assertFalse(body.contains("Movie 2000"));
    }

    @Test
    void getMoviesByYear_withInvalidYear_returns400() throws Exception {
        HttpResponse<String> resp = sendGet("/movies?year=abc");

        assertEquals(400, resp.statusCode());
        assertTrue(resp.body().contains("Некорректный параметр запроса"));
    }
}