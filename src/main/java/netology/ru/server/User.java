package netology.ru.server;

public class User {
    private static String name;
    private static String nick;


    public User(String name, String nick) {
        this.name = name;
        this.nick = nick;
    }

    public User(String name) {
        this.name = name;
    }

    public static String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String toString (){
        return name;
    }

}
