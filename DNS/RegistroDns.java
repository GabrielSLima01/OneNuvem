public class RegistroDns {
    private final String host;
    private final int porta;

    public RegistroDns(String host, int porta) {
        this.host = host;
        this.porta = porta;
    }

    public String getHost() {
        return host;
    }

    public int getPorta() {
        return porta;
    }
}
