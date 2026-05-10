package Entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcorrenciaFrete {

    private Integer id;
    private Integer freteId;
    private TipoOcorrenciaFrete tipo;
    private LocalDateTime dataHora;
    private String municipio;
    private String uf;
    private String descricao;
    private String nomeRecebedor;
    private String documentoRecebedor;
}
