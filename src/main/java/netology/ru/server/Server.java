package netology.ru.server;

import java.io.*;
import java.net.Socket;

public class Server extends Thread {
    private final Socket socket;
    private final BufferedReader inputMessage;
    private final BufferedWriter outputMessage;
    public Server(Socket socket) throws IOException {
        this.socket = socket;
        inputMessage = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outputMessage = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }

    @Override
    public void run() {
        String word;
        try {
            if (this.socket.isConnected()) {
                send("Для подключения к чату введите \"/start\"");
            } else {
                MultiThreadChatServer.serverList.remove(this);
            }
            while (!MultiThreadChatServer.userMap.containsKey(this)) {
                word = inputMessage.readLine();
                if (word.equals("/start")) {
                    createUser();
                } else if (word.equals("/exit")) {
                    MultiThreadChatServer.userMap.remove(this);
                    this.socket.close();
                    break;
                } else {
                    send("Для подключения к чату введите \"/start\"");
                }
            }
            while (this.socket.isConnected()) {
                word = inputMessage.readLine();
                if (MultiThreadChatServer.userMap.containsKey(this)){
                    for (Server server : MultiThreadChatServer.serverList) {
                        if (server == this) {
                            continue;
                        }
                        server.send(MultiThreadChatServer.userMap.get(this) + ": " + word); // отослать принятое сообщение с
                        // привязанного клиента всем остальным
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void createUser() throws IOException {
        int count = 0;
        String name;
        while (true) {
            try {
                if (count == 0) {
                    send("Введите желаемый ник");
                    count++;
                } else if (count == 1) {
                    name = inputMessage.readLine();
                    MultiThreadChatServer.userMap.putIfAbsent(this, new User(name));
//                    MultiThreadChatServer.userMap.put(this, name);
//                    System.out.println(MultiThreadChatServer.userMap.size());
                    send("Добро пожаловать в чат, " + MultiThreadChatServer.userMap.get(this));
                    break;
                }
            } catch (IOException ignored) {
            }
        }
    }

    private void send(String word) {
        try {
            outputMessage.write(word + "\n");
            outputMessage.flush();
        } catch (IOException ignored) {
        }
    }
}
