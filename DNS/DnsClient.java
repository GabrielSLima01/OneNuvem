import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class DnsClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5050;

    public static void main(String[] args) {
        if (args.length == 0) {
            mostrarUso();
            return;
        }

        String dnsHost = DEFAULT_HOST;
        int dnsPort = DEFAULT_PORT;
        int comandoInicio = 0;

        if (args.length >= 2 && "-host".equalsIgnoreCase(args[0])) {
            dnsHost = args[1];
            comandoInicio = 2;
        }

        if (args.length >= 4 && "-port".equalsIgnoreCase(args[2])) {
            try {
                dnsPort = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                System.out.println("Porta do DNS invalida.");
                return;
            }
            comandoInicio = 4;
        }

        if (comandoInicio >= args.length) {
            mostrarUso();
            return;
        }

        StringBuilder comando = new StringBuilder();
        for (int i = comandoInicio; i < args.length; i++) {
            if (i > comandoInicio) {
                comando.append(" ");
            }
            comando.append(args[i]);
        }

        enviarComando(dnsHost, dnsPort, comando.toString());
    }

    private static void enviarComando(String host, int porta, String comando) {
        try (
            Socket socket = new Socket(host, porta);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            writer.println(comando);
            String resposta = reader.readLine();
            System.out.println("Resposta do DNS: " + resposta);
        } catch (IOException e) {
            System.out.println("Erro ao conectar no DNS: " + e.getMessage());
        }
    }

    private static void mostrarUso() {
        System.out.println("Uso:");
        System.out.println("java -cp DNS DnsClient REGISTER nome host porta");
        System.out.println("java -cp DNS DnsClient LOOKUP nome");
        System.out.println("java -cp DNS DnsClient REMOVE nome");
        System.out.println("java -cp DNS DnsClient LIST");
        System.out.println("Opcional: java -cp DNS DnsClient -host localhost -port 5050 LOOKUP nome");
    }
}
