package Mensageria;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MensageriaConfig {

    private static final Properties PROPRIEDADES = carregarPropriedades();

    private MensageriaConfig() {
    }

    public static String apiBaseUrl() {
        return obter("mensageria.api.baseUrl", "MENSAGERIA_API_BASE_URL", "http://localhost:8082");
    }

    public static String eventosPath() {
        return obter("mensageria.api.eventosPath", "MENSAGERIA_API_EVENTOS_PATH", "/api/mensageria/eventos");
    }

    public static String origem() {
        return obter("mensageria.origem", "MENSAGERIA_ORIGEM", "SISTEMA_FRETES_WEB");
    }

    public static boolean habilitada() {
        return Boolean.parseBoolean(obter("mensageria.habilitada", "MENSAGERIA_HABILITADA", "false"));
    }

    public static String endpointEventos() {
        return apiBaseUrl() + eventosPath();
    }

    public static String websocketPath() {
        return normalizarCaminho(obter("mensageria.websocket.path", "MENSAGERIA_WEBSOCKET_PATH", "/ws-fretes"));
    }

    public static String websocketTopic() {
        return normalizarCaminho(obter("mensageria.websocket.topic", "MENSAGERIA_WEBSOCKET_TOPIC", "/topic/fretes"));
    }

    public static String websocketUrl() {
        String baseUrl = apiBaseUrl().trim();
        String protocolo = baseUrl.startsWith("https://") ? "wss://" : "ws://";
        String semProtocolo = baseUrl.replaceFirst("^https?://", "");
        return protocolo + removerBarraFinal(semProtocolo) + websocketPath();
    }

    public static int maxEventosPorEnvio() {
        return parseInt(obter("mensageria.lote.maxEventos", "MENSAGERIA_LOTE_MAX_EVENTOS", "50"), 50);
    }

    public static int connectTimeoutMs() {
        return parseInt(obter("mensageria.api.connectTimeoutMs", "MENSAGERIA_API_CONNECT_TIMEOUT_MS", "5000"), 5000);
    }

    public static int readTimeoutMs() {
        return parseInt(obter("mensageria.api.readTimeoutMs", "MENSAGERIA_API_READ_TIMEOUT_MS", "7000"), 7000);
    }

    private static String obter(String chave, String variavelAmbiente, String valorPadrao) {
        String valorSistema = System.getProperty(chave);
        if (valorSistema != null && !valorSistema.trim().isEmpty()) {
            return valorSistema.trim();
        }

        String valorAmbiente = System.getenv(variavelAmbiente);
        if (valorAmbiente != null && !valorAmbiente.trim().isEmpty()) {
            return valorAmbiente.trim();
        }

        String valorArquivo = PROPRIEDADES.getProperty(chave);
        if (valorArquivo != null && !valorArquivo.trim().isEmpty()) {
            return valorArquivo.trim();
        }

        return valorPadrao;
    }

    private static Properties carregarPropriedades() {
        Properties props = new Properties();
        try (InputStream in = MensageriaConfig.class.getClassLoader().getResourceAsStream("mensageria.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // Configuracao opcional; valores padrao e variaveis de ambiente continuam valendo.
        }
        return props;
    }

    private static int parseInt(String valor, int valorPadrao) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return valorPadrao;
        }
    }

    private static String normalizarCaminho(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "";
        }
        String caminho = valor.trim();
        return caminho.startsWith("/") ? caminho : "/" + caminho;
    }

    private static String removerBarraFinal(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.endsWith("/") ? valor.substring(0, valor.length() - 1) : valor;
    }
}
