package model;

public class Movie {
    private final long id;
    private final String title;
    private final int year;

    public Movie(long id, String title, int year) {
        this.id = id;
        this.title = title;
        this.year = year;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    // Преобразует объект фильма в JSON-строку
    public String toJson() {
        return String.format(
                "{\"id\":%d,\"title\":\"%s\",\"year\":%d}",
                id,
                escapeJson(title),
                year
        );
    }

    // Экранирует специальные символы для JSON
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

    @Override
    public String toString() {
        return "Movie{id=" + id + ", title='" + title + "', year=" + year + "}";
    }
}

