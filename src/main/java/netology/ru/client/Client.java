package netology.ru.client;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final ClientHistoryLogger logger = ClientHistoryLogger.getInstance();
    private static Socket clientSocket; //сокет для общения
    // мы узнаем что хочет сказать клиент?
    private static BufferedReader inputMessage; // поток чтения из сокета
    private static BufferedWriter outputMessage; // поток записи в сокет
    private static BufferedReader reader;
    private static int port = 8888;
    private static String host = "127.0.0.1";

    public static void main(String[] args) {
        File settings = new File("src/main/resources/client_settings.ini");
        if (settings.exists()) {
            System.out.println("Найден файл настроек client_settings.ini");
            try (BufferedReader reader = new BufferedReader(new FileReader(settings))) {
                String line = reader.readLine();
                while (line != null) {
                    if (line.startsWith("server:")) {
                        host = line.substring(8);
                    } else if (line.startsWith("port:")) {
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
            try {
                if (settings.createNewFile()) {
                    FileWriter writer = new FileWriter(settings);
                    writer.write("server: " + host + "\n");
                    writer.write("port: " + port);
                    writer.close();
                    System.out.println("Файл настроек не найден. Используются сервер и порт по умолчанию: " + host + ":" + port);
                    System.out.println("Настройки сохранены в файл настроек \"client_settings.ini\".");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
//        String name = "";
        try {
            clientSocket = new Socket(host, port); // этой строкой мы запрашиваем
            //  у сервера доступ на соединение
            inputMessage = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputMessage = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(System.in));
            Thread inputMessages = new Thread(() -> {
                while (!clientSocket.isClosed()) {
                    try {
                        if (inputMessage.ready()){
                            String msg = inputMessage.readLine();
                            logger.info(msg);
                            System.out.println(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    inputMessage.close();
                    System.out.println("Поток входящих сообщений был закрыт");
                    clientSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Thread.currentThread().interrupt();
                System.out.println("Работа клиента была завершена.");
            });
            Thread searchName = new Thread(() -> {
                String inputName = "";
                while (inputName.equals("")) {
                    try {
                        String msg = inputMessage.readLine();
                        String userAdded = "Добро пожаловать в чат, ";
                        if (msg.contains(userAdded)) {
                            inputName = msg.substring(userAdded.length());
                            logger.info("Выбран ник: " + inputName);
                        } else {
                            logger.info(msg);
                        }
                        System.out.println(msg);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                inputMessages.start();
//                logger.info(inputName);
                Thread.currentThread().interrupt();
            });
            Thread outputMessages = new Thread(() -> {
                boolean start = true;
                while (!clientSocket.isClosed()) {
                    try {
                        if (start) {
                            String msg = inputMessage.readLine();
                            logger.info(msg);
                            System.out.println(msg);
//                            inputMsgCount++;
                        }
                        String word = reader.readLine(); // ждём пока клиент что-нибудь
                        // не напишет в консоль
                        if (word.equals("/start")) {
//                            send(word);
                            searchName.start();
                            start = false;
                        } else if (word.equals("/exit")) { // проверяем на условие выхода из программы
                            send(word);
                            reader.close();
                            outputMessage.close();
                            break;
                        }
                        send(word); // отправляем сообщение на сервер
                    } catch (IOException e) {
                        System.out.println("Ошибка при отправке сообщения.");
                    }
                }
                try {
                    outputMessage.close();
                } catch (IOException ignored) {
                }
                Thread.currentThread().interrupt();
                System.out.println("Поток исходящих сообщений был закрыт.");
            });
//            inputMessages.start();
            outputMessages.start();
        } catch (IOException e) {
            System.out.println("Не удалось подключиться к серверу.");
        }
    }

    private static void send(String word) throws IOException {
        try {
            outputMessage.write(word + "\n");
            logger.info("Я: " + word);
            outputMessage.flush();
        } catch (IOException e) {
            System.out.println("Соединение c сервером было прервано.");
            clientSocket.close();
        }
    }
}
