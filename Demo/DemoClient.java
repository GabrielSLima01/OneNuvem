import Requests.GatewayClient;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DemoClient {

    public static void main(String[] args) throws Exception {
        String host = args.length >= 1 ? args[0] : "localhost";
        int port = args.length >= 2 ? Integer.parseInt(args[1]) : 8000;
        String command = args.length >= 3 ? args[2] : "scenario";

        GatewayClient gateway = new GatewayClient(host, port);
        System.out.println("Gateway: " + host + ":" + port);

        if ("heartbeat".equalsIgnoreCase(command)) {
            gateway.heartbeat("demo-client");
            System.out.println("Heartbeat OK");
            return;
        }

        if ("upload".equalsIgnoreCase(command)) {
            String fileName = args.length >= 4 ? args[3] : "apresentacao.txt";
            String content = args.length >= 5 ? args[4] : "Conteudo de demonstracao da OneNuvem";

            gateway.uploadFile(fileName, content.getBytes(StandardCharsets.UTF_8));
            System.out.println("Upload OK: " + fileName);
            return;
        }

        if ("download".equalsIgnoreCase(command)) {
            String fileName = args.length >= 4 ? args[3] : "apresentacao.txt";
            byte[] downloaded = gateway.downloadFile(fileName);

            System.out.println("Download: " + new String(downloaded, StandardCharsets.UTF_8));
            return;
        }

        if ("list".equalsIgnoreCase(command)) {
            System.out.println("Arquivos visiveis pelo gateway: " + Arrays.toString(gateway.listFiles()));
            return;
        }

        runScenario(gateway);
    }

    private static void runScenario(GatewayClient gateway) throws Exception {
        byte[] content = "Conteudo de demonstracao da OneNuvem".getBytes(StandardCharsets.UTF_8);

        gateway.heartbeat("demo-client");
        System.out.println("Heartbeat OK");

        gateway.uploadFile("apresentacao.txt", content);
        System.out.println("Upload OK: apresentacao.txt");

        System.out.println("Arquivos visiveis pelo gateway: " + Arrays.toString(gateway.listFiles()));

        byte[] downloaded = gateway.downloadFile("apresentacao.txt");
        System.out.println("Download: " + new String(downloaded, StandardCharsets.UTF_8));
    }
}
