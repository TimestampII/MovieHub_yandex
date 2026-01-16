package model;

import com.google.gson.Gson;
import java.util.List;

// Модель ответа с ошибкой
public class ErrorResponse {
    private static final Gson gson = new Gson();

    private final String error;
    private final List<String> details;

    //Конструктор ошибки без деталей
    public ErrorResponse(String error) {
        this.error = error;
        this.details = null;
    }

    // Конструктор для ошибки с деталями
    public ErrorResponse(String error, List<String> details) {
        this.error = error;
        this.details = details;
    }

    public String getError() {
        return error;
    }

    public List<String> getDetails() {
        return details;
    }

    // Преобразует объект ошибки в JSON-строку используя Gson
    public String toJson() {
        return gson.toJson(this);
    }
}