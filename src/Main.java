import server.MoviesServer;

public class Main {
    public static void main(String[] args) {
        MoviesServer server = new MoviesServer();
        server.start();

        System.out.println("MovieHub API запущен на http://localhost:8080");
        System.out.println("Нажмите Ctrl+C для остановки сервера");

        // Добавляем shutdown hook для корректной остановки
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nОстанавливаем сервер...");
            server.stop();
        }));
    }
}
