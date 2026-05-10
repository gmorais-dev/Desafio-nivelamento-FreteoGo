package BO;

import DAO.EventoSistemaDAO;
import Entity.EventoSistema;
import Exception.MensageriaException;
import Mensageria.MensageriaConfig;
import Mensageria.MensageriaHttpClient;
import Mensageria.MensageriaHttpResponse;
import Mensageria.ResultadoEnvioMensageria;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class MensageriaBO {

    private final EventoSistemaDAO eventoSistemaDAO = new EventoSistemaDAO();
    private final MensageriaHttpClient mensageriaHttpClient = new MensageriaHttpClient();

    public ResultadoEnvioMensageria enviarPendentes() throws MensageriaException {
        if (!MensageriaConfig.habilitada()) {
            throw new MensageriaException("Mensageria desabilitada na configuração do sistema.");
        }

        try {
            List<EventoSistema> eventos = eventoSistemaDAO.listarParaEnvio(MensageriaConfig.maxEventosPorEnvio());
            ResultadoEnvioMensageria resultado = new ResultadoEnvioMensageria();
            resultado.setTotalEncontrados(eventos.size());

            for (EventoSistema evento : eventos) {
                processarEvento(evento, resultado);
            }

            return resultado;
        } catch (SQLException e) {
            throw new MensageriaException("Erro ao consultar eventos pendentes para mensageria.", e);
        }
    }

    private void processarEvento(EventoSistema evento, ResultadoEnvioMensageria resultado) throws MensageriaException {
        try {
            MensageriaHttpResponse response = mensageriaHttpClient.enviarEvento(evento.getPayload());
            if (response.isSucesso()) {
                eventoSistemaDAO.marcarComoEnviado(evento.getId());
                resultado.incrementarEnviados();
                return;
            }

            eventoSistemaDAO.marcarComoErro(evento.getId(), montarMensagemErro(response));
            resultado.incrementarErros();
        } catch (IOException e) {
            registrarErroDeEnvio(evento, resultado, "Falha de comunicação com a API de mensageria: " + e.getMessage());
        } catch (SQLException e) {
            throw new MensageriaException("Erro ao atualizar o status do evento " + evento.getId() + " na mensageria.", e);
        }
    }

    private void registrarErroDeEnvio(EventoSistema evento, ResultadoEnvioMensageria resultado, String mensagem)
            throws MensageriaException {
        try {
            eventoSistemaDAO.marcarComoErro(evento.getId(), mensagem);
            resultado.incrementarErros();
        } catch (SQLException e) {
            throw new MensageriaException("Erro ao registrar falha de envio do evento " + evento.getId() + ".", e);
        }
    }

    private String montarMensagemErro(MensageriaHttpResponse response) {
        StringBuilder mensagem = new StringBuilder();
        mensagem.append("API de mensageria respondeu HTTP ").append(response.getStatusCode());
        if (response.getResponseBody() != null && !response.getResponseBody().trim().isEmpty()) {
            mensagem.append(": ").append(response.getResponseBody().trim());
        }
        return mensagem.toString();
    }
}
