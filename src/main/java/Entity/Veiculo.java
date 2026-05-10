package Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapeamento:
 *   tabela  -> veiculo
 *   coluna  -> atributo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Veiculo {

    private Integer id;
    private String placa;
    private String rntrc;
    private Integer anoFabricacao;
    private String tipo;
    private Double taraKg;
    private Double capacidadeKg;
    private Double volumeM3;
    private String status;
}
