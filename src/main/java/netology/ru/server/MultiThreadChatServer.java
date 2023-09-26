package netology.ru.server;


import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.*;

public class MultiThreadChatServer {
    private static final Logger logger = ServerLogger.getInstance();

    public static CopyOnWriteArrayList<Server> serverList = new CopyOnWriteArrayList<>();
    public static ConcurrentHashMap<Server, User> userMap = new ConcurrentHashMap<>();
    private static int port = 8888;

    public static void main(String[] args) {
        logger.info("Запуск сервера");
        File settings = new File("src/main/resources/server_settings.ini");
        if (settings.exists()) {
            logger.info("Обнаружен файл настроек " + settings.getName());
            try (BufferedReader reader = new BufferedReader(new FileReader(settings))) {
                String line = reader.readLine();
                while (line != null) {
                    if (line.startsWith("port:")) {
                        try {
                            port = Integer.parseInt(line.substring(6));
                        } catch (Exception e) {
                            logger.warning("Порт в настройках не установлен. Используется порт по умолчанию: " + port);
                        }
                    }
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                if (settings.createNewFile()) {
                    FileWriter writer = new FileWriter(settings);
                    writer.write("port: " + port + "\n");
                    writer.close();
                    System.out.println("Файл настроек не найден. Используются порт по умолчанию: " + port);
                    System.out.println("Настройки сохранены в файл настроек \"server_settings.ini\".");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (ServerSocket server = new ServerSocket(port)) {
            while (!server.isClosed()) {
                for (Server serv : serverList) {
                    if (!serv.isAlive()) {
                        logger.info("Обнаружено не активное соединение с клиентом. Удаляем...");
                        serverList.remove(serv);
                        userMap.remove(serv);
                        logger.info("Удаление не активного соединения завершено.");
                    }
                }
                // Блокируется до возникновения нового соединения:
                Socket socket = server.accept();
                try {
                    serverList.add(new Server(socket)); // добавить новое соединение в список
                    logger.info("Подключился новый пользователь.");
                } catch (IOException e) {
                    socket.close();
                }
            }
        } catch (IOException ignored) {
        }
    }
}
