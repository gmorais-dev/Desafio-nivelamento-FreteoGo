package Exception;

public class FreteException extends NegocioException {

    public FreteException(String mensagem) {
        super(mensagem);
    }

    public FreteException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
