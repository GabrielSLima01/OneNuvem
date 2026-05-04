import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DnsServer {
    private static final int DEFAULT_PORT = 5050;
    private final int port;
    private final Map<String, RegistroDns> registros;

    public DnsServer(int port) {
        this.port = port;
        this.registros = new ConcurrentHashMap<String, RegistroDns>();
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor DNS iniciado na porta " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClienteHandler(socket, registros)).start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar servidor DNS: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Porta invalida. Usando porta padrao " + DEFAULT_PORT);
            }
        }

        new DnsServer(port).iniciar();
    }

    private static class ClienteHandler implements Runnable {
        private final Socket socket;
        private final Map<String, RegistroDns> registros;

        public ClienteHandler(Socket socket, Map<String, RegistroDns> registros) {
            this.socket = socket;
            this.registros = registros;
        }

        @Override
        public void run() {
            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String linha = reader.readLine();

                if (linha == null || linha.trim().isEmpty()) {
                    writer.println("ERROR Comando vazio");
                    return;
                }

                String resposta = processarComando(linha);
                writer.println(resposta);
            } catch (IOException e) {
                System.out.println("Erro ao atender cliente: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Erro ao fechar socket: " + e.getMessage());
                }
            }
        }

        private String processarComando(String linha) {
            String[] partes = linha.trim().split("\\s+");
            String comando = partes[0].toUpperCase();

            if ("REGISTER".equals(comando)) {
                return registrar(partes);
            }

            if ("LOOKUP".equals(comando)) {
                return buscar(partes);
            }

            if ("REMOVE".equals(comando)) {
                return remover(partes);
            }

            if ("LIST".equals(comando)) {
                return listar();
            }

            return "ERROR Comando desconhecido";
        }

        private String registrar(String[] partes) {
            if (partes.length != 4) {
                return "ERROR Uso: REGISTER nome host porta";
            }

            String nome = partes[1];
            String host = partes[2];
            int porta;

            try {
                porta = Integer.parseInt(partes[3]);
            } catch (NumberFormatException e) {
                return "ERROR Porta invalida";
            }

            registros.put(nome, new RegistroDns(host, porta));
            return "OK Registro salvo";
        }

        private String buscar(String[] partes) {
            if (partes.length != 2) {
                return "ERROR Uso: LOOKUP nome";
            }

            String nome = partes[1];
            RegistroDns registro = registros.get(nome);

            if (registro == null) {
                return "NOT_FOUND";
            }

            return "FOUND " + registro.getHost() + " " + registro.getPorta();
        }

        private String remover(String[] partes) {
            if (partes.length != 2) {
                return "ERROR Uso: REMOVE nome";
            }

            String nome = partes[1];
            RegistroDns removido = registros.remove(nome);

            if (removido == null) {
                return "NOT_FOUND";
            }

            return "OK Registro removido";
        }

        private String listar() {
            if (registros.isEmpty()) {
                return "EMPTY";
            }

            StringBuilder builder = new StringBuilder("LIST");

            for (Map.Entry<String, RegistroDns> entry : registros.entrySet()) {
                builder.append(" | ")
                    .append(entry.getKey())
                    .append(" -> ")
                    .append(entry.getValue().getHost())
                    .append(":")
                    .append(entry.getValue().getPorta());
            }

            return builder.toString();
        }
    }
}
