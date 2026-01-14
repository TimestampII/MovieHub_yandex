package model;

import java.util.List;

public class ErrorResponse {
    private final String error;
    private final List<String> details;

    // Конструктор для ошибки без деталей
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

    //Преобразует объект ошибки в JSON-строку
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"error\":\"");
        sb.append(escapeJson(error));
        sb.append("\"");

        if (details != null && !details.isEmpty()) {
            sb.append(",\"details\":[");
            for (int i = 0; i < details.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append("\"");
                sb.append(escapeJson(details.get(i)));
                sb.append("\"");
            }
            sb.append("]");
        }

        sb.append("}");
        return sb.toString();
    }

    //Экранирует специальные символы для JSON
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
