package Exception;

public class MensageriaException extends NegocioException {

    public MensageriaException(String mensagem) {
        super(mensagem);
    }

    public MensageriaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
