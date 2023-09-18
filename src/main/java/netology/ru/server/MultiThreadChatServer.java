package netology.ru.server;

import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class MultiThreadChatServer {
    public static LinkedList<Server> serverList = new LinkedList<>();
    public static ConcurrentHashMap<Server, User> userMap = new ConcurrentHashMap<>();
    private static int port = 8888;

    public static void main(String[] args) {
        File settings = new File("src/main/java/netology/ru/server/settings.ini");
        if (settings.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(settings))){
                String line = reader.readLine();
                while (line != null) {
                    if (line.startsWith("port:")) {
                        try {
                            port = Integer.parseInt(line.substring(6));
                        } catch (Exception e) {
                            System.out.println("Порт в настройках не установлен. Используется порт по умолчанию: " + port);
                        }
                    }
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Файл настроек не найден. Используется порт по умолчанию: " + port);
        }

        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                // Блокируется до возникновения нового соединения:
                Socket socket = server.accept();
                try {
                    serverList.add(new Server(socket)); // добавить новое соединение в список
                } catch (IOException e) {
                    // Если завершится неудачей, закрывается сокет,
                    // в противном случае, нить закроет его при завершении работы:
                    socket.close();
                }
            }
        } catch (IOException ignored){
        }
}
}
