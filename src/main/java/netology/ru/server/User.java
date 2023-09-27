package netology.ru.server;
public class User {
    ServerHistoryLogger logger = ServerHistoryLogger.getInstance();
    private final String name;
    public User(String name) {
        logger.debug("Запуск конструктора создания пользователя.");
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
