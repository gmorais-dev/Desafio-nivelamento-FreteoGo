package Mensageria;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MensageriaHttpClient {

    public MensageriaHttpResponse enviarEvento(String payload) throws IOException {
        HttpURLConnection conexao = null;
        try {
            URL url = new URL(MensageriaConfig.endpointEventos());
            conexao = (HttpURLConnection) url.openConnection();
            conexao.setRequestMethod("POST");
            conexao.setDoOutput(true);
            conexao.setConnectTimeout(MensageriaConfig.connectTimeoutMs());
            conexao.setReadTimeout(MensageriaConfig.readTimeoutMs());
            conexao.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conexao.setRequestProperty("Accept", "application/json");

            byte[] bytes = payload == null ? new byte[0] : payload.getBytes("UTF-8");
            OutputStream output = null;
            try {
                output = conexao.getOutputStream();
                output.write(bytes);
                output.flush();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            int status = conexao.getResponseCode();
            String body = lerCorpo(status >= 200 && status < 400 ? conexao.getInputStream() : conexao.getErrorStream());
            return new MensageriaHttpResponse(status, body);
        } finally {
            if (conexao != null) {
                conexao.disconnect();
            }
        }
    }

    private String lerCorpo(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder conteudo = new StringBuilder();
            String linha;
            while ((linha = reader.readLine()) != null) {
                conteudo.append(linha);
            }
            return conteudo.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
