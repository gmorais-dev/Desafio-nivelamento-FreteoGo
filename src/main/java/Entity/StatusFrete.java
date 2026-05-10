package Entity;

import java.text.Normalizer;

public enum StatusFrete {
    PENDENTE("PENDENTE"),
    EMITIDO("EMITIDO"),
    SAIDA_CONFIRMADA("SAIDA_CONFIRMADA"),
    EM_TRANSITO("EM_TRANSITO"),
    ENTREGUE("ENTREGUE"),
    NAO_ENTREGUE("NAO_ENTREGUE"),
    CANCELADO("CANCELADO");

    private final String codigo;

    StatusFrete(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static StatusFrete fromCodigo(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        String normalizado = removerAcentos(valor.trim())
                .toUpperCase()
                .replace("-", "_")
                .replace(" ", "_");

        for (StatusFrete status : values()) {
            if (status.codigo.equals(normalizado)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Status de frete invalido: " + valor);
    }

    private static String removerAcentos(String valor) {
        return Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    @Override
    public String toString() {
        return codigo;
    }
}
