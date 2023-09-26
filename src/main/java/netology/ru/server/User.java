package netology.ru.server;

import java.util.logging.Logger;

public class User {
    Logger logger = ServerLogger.getInstance();
    private final String name;
    public User(String name) {
        logger.fine("Запуск конструктора создания пользователя.");
        this.name = name;
    }

    public String getUserName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String toString (){
        return name;
    }

}
