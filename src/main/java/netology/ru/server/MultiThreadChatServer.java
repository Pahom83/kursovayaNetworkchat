package netology.ru.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiThreadChatServer {
    private static final ServerHistoryLogger logger = ServerHistoryLogger.getInstance();

    public static CopyOnWriteArrayList<Server> serverList = new CopyOnWriteArrayList<>();
    public static ConcurrentHashMap<Server, User> userMap = new ConcurrentHashMap<>();
    private static int port = 8888;

    public static void main(String[] args) {
        logger.debug("Запуск сервера");
        File settings = new File("src/main/resources/server_settings.ini");
        if (settings.exists()) {
            logger.debug("Обнаружен файл настроек " + settings.getName());
            try (BufferedReader reader = new BufferedReader(new FileReader(settings))) {
                String line = reader.readLine();
                while (line != null) {
                    if (line.startsWith("port:")) {
                        try {
                            port = Integer.parseInt(line.substring(6));
                            logger.debug("Установлен порт из файла настроек: " + port);
                        } catch (Exception e) {
                            logger.debug("Порт в настройках не установлен. Используется порт по умолчанию: " + port);
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
                    logger.debug("Файл настроек не найден. Используются порт по умолчанию: " + port);
                    logger.debug("Настройки сохранены в файл настроек \"server_settings.ini\".");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (ServerSocket server = new ServerSocket(port)) {
            while (!server.isClosed()) {
                for (Server serv : serverList) {
                    if (!serv.isAlive()) {
                        logger.debug("Обнаружено не активное соединение с клиентом. Удаляем...");
                        serverList.remove(serv);
                        userMap.remove(serv);
                        logger.debug("Удаление не активного соединения завершено.");
                    }
                }
                // Блокируется до возникновения нового соединения:
                Socket socket = server.accept();
                try {
                    logger.debug("Подключился новый пользователь.");
                    serverList.add(new Server(socket)); // добавить новое соединение в список
                } catch (IOException e) {
                    socket.close();
                }
            }
        } catch (IOException ignored) {
        }
    }
}
