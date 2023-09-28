package netology.ru.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHistoryLogger {
    public static ClientHistoryLogger clientHistoryLogger;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static File log;

    public static ClientHistoryLogger getInstance() {
        if (clientHistoryLogger == null){
            clientHistoryLogger = new ClientHistoryLogger();
        }
        return clientHistoryLogger;
    }

    void info(String msg){
        if (createLogFile() | log.exists()){
            try {
                FileWriter writer = new FileWriter (log, true);
                writer.write(dateFormat.format(new Date()) + ": " + msg + "\n");
                writer.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

    }
    private boolean createLogFile(){
        try {
            log = new File("history.log");
            return log.createNewFile();
        } catch (IOException e){
            return false;
        }
    }
}