package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import storage.MoviesStore;
import model.Movie;


import java.io.IOException;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();

        try {
            // GET /movies (без параметров)
            if (method.equals("GET") && path.equals("/movies") && query == null) {
                handleGetAll(ex);
            }
            // GET /movies?year=YYYY
            else if (method.equals("GET") && path.equals("/movies") && query != null) {
                handleGetByYear(ex, query);
            }
            // GET /movies/{id}
            else if (method.equals("GET") && path.startsWith("/movies/")) {
                handleGetById(ex, path);
            }
            // POST /movies
            else if (method.equals("POST") && path.equals("/movies")) {
                handlePost(ex);
            }
            // DELETE /movies/{id}
            else if (method.equals("DELETE") && path.startsWith("/movies/")) {
                handleDelete(ex, path);
            }
            // Неподдерживаемый метод
            else {
                ex.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(ex, 500, "Внутренняя ошибка сервера");
        }
    }

    //возвращает все фильмы
    private void handleGetAll(HttpExchange ex) throws IOException {
        List<Movie> movies = store.findAll();
        String json = movies.stream()
                .map(Movie::toJson)
                .collect(Collectors.joining(",", "[", "]"));
        sendJson(ex, 200, json);
    }

    // фильтрация по году
    private void handleGetByYear(HttpExchange ex, String query) throws IOException {
        String[] params = query.split("=");

        if (params.length != 2 || !params[0].equals("year")) {
            sendError(ex, 400, "Некорректный параметр запроса — 'year'");
            return;
        }

        try {
            int year = Integer.parseInt(params[1]);
            List<Movie> movies = store.findByYear(year);
            String json = movies.stream()
                    .map(Movie::toJson)
                    .collect(Collectors.joining(",", "[", "]"));
            sendJson(ex, 200, json);
        } catch (NumberFormatException e) {
            sendError(ex, 400, "Некорректный параметр запроса — 'year'");
        }
    }

    // получение фильма по ID
    private void handleGetById(HttpExchange ex, String path) throws IOException {
        String idStr = path.substring("/movies/".length());

        try {
            long id = Long.parseLong(idStr);
            Optional<Movie> movie = store.findById(id);

            if (movie.isPresent()) {
                sendJson(ex, 200, movie.get().toJson());
            } else {
                sendError(ex, 404, "Фильм не найден");
            }
        } catch (NumberFormatException e) {
            sendError(ex, 400, "Некорректный ID");
        }
    }

    // добавление нового фильма
    private void handlePost(HttpExchange ex) throws IOException {
        // Проверка Content-Type
        String contentType = ex.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.startsWith("application/json")) {
            ex.sendResponseHeaders(415, -1);
            return;
        }

        // Чтение и парсинг тела запроса
        String body = readBody(ex);
        Map<String, Object> data;

        try {
            data = parseJson(body);
        } catch (Exception e) {
            sendError(ex, 422, "Ошибка валидации",
                    List.of("Некорректный JSON"));
            return;
        }

        // Валидация данных
        List<String> errors = new ArrayList<>();
        String title = (String) data.get("title");
        Object yearObj = data.get("year");

        // Валидация title
        if (title == null || title.trim().isEmpty()) {
            errors.add("название не должно быть пустым");
        } else if (title.length() > 100) {
            errors.add("название не должно превышать 100 символов");
        }

        // Валидация year
        Integer year = null;
        if (yearObj == null) {
            errors.add("год обязателен");
        } else {
            try {
                year = ((Number) yearObj).intValue();
                int currentYear = Year.now().getValue();
                if (year < 1888 || year > currentYear + 1) {
                    errors.add("год должен быть между 1888 и " + (currentYear + 1));
                }
            } catch (Exception e) {
                errors.add("год должен быть числом");
            }
        }

        // Если есть ошибки валидации
        if (!errors.isEmpty()) {
            sendError(ex, 422, "Ошибка валидации", errors);
            return;
        }

        // Создание фильма
        Movie movie = store.add(title.trim(), year);
        sendJson(ex, 201, movie.toJson());
    }

    // Удаление фильма
    private void handleDelete(HttpExchange ex, String path) throws IOException {
        String idStr = path.substring("/movies/".length());

        try {
            long id = Long.parseLong(idStr);

            if (store.delete(id)) {
                sendNoContent(ex);
            } else {
                sendError(ex, 404, "Фильм не найден");
            }
        } catch (NumberFormatException e) {
            sendError(ex, 400, "Некорректный ID");
        }
    }

    // Парсер JSON
    private Map<String, Object> parseJson(String json) {
        Map<String, Object> result = new HashMap<>();
        json = json.trim();

        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON");
        }

        json = json.substring(1, json.length() - 1);

        // Разделение по запятым с учетом строк в кавычках
        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length != 2) continue;

            String key = kv[0].trim().replace("\"", "");
            String value = kv[1].trim();

            // Парсинг значения
            if (value.startsWith("\"") && value.endsWith("\"")) {
                // Строка
                result.put(key, value.substring(1, value.length() - 1));
            } else {
                // Число
                try {
                    result.put(key, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    result.put(key, value);
                }
            }
        }

        return result;
    }
}
