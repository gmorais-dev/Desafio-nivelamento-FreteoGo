package BO;

import DAO.OcorrenciaFreteDAO;
import Entity.OcorrenciaFrete;
import Exception.FreteException;

import java.sql.SQLException;
import java.util.List;

public class OcorrenciaFreteBO {

    private final OcorrenciaFreteDAO ocorrenciaFreteDAO = new OcorrenciaFreteDAO();

    public List<OcorrenciaFrete> listarPorFrete(int freteId) throws FreteException {
        if (freteId <= 0) {
            throw new FreteException("Frete inválido para consulta das ocorrências.");
        }
        try {
            return ocorrenciaFreteDAO.listarPorFrete(freteId);
        } catch (SQLException e) {
            throw new FreteException("Erro ao carregar as ocorrências do frete.", e);
        }
    }
}
