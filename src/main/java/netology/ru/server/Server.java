package netology.ru.server;

import java.io.*;
import java.net.Socket;

public class Server extends Thread {
    private final Socket socket;
    private final BufferedReader inputMessage;
    private final BufferedWriter outputMessage;
    private final ServerHistoryLogger logger = ServerHistoryLogger.getInstance();

    public Server(Socket socket) throws IOException {
        this.socket = socket;
        inputMessage = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outputMessage = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        logger.debug("Запуск потока обмена сообщениями для нового пользователя.");
        start();
    }

    @Override
    public void run() {
        String start = "Для подключения к чату введите \"/start\"";
        String msg;
        try {
            if (socket.isConnected()) {
                sendMsg(start);
            } else {
                MultiThreadChatServer.serverList.remove(this);
                logger.debug("Соединение с клиентом прервано.");
            }
            while (!MultiThreadChatServer.userMap.containsKey(this)) {
                msg = inputMessage.readLine();
                logger.info("Новый пользователь отправил: " + msg);
                if (msg.equals("/start")) {
                    addUser();
                    String name = MultiThreadChatServer.userMap.get(this).getUserName();
                    sendMsgAll(name, "К нам присоединился " + name);
                    break;
                } else {
                    sendMsg(start);
                }
            }
            String name = MultiThreadChatServer.userMap.get(this).getUserName();
            while (true) {
                msg = inputMessage.readLine();
                logger.info(MultiThreadChatServer.userMap.get(this).getUserName() + ": " + msg);
                socket.setKeepAlive(true);
                if (msg.equals("")) {
                    continue;
                }
                if (msg.equals("/exit")) {
                    logger.debug("Пользователь " + MultiThreadChatServer.userMap.get(this) + " покинул чат.");
                    if (MultiThreadChatServer.userMap.size() != 1) {
                        sendMsgAll(name, "Пользователь " + MultiThreadChatServer.userMap.get(this) + " покинул чат.");
                    }
                    MultiThreadChatServer.userMap.remove(this, MultiThreadChatServer.userMap.get(this));
                    MultiThreadChatServer.serverList.remove(this);
                    logger.debug("Соединение " + this + " удалено из списка соединений.");
                    break;
                } else if (msg.equals("/users")){
                    logger.debug("Пользователь отправил команду \"/users\"");
                    if (MultiThreadChatServer.serverList.size() == 1){
                        sendMsg("В чате нет других пользователей.");
                    } else {
                        listUsers();
                    }
                } else if (msg.equals("/menu")){
                    chatMenu();
                }else if (MultiThreadChatServer.userMap.containsKey(this)) {
                    sendMsgAll(name, msg);
                }
            }
            logger.debug("Закрытие входящего потока сообщений.");
            inputMessage.close();
            logger.debug("Закрытие исходящего потока сообщений.");
            outputMessage.close();
            logger.debug("Закрытие сокета.");
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void listUsers() {
        for (Server server: MultiThreadChatServer.serverList){
            if (server != this){
                sendMsg(MultiThreadChatServer.userMap.get(server).getUserName());
            } else {
                sendMsg(MultiThreadChatServer.userMap.get(server).getUserName() + " (Вы)");
            }
        }
    }

    private void addUser() throws IOException {
        logger.debug("Запуск метода addUser() для добавления пользователя в базу.");
        int count = 0;
        String name;
        while (!MultiThreadChatServer.userMap.containsKey(this)) {
            if (count == 0) {
                sendMsg("Введите желаемый ник");
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
        logger.debug("Пользователь " + name + " добавлен.");
        sendMsg("Добро пожаловать в чат, " + name);
        logger.debug("Пользователю " + name + " отправлено приветственное сообщение.");
        chatMenu();
    }
    private void sendMsgAll(String name, String msg){
        logger.info(name + ": " + msg); // запись сообщения в лог истории сервера
        for (Server server : MultiThreadChatServer.serverList) {
            if (server == this & MultiThreadChatServer.serverList.size() == 1) { // если в чате только один пользователь
                server.sendMsg("В чате только один пользователь: " + name + " (вы)."); // отправляем ему это сообщение
                logger.info("В чате только один пользователь: " + name); // дублируем сообщение в лог
            }
            if (server != this) {
                server.sendMsg(name + ": " + msg); // отсылаем сообщение всем пользователям чата
            }
        }
    }
    private void sendMsg(String msg) {
        try {
            outputMessage.write(msg + "\n");
            outputMessage.flush();
        } catch (IOException ignored) {
        }
    }

    private boolean validateUserName(String name) {
        if (MultiThreadChatServer.userMap.size() != 0) {
            for (User user : MultiThreadChatServer.userMap.values()) {
                if (user.getUserName().equals(name)) {
                    sendMsg("Такой пользователь уже существует. Придумайте другой ник.");
                    logger.info("Пользователь ввел уже существующий в базе ник.");
                    return false;
                }
            }
        }
        if (name.equals("") | name.length() < 3) {
            sendMsg("Ник не должен быть пустым или короче 3-х символов!!!");
            return false;
        }
        return true;
    }

    private void chatMenu() {
        String[] menu = {
                "Меню чата: /menu",
                "Показать список участников: /users",
                "Выйти из чата: /exit",
        };
        for (String s : menu) {
            sendMsg(s);
        }
    }
}
