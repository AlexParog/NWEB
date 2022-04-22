package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static final int PORT = 9999;
    public static final int N_THREADS = 64;

    public static void start() {
        ExecutorService threadPool = Executors.newFixedThreadPool(N_THREADS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                ServerConnection client = new ServerConnection(serverSocket);
                threadPool.submit(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
