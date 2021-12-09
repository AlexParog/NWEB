package ru.netology;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerConnection implements Runnable {

    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private final Server server;

    public ServerConnection(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        var socket = server.getServerSocket();
        threadPool.execute((Runnable) socket); // каждое подключение выполнять в отдельном потоке из пула
    }
}
