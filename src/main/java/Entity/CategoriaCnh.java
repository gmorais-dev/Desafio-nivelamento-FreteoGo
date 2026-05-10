package Entity;

public enum CategoriaCnh {
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    E("E");

    private final String codigo;

    CategoriaCnh(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static CategoriaCnh fromCodigo(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        String normalizado = valor.trim()
                .toUpperCase()
                .replace("/", "")
                .replace("-", "")
                .replace(" ", "");

        for (CategoriaCnh categoria : values()) {
            if (categoria.codigo.equals(normalizado)) {
                return categoria;
            }
        }

        throw new IllegalArgumentException("Categoria de CNH invalida: " + valor);
    }

    @Override
    public String toString() {
        return codigo;
    }
}
