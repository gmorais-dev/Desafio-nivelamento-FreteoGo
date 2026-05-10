document.addEventListener('DOMContentLoaded', function () {
    var tipoEl = document.getElementById('tipoOcorrencia');
    var dataHoraEl = document.getElementById('dataHoraOcorrencia');
    var ufEl = document.getElementById('ufOcorrencia');
    var grupoRecebedor = document.getElementById('grupoRecebedor');
    var obrigatorioDescricao = document.getElementById('obrigatorioDescricao');
    var obrigatorioRecebedorNome = document.getElementById('obrigatorioRecebedorNome');
    var obrigatorioRecebedorDoc = document.getElementById('obrigatorioRecebedorDoc');
    var form = document.getElementById('formOcorrencia');

    if (dataHoraEl && !dataHoraEl.value) {
        dataHoraEl.value = dataHoraAtualLocal();
    }

    if (ufEl) {
        ufEl.addEventListener('input', function () {
            this.value = this.value.toUpperCase().replace(/[^A-Z]/g, '').substring(0, 2);
        });
    }

    function atualizarCamposDinamicos() {
        var tipo = tipoEl ? tipoEl.value : '';
        var exigeDescricao = tipo === 'AVARIA' || tipo === 'EXTRAVIO' || tipo === 'OUTROS';
        var exigeRecebedor = tipo === 'ENTREGA_REALIZADA';

        if (obrigatorioDescricao) obrigatorioDescricao.hidden = !exigeDescricao;
        if (obrigatorioRecebedorNome) obrigatorioRecebedorNome.hidden = !exigeRecebedor;
        if (obrigatorioRecebedorDoc) obrigatorioRecebedorDoc.hidden = !exigeRecebedor;

        if (grupoRecebedor) {
            grupoRecebedor.classList.toggle('oculto', !exigeRecebedor);
        }
    }

    if (tipoEl) {
        tipoEl.addEventListener('change', atualizarCamposDinamicos);
        atualizarCamposDinamicos();
    }

    if (form) {
        form.addEventListener('submit', function (event) {
            var tipo = tipoEl ? tipoEl.value : '';
            var descricao = document.getElementById('descricaoOcorrencia');
            var nomeRecebedor = document.getElementById('nomeRecebedor');
            var documentoRecebedor = document.getElementById('documentoRecebedor');
            var mensagens = [];

            if (!tipo) mensagens.push('Selecione o tipo da ocorrência.');
            if (!dataHoraEl || !dataHoraEl.value) mensagens.push('Informe a data e hora da ocorrência.');

            if (tipo === 'AVARIA' || tipo === 'EXTRAVIO' || tipo === 'OUTROS') {
                if (!descricao || !descricao.value.trim()) {
                    mensagens.push('A descrição é obrigatória para este tipo de ocorrência.');
                }
            }

            if (tipo === 'ENTREGA_REALIZADA') {
                if (!nomeRecebedor || !nomeRecebedor.value.trim()) {
                    mensagens.push('Informe o nome do recebedor.');
                }
                if (!documentoRecebedor || !documentoRecebedor.value.trim()) {
                    mensagens.push('Informe o documento do recebedor.');
                }
            }

            if (mensagens.length > 0) {
                event.preventDefault();
                window.alert(mensagens.join('\n'));
            }
        });
    }
});

function dataHoraAtualLocal() {
    var agora = new Date();
    var ano = agora.getFullYear();
    var mes = String(agora.getMonth() + 1).padStart(2, '0');
    var dia = String(agora.getDate()).padStart(2, '0');
    var hora = String(agora.getHours()).padStart(2, '0');
    var minuto = String(agora.getMinutes()).padStart(2, '0');

    return ano + '-' + mes + '-' + dia + 'T' + hora + ':' + minuto;
}
