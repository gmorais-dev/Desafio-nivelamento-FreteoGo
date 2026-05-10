package Entity;

import java.text.Normalizer;

public enum TipoOcorrenciaFrete {
    SAIDA_PATIO("SAIDA_PATIO", "Saída do Pátio"),
    EM_ROTA("EM_ROTA", "Em Rota"),
    TENTATIVA_ENTREGA("TENTATIVA_ENTREGA", "Tentativa de Entrega"),
    ENTREGA_REALIZADA("ENTREGA_REALIZADA", "Entrega Realizada"),
    AVARIA("AVARIA", "Avaria"),
    EXTRAVIO("EXTRAVIO", "Extravio"),
    OUTROS("OUTROS", "Outros");

    private final String codigo;
    private final String descricao;

    TipoOcorrenciaFrete(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoOcorrenciaFrete fromCodigo(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        String normalizado = removerAcentos(valor.trim())
            .toUpperCase()
            .replace("-", "_")
            .replace(" ", "_");

        for (TipoOcorrenciaFrete tipo : values()) {
            if (tipo.codigo.equals(normalizado)) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Tipo de ocorrência inválido: " + valor);
    }

    private static String removerAcentos(String valor) {
        return Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
