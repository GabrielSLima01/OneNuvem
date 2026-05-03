package Server.serverbase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {

    private static final Map<String, String>
            variables =
            new HashMap<>();

    static {

        try {

            BufferedReader reader =
                    new BufferedReader(
                            new FileReader(
                                    "server.env"
                            )
                    );

            String line;

            while (
                    (line =
                     reader.readLine())
                            != null
            ) {

                if (
                        line.contains("=")
                ) {

                    String[] parts =
                            line.split("=");

                    String key =
                            parts[0].trim();

                    String value =
                            parts[1].trim();

                    variables.put(
                            key,
                            value
                    );
                }
            }

            reader.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static String get(
            String key
    ) {

        return variables.get(key);
    }
}