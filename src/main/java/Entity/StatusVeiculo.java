package Entity;

public enum StatusVeiculo {
    DISPONIVEL("DISPONIVEL"),
    EM_VIAGEM("EM_VIAGEM"),
    MANUTENCAO("MANUTENCAO");

    private final String codigo;

    StatusVeiculo(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
