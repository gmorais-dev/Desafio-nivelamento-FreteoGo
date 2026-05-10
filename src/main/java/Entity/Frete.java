package Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Frete {

    private Integer id;
    private String numero;
    private Integer remetenteId;
    private Integer destinatarioId;
    private Integer motoristaId;
    private Integer veiculoId;
    private String municipioOrigem;
    private String ufOrigem;
    private String municipioDestino;
    private String ufDestino;
    private String descricaoCarga;
    private Double pesoKg;
    private Integer volumes;
    private Double valorFrete;
    private Double aliquotaIcms;
    private Double valorIcms;
    private Double valorTotal;
    private StatusFrete status;
    private LocalDateTime dataEmissao;
    private LocalDate dataPrevisaoEntrega;
    private LocalDateTime dataSaida;
    private LocalDateTime dataEntrega;

    private String remetenteNome;
    private String destinatarioNome;
    private String motoristaNome;
    private String veiculoPlaca;
}
