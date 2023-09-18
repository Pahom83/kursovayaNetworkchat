package netology.ru.client;

import java.io.*;
import java.net.Socket;

public class Client {

    private static Socket clientSocket; //сокет для общения
    // мы узнаем что хочет сказать клиент?
    private static BufferedReader inputMessage; // поток чтения из сокета
    private static BufferedWriter outputMessage; // поток записи в сокет

    public static void main(String[] args) {
        try {
            clientSocket = new Socket("127.0.0.1", 8888); // этой строкой мы запрашиваем
            //  у сервера доступ на соединение
            inputMessage = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputMessage = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            Thread inputMessages = new Thread(() -> {
                while (clientSocket.isConnected()) {
                    try {
//                        String serverWord = inputMessage.readLine();
                        System.out.println(inputMessage.readLine());
                    } catch (IOException e) {
                        try {
                            clientSocket.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
//                        throw new RuntimeException(e);
                    }
                }
            });
            Thread outputMessages = new Thread(() -> {
                while (clientSocket.isConnected()) {
                    try {
                        String word = reader.readLine(); // ждём пока клиент что-нибудь
                        // не напишет в консоль
                        send(word); // отправляем сообщение на сервер
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            inputMessages.start();
            outputMessages.start();
        } catch (IOException ignored) {
        }
    }

    private static void send(String word) {
        try {
            outputMessage.write(word + "\n");
            outputMessage.flush();
        } catch (IOException ignored) {
        }
    }
}
