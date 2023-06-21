package ru.netology;

public class Main {
    public static final int PORT = 8080;

    public static void main(String[] args) {
        final var server = new Server(64);

        // добавление хендлеров (обработчиков)
        server.addHandler("GET", "/message", (server::responseError));
        server.addHandler("GET", "/spring.png", ((request, responseStream) -> System.out.println("Привет из GET, spring.png")));
        server.start(PORT);
    }

    public static int getPort() {
        return PORT;
    }
}


