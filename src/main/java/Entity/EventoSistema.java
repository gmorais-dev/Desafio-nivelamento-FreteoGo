package Entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoSistema {

    private Integer id;
    private TipoEvento tipo;
    private String entidade;
    private Integer entidadeId;
    private String payload;
    private String status;
    private Integer tentativas;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataPublicacao;
    private String mensagemErro;
}
