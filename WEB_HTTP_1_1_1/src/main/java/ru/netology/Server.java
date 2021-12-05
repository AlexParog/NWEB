package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
            "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);

    @Override
    public void run() {

        try (final var serverSocket = new ServerSocket(9999)) {

            while (true) {

                try (final var socket = serverSocket.accept();
                     final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     threadPool.submit(in); // каждое подключение выполнять в отдельном потоке из пула
                     final var out = new BufferedOutputStream(socket.getOutputStream())) {

                    final var requestLine = in.readLine();
                    final var parts = requestLine.split(" ");

                    if (parts.length != 3) {
                        continue;
                    }

                    final String path = isNotFoundPath(out, parts);
                    if (path == null) continue;

                    final var filePath = Path.of(".", "public", path);
                    final var mimeType = Files.probeContentType(filePath);

                    // special case for classic
                    if (isOkSpecialCase(out, path, filePath, mimeType)) continue;

                    isOk(out, filePath, mimeType);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*public void startServer() {

        try (final var serverSocket = new ServerSocket(9999)) {

            while (true) {

                try (final var socket = serverSocket.accept();
                     final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     final var out = new BufferedOutputStream(socket.getOutputStream())) {

                    final var requestLine = in.readLine();
                    final var parts = requestLine.split(" ");

                    if (parts.length != 3) {
                        continue;
                    }

                    final String path = isNotFoundPath(out, parts);
                    if (path == null) continue;

                    final var filePath = Path.of(".", "public", path);
                    final var mimeType = Files.probeContentType(filePath);

                    // special case for classic
                    if (isOkSpecialCase(out, path, filePath, mimeType)) continue;

                    isOk(out, filePath, mimeType);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    private boolean isOkSpecialCase(BufferedOutputStream out, String path, Path filePath, String mimeType) throws IOException {
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
            return true;
        }
        return false;
    }

    private void isOk(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
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
    }

    private String isNotFoundPath(BufferedOutputStream out, String[] parts) throws IOException {
        final var path = parts[1];
        if (!validPaths.contains(path)) {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
            return null;
        }
        return path;
    }
}