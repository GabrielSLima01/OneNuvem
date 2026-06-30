package Server.serverbase;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NodeLogger {

    private static final String LOG_FILE = "node.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void info(String action, String message) {
        write("INFO", action, message);
    }

    public static synchronized void error(String action, String message) {
        write("ERROR", action, message);
    }

    private static void write(String level, String action, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String line = String.format("[%s] [%s] %s - %s", timestamp, level, action, message);

        System.out.println(line);

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(line);
        } catch (IOException e) {
            // se nem o log funcionar, ao menos avisa no console
            System.err.println("Falha ao escrever log em arquivo: " + e.getMessage());
        }
    }
}