package netology.ru.server;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class Server extends Thread {
    private final Socket socket;
    private final BufferedReader inputMessage;
    private final BufferedWriter outputMessage;
    private final Logger logger = ServerLogger.getInstance();

    public Server(Socket socket) throws IOException {
        this.socket = socket;
        inputMessage = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outputMessage = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        logger.info("Запуск потока обмена сообщениями для нового пользователя.");
        start();
    }

    @Override
    public void run() {
        String start = "Для подключения к чату введите \"/start\"";
        String word;
        try {
            if (socket.isConnected()) {
                send(start);
//                chatMenu();
            } else {
                MultiThreadChatServer.serverList.remove(this);
                logger.warning("Соединение с клиентом прервано.");
            }
            while (!MultiThreadChatServer.userMap.containsKey(this)) {
                word = inputMessage.readLine();
                if (word.equals("/start")) {
                    addUser();
                    break;
                } else {
                    send(start);
                }
            }
            while (true) {

                word = inputMessage.readLine();
                socket.setKeepAlive(true);
                if (word.equals("") | word == null) {
                    continue;
                }
                if (word.equals("/exit")) {
                    logger.info("Пользователь " + MultiThreadChatServer.userMap.get(this) + " покинул чат.");
                    send("Пользователь " + MultiThreadChatServer.userMap.get(this) + " покинул чат.");
                    MultiThreadChatServer.userMap.remove(this, MultiThreadChatServer.userMap.get(this));
                    MultiThreadChatServer.serverList.remove(this);
                    logger.info("Соединение " + this + " удалено из списка соединений.");
                    break;
                } else if (MultiThreadChatServer.userMap.containsKey(this)) {
                    for (Server server : MultiThreadChatServer.serverList) {
                        if (server == this & MultiThreadChatServer.serverList.size() == 1) {
                            server.send("В чате только один пользователь: " + MultiThreadChatServer.userMap.get(this).getUserName());
                        }
                        if (server != this) {
                            server.send(MultiThreadChatServer.userMap.get(this) + ": " + word); // отослать принятое в чат всем пользователям
                            logger.info("Пользователь " + MultiThreadChatServer.userMap.get(this) + " отправил в чат сообщение: " + word);
                        }
                    }
                }
            }
            logger.info("Закрытие входящего потока сообщений.");
            inputMessage.close();
            logger.info("Закрытие исходящего потока сообщений.");
            outputMessage.close();
            logger.info("Закрытие сокета.");
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void addUser() throws IOException {
        logger.info("Запуск метода addUser() для добавления пользователя в базу.");
        int count = 0;
        String name;
        while (!MultiThreadChatServer.userMap.containsKey(this)) {
            if (count == 0) {
                send("Введите желаемый ник");
                count++;
            } else if (count == 1) {
                name = inputMessage.readLine();
                if (validateUserName(name)) {
                    createUser(name);
                    break;
                } else {
                    count = 0;
                }
            }
        }
    }

    private void createUser(String name) {
        MultiThreadChatServer.userMap.putIfAbsent(this, new User(name));
        logger.info("Пользователь " + name + " добавлен.");
        send("Добро пожаловать в чат, " + name);
        logger.info("Пользователю " + name + " отправлено приветственное сообщение.");
    }

    private void send(String word) {
        try {
            outputMessage.write(word + "\n");
            logger.info(word);
            outputMessage.flush();
        } catch (IOException ignored) {
        }
    }

    private boolean validateUserName(String name) {
        if (MultiThreadChatServer.userMap.size() != 0) {
            for (User user : MultiThreadChatServer.userMap.values()) {
                if (user.getUserName().equals(name)) {
                    send("Такой пользователь уже существует. Придумайте другой ник.");
                    logger.info("Пользователь ввел уже существующий в базе ник.");
                    return false;
                }
            }
        }
        if (name.equals("") | name.length() < 3) {
            send("Ник не должен быть пустым или короче 4-х символов!!!");
            return false;
        }
        return true;
    }
//    private void chatMenu(){
//        String[] menu = {
//        "Подключиться к серверу: /start",
//        "Завершить работу с сервером: /exit",
//        "Показать список участников: /users",
//        };
//        for (int i = 0;i < menu.length; i++){
//            send(menu[i]);
//        }
//    }
}
