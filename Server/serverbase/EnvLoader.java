package Server.serverbase;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {

    private static final Map<String, String> variables = new HashMap<>();

    static {

        Path path = Files.exists(Path.of("server.env"))
                ? Path.of("server.env")
                : Path.of("server.env.example");

        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))){

            String line;

            while ((line = reader.readLine()) != null){

                if (line.contains("=") && !line.trim().startsWith("#")){

                    String[] parts =line.split("=", 2);
                    String key =parts[0].trim();
                    String value =parts[1].trim().replace("\"", "").replace("'", "");

                    variables.put(key,value);
                }
            }

            reader.close();

        } catch (Exception e){

            e.printStackTrace();
        }
    }

    public static String get(String key){

        return variables.get(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        return variables.getOrDefault(key, defaultValue);
    }

    public static String getRequired(String key) {
        String value = variables.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Variavel obrigatoria ausente: " + key);
        }
        return value;
    }
}
