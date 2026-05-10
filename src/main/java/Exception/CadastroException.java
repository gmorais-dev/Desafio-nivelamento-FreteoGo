package Exception;

/**
 * Exceção para erros de cadastro (Cliente, Motorista, Veículo).
 * Lançada pelo BO quando uma validação de cadastro falha.
 */
public class CadastroException extends NegocioException {

    public CadastroException(String mensagem) {
        super(mensagem);
    }

    public CadastroException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
