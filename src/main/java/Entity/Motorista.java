package Entity;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Motorista {

    private Integer id;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String telefone;
    private String cnhNumero;
    private CategoriaCnh cnhCategoria;
    private LocalDate cnhValidade;
    private String tipoVinculo;
    private StatusMotorista status;
}
