package Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapeamento:
 *   tabela  ? cliente
 *   coluna  ? atributo
 */
@Data               
@NoArgsConstructor
@AllArgsConstructor 
public class Cliente {

    private Integer id;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String inscricaoEstadual;
    private String tipo;
    private String logradouro;
    private Integer numero;
    private String complemento;
    private String bairro;
    private String municipio;
    private String uf;
    private String cep;
    private String telefone;
    private String email;
    private String status;
}
