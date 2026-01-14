package server.handlers;
import model.ErrorResponse;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String CT_JSON = "application/json; charset=UTF-8";

    //Отправляет JSON-ответ клиенту
    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Отправляет ответ без тела
    protected void sendNoContent(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(204, -1);
    }

    //Отправляет ошибку без деталей
    protected void sendError(HttpExchange ex, int status, String error) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(error);
        sendJson(ex, status, errorResponse.toJson());
    }

    protected void sendError(HttpExchange ex, int status, String error, List<String> details) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(error, details);
        sendJson(ex, status, errorResponse.toJson());
    }

    // Читает тело запроса
    protected String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
