import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DnsClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5050;
    
    // Cores ANSI para console
    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    public static void main(String[] args) {
        printHeader();
        
        String dnsHost = DEFAULT_HOST;
        int dnsPort = DEFAULT_PORT;

        if (args.length >= 2 && "-host".equalsIgnoreCase(args[0])) {
            dnsHost = args[1];
        }

        if (args.length >= 4 && "-port".equalsIgnoreCase(args[2])) {
            try {
                dnsPort = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                printErro("Porta do DNS invalida.");
                return;
            }
        }

        printInfo("Servidor DNS: " + dnsHost + ":" + dnsPort);
        printLinha();
        
        // Sequência de operações de exemplo
        executarSequenciaExemplo(dnsHost, dnsPort);
    }
    
    private static void executarSequenciaExemplo(String host, int porta) {
        // 1. REGISTER web
        printProcessando("OPERAÇÃO 1: Registrando serviço WEB");
        executarComando(host, porta, "REGISTER web 192.168.1.100 8080");
        aguardar(2);
        
        // 2. REGISTER email
        printProcessando("OPERAÇÃO 2: Registrando serviço EMAIL");
        executarComando(host, porta, "REGISTER email 192.168.1.101 25");
        aguardar(2);
        
        // 3. REGISTER db
        printProcessando("OPERAÇÃO 3: Registrando serviço DATABASE");
        executarComando(host, porta, "REGISTER db 192.168.1.102 5432");
        aguardar(2);
        
        // 4. LIST
        printProcessando("OPERAÇÃO 4: Listando todos os registros");
        executarComando(host, porta, "LIST");
        aguardar(2);
        
        // 5. LOOKUP web
        printProcessando("OPERAÇÃO 5: Consultando serviço WEB");
        executarComando(host, porta, "LOOKUP web");
        aguardar(2);
        
        // 6. LOOKUP email
        printProcessando("OPERAÇÃO 6: Consultando serviço EMAIL");
        executarComando(host, porta, "LOOKUP email");
        aguardar(2);
        
        // 7. LOOKUP inexistente
        printProcessando("OPERAÇÃO 7: Consultando serviço inexistente");
        executarComando(host, porta, "LOOKUP ftp");
        aguardar(2);
        
        // 8. REMOVE email
        printProcessando("OPERAÇÃO 8: Removendo serviço EMAIL");
        executarComando(host, porta, "REMOVE email");
        aguardar(2);
        
        // 9. LIST final
        printProcessando("OPERAÇÃO 9: Listando registros finais");
        executarComando(host, porta, "LIST");
        aguardar(1);
        
        printLinha();
        printSucesso("✓ SEQUÊNCIA DE TESTES CONCLUÍDA COM SUCESSO!");
        printLinha();
    }
    
    private static void executarComando(String host, int porta, String comando) {
        enviarComando(host, porta, comando);
    }
    
    private static void aguardar(int segundos) {
        try {
            Thread.sleep(segundos * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void enviarComando(String host, int porta, String comando) {
        try {
            printInfo("Conectando ao servidor...");
            Socket socket = new Socket(host, porta);
            printSucesso("✓ Conexão estabelecida!");
            
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            printInfo("Enviando comando: " + comando);
            writer.println(comando);
            
            printProcessando("Aguardando resposta...");
            String resposta = reader.readLine();
            
            printLinha();
            if (resposta != null && resposta.startsWith("ERROR")) {
                printErro("ERRO: " + resposta);
            } else if (resposta != null && resposta.startsWith("NOT_FOUND")) {
                printAviso("Registro não encontrado!");
            } else if (resposta != null && resposta.startsWith("OK")) {
                printSucesso("✓ " + resposta);
            } else if (resposta != null && resposta.startsWith("FOUND")) {
                printSucesso("✓ Registro encontrado!");
                printInfo("Dados: " + resposta.substring(6));
            } else {
                printSucesso("Resposta: " + resposta);
            }
            printLinha();
            
            socket.close();
            printInfo("Conexão fechada.");
            
        } catch (IOException e) {
            printErro("Erro ao conectar no DNS: " + e.getMessage());
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
    
    private static void printHeader() {
        printLinha();
        System.out.println(BOLD + CYAN + "╔══════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + CYAN + "║     DNS CLIENT - OneNuvem System      ║" + RESET);
        System.out.println(BOLD + CYAN + "║   Distributed Systems - 2026          ║" + RESET);
        System.out.println(BOLD + CYAN + "╚══════════════════════════════════════╝" + RESET);
        printLinha();
    }
    
    private static void printLinha() {
        System.out.println(CYAN + "═══════════════════════════════════════════════════════════" + RESET);
    }
    
    private static void printInfo(String msg) {
        System.out.println(BLUE + "[INFO] " + RESET + msg + " " + getTimestamp());
    }
    
    private static void printSucesso(String msg) {
        System.out.println(GREEN + "[✓ SUCESSO] " + RESET + msg);
    }
    
    private static void printErro(String msg) {
        System.out.println(RED + "[✗ ERRO] " + RESET + msg);
    }
    
    private static void printAviso(String msg) {
        System.out.println(YELLOW + "[⚠ AVISO] " + RESET + msg);
    }
    
    private static void printProcessando(String msg) {
        System.out.println(YELLOW + "[⏳ PROCESSANDO] " + RESET + msg);
    }
    
    private static String getTimestamp() {
        return CYAN + "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")) + "]" + RESET;
    }
}
