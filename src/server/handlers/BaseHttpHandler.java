package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.ErrorResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Базовый класс для всех HTTP-хендлеров
 * Отвечает за сериализацию и отправку ответов
 */
public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String CT_JSON = "application/json; charset=UTF-8";

    // Gson для сериализации объектов в JSON
    private static final Gson gson = new Gson();

    /**
     * Отправляет JSON-ответ клиенту
     *
     * @param ex HttpExchange
     * @param status HTTP статус код
     * @param json тело ответа в формате JSON
     */
    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Отправляет ответ без тела (204 No Content)
     *
     * @param ex HttpExchange
     */
    protected void sendNoContent(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(204, -1);
    }

    /**
     * Отправляет ошибку без деталей
     * Сериализует ErrorResponse в JSON с помощью Gson
     *
     * @param ex HttpExchange
     * @param status HTTP статус код
     * @param error текст ошибки
     */
    protected void sendError(HttpExchange ex, int status, String error) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(error);
        String json = gson.toJson(errorResponse); // Сериализация здесь!
        sendJson(ex, status, json);
    }

    /**
     * Отправляет ошибку с деталями
     * Сериализует ErrorResponse в JSON с помощью Gson
     *
     * @param ex HttpExchange
     * @param status HTTP статус код
     * @param error текст ошибки
     * @param details список деталей ошибки
     */
    protected void sendError(HttpExchange ex, int status, String error, List<String> details) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(error, details);
        String json = gson.toJson(errorResponse); // Сериализация здесь!
        sendJson(ex, status, json);
    }

    /**
     * Читает тело запроса
     *
     * @param ex HttpExchange
     * @return содержимое тела запроса
     */
    protected String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}