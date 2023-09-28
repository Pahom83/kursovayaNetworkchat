package netology.ru.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerHistoryLogger {
    public static ServerHistoryLogger serverHistoryLogger;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final File history = new File("server_history.log");
    private static final File debug = new File("server_debug.log");

    public static ServerHistoryLogger getInstance() {
        if (serverHistoryLogger == null) {
            serverHistoryLogger = new ServerHistoryLogger();
        }
        return serverHistoryLogger;
    }

    void info(String msg) {
        if (createLogFile(history) | history.exists()) {
            try {
                FileWriter writer = new FileWriter(history, true);
                writer.write(dateFormat.format(new Date()) + ": " + msg + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    void debug(String msg) {
        if (createLogFile(debug) | debug.exists()) {
            try {
                FileWriter writer = new FileWriter(debug, true);
                writer.write(dateFormat.format(new Date()) + ": " + msg + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean createLogFile(File file) {
        try {
            return file.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }
}
