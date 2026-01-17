package model;

import java.util.List;

/**
 * Модель ответа с ошибкой
 * Отвечает только за хранение данных об ошибке
 */
public class ErrorResponse {
    private final String error;
    private final List<String> details;

    /**
     * Конструктор для ошибки без деталей
     */
    public ErrorResponse(String error) {
        this.error = error;
        this.details = null;
    }

    /**
     * Конструктор для ошибки с деталями
     */
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
}