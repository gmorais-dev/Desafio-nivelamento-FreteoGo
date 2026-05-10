package Mensageria;

public class ResultadoEnvioMensageria {

    private int totalEncontrados;
    private int enviados;
    private int erros;

    public void setTotalEncontrados(int totalEncontrados) {
        this.totalEncontrados = totalEncontrados;
    }

    public void incrementarEnviados() {
        this.enviados++;
    }

    public void incrementarErros() {
        this.erros++;
    }

    public int getTotalEncontrados() {
        return totalEncontrados;
    }

    public int getEnviados() {
        return enviados;
    }

    public int getErros() {
        return erros;
    }
}
