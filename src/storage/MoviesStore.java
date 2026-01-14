package storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import model.Movie;


public class MoviesStore {
    private final Map<Long, Movie> movies = new ConcurrentHashMap<>();
    private long nextId = 1;

    //Добавляет новый фильм в хранилище
    public Movie add(String title, int year) {
        Movie movie = new Movie(nextId++, title, year);
        movies.put(movie.getId(), movie);
        return movie;
    }

    // Ищет фильм по ID
    public Optional<Movie> findById(long id) {
        return Optional.ofNullable(movies.get(id));
    }

    // Возвращает все фильмы
    public List<Movie> findAll() {
        return new ArrayList<>(movies.values());
    }

    // Возвращает фильмы за определенный год
    public List<Movie> findByYear(int year) {
        return movies.values().stream()
                .filter(movie -> movie.getYear() == year)
                .collect(Collectors.toList());
    }

    // Удаляет фильм по ID
    public boolean delete(long id) {
        return movies.remove(id) != null;
    }

    // Очищает хранилище (используется в тестах)
    public void clear() {
        movies.clear();
        nextId = 1;
    }

    // Возвращает количество фильмов в хранилище
    public int size() {
        return movies.size();
    }
}
