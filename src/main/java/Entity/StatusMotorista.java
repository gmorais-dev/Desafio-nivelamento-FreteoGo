package Entity;

public enum StatusMotorista {
    ATIVO("ATIVO"),
    INATIVO("INATIVO"),
    SUSPENSO("SUSPENSO");

    private final String codigo;

    StatusMotorista(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static StatusMotorista fromCodigo(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        String normalizado = valor.trim().toUpperCase().replace(" ", "_");
        if ("AFASTADO".equals(normalizado)) {
            return SUSPENSO;
        }
        for (StatusMotorista status : values()) {
            if (status.codigo.equals(normalizado)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Status de motorista invalido: " + valor);
    }

    @Override
    public String toString() {
        return codigo;
    }
}
