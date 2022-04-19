package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ServerConnection implements Runnable {
    BufferedReader in;
    BufferedOutputStream out;

    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public ServerConnection(ServerSocket serverSocket) {
        try {
            var socket = serverSocket.accept();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        try {
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                return;
            }

            final var path = parts[1];
            if (isNotFound(path)) return;

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            isOk(filePath, mimeType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void isOk(Path filePath, String mimeType) throws IOException {
        final var length = Files.size(filePath);
        out.write(("HTTP/1.1 200 OK\r\n"
                + "Content-Type: " + mimeType + "\r\n"
                + "Content-Length: " + length + "\r\n"
                + "Connection: close\r\n" + "\r\n")
                .getBytes());
        Files.copy(filePath, out);
        out.flush();
    }

    private boolean isNotFound(String path) throws IOException {
        if (!validPaths.contains(path)) {
            out.write(("HTTP/1.1 404 Not Found\r\n"
                    + "Content-Length: 0\r\n"
                    + "Connection: close\r\n"
                    + "\r\n").getBytes());
            out.flush();
            return true;
        }
        return false;
    }
}
