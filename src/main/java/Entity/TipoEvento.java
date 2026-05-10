package Entity;

public enum TipoEvento {
    FRETE_CRIADO("FRETE_CRIADO"),
    FRETE_SAIDA_CONFIRMADA("FRETE_SAIDA_CONFIRMADA"),
    FRETE_EM_TRANSITO("FRETE_EM_TRANSITO"),
    FRETE_ENTREGUE("FRETE_ENTREGUE"),
    FRETE_NAO_ENTREGUE("FRETE_NAO_ENTREGUE"),
    FRETE_CANCELADO("FRETE_CANCELADO"),
    OCORRENCIA_FRETE_REGISTRADA("OCORRENCIA_FRETE_REGISTRADA");

    private final String codigo;

    TipoEvento(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
