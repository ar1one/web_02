package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ExecutorService threadPoll;
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private ConcurrentHashMap<String, Map<String, Handler>> map;

    public Server(int threads) {
        this.threadPoll = Executors.newFixedThreadPool(threads);
        map = new ConcurrentHashMap<>();
    }

    public void start(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            do {
                final var socket = serverSocket.accept();
                threadPoll.submit(() -> connection(socket));
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connection(Socket socket) {

        try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var out = new BufferedOutputStream(socket.getOutputStream())) {

            final var requestLine = in.readLine();

            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                return;
            }
            final var path = parts[1];
            final var URL = new URI("http://netology.homework:" + Main.getPort() + path);

            //собрали объект запроса
            var request = new Request(parts[0], parts[1], URL);
            var method = request.getRequestMethod();
            var requestPath = request.getPath();

            System.out.println(request.getQueryParams());
            System.out.println(request.getQueryParam("a"));


            if (map.containsKey(method)) {
                if (map.get(method).containsKey(requestPath)) {
                    map.get(method).get(requestPath).handle(request, out);
                }
            }

            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

            final var filePath = Path.of(".", "public", path);


            final var mimeType = Files.probeContentType(filePath);

            // special case for classic
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();

            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (map.containsKey(method)) {
            map.get(method).put(path, handler);
        } else {
            map.put(method, new HashMap<>());
            map.get(method).put(path, handler);
        }
    }

    public void responseError(Request request, BufferedOutputStream out) {
        System.out.println("Error 404");
    }


}
