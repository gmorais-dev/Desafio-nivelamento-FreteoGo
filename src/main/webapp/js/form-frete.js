document.addEventListener('DOMContentLoaded', function () {

    var form = document.getElementById('formFrete');
    var btnLimpar = document.getElementById('btnLimpar');
    var dataEmissaoInput = document.getElementById('dataEmissao');
    var valorFreteInput = document.getElementById('valorFrete');
    var aliquotaIcmsInput = document.getElementById('aliquotaIcms');
    var pesoInput = document.getElementById('pesoKg');
    var volumesInput = document.getElementById('volumes');
    var valorIcmsInput = document.getElementById('valorIcms');
    var valorTotalInput = document.getElementById('valorTotal');
    var valorTotalTexto = document.getElementById('valorTotalTexto');

    if (dataEmissaoInput && !dataEmissaoInput.value) {
        dataEmissaoInput.value = dataAtualIso();
    }

    ['ufOrigem', 'ufDestino'].forEach(function (id) {
        var el = document.getElementById(id);
        if (!el) return;

        el.addEventListener('input', function () {
            this.value = this.value.toUpperCase().replace(/[^A-Z]/g, '').substring(0, 2);
        });
    });

    [valorFreteInput, aliquotaIcmsInput, pesoInput].forEach(function (el) {
        if (!el) return;
        el.addEventListener('input', function () {
            this.value = sanitizarDecimal(this.value);
            atualizarTotais();
        });
    });

    if (volumesInput) {
        volumesInput.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').substring(0, 6);
        });
    }

    document.querySelectorAll('input, select, textarea').forEach(function (el) {
        el.addEventListener('input', function () { limparErroCampo(el); });
        el.addEventListener('change', function () {
            limparErroCampo(el);
            atualizarTotais();
        });
    });

    if (btnLimpar && form) {
        btnLimpar.addEventListener('click', function () {
            form.reset();
            removerAlertas();
            document.querySelectorAll('.campo-invalido').forEach(function (el) {
                limparErroCampo(el);
            });
            if (dataEmissaoInput) dataEmissaoInput.value = dataAtualIso();
            atualizarTotais();
        });
    }

    if (form) {
        form.addEventListener('submit', function (e) {
            var pendencias = [];
            var camposObrigatorios = [
                { id: 'dataEmissao', label: 'Data de emissão' },
                { id: 'dataPrevisaoEntrega', label: 'Previsão de entrega' },
                { id: 'remetenteId', label: 'Remetente' },
                { id: 'destinatarioId', label: 'Destinatário' },
                { id: 'motoristaId', label: 'Motorista' },
                { id: 'veiculoId', label: 'Veículo' },
                { id: 'municipioOrigem', label: 'Origem' },
                { id: 'ufOrigem', label: 'UF origem' },
                { id: 'municipioDestino', label: 'Destino' },
                { id: 'ufDestino', label: 'UF destino' },
                { id: 'descricaoCarga', label: 'Descrição da carga' },
                { id: 'pesoKg', label: 'Peso bruto' },
                { id: 'volumes', label: 'Volumes' },
                { id: 'valorFrete', label: 'Valor do frete' },
                { id: 'aliquotaIcms', label: 'Alíquota ICMS' }
            ];

            camposObrigatorios.forEach(function (campo) {
                var el = document.getElementById(campo.id);
                if (!el) return;

                if (!el.value || el.value.trim() === '') {
                    pendencias.push(campo.label);
                    marcarErroCampo(el, 'Este campo é obrigatório.');
                }
            });

            validarClientes(pendencias);
            validarDatas(pendencias);
            validarMotorista(pendencias);
            validarVeiculo(pendencias);
            validarCarga(pendencias);
            validarValores(pendencias);

            if (pendencias.length > 0) {
                e.preventDefault();
                mostrarAlertaErro(
                    'Por favor, revise os dados do frete antes de salvar.',
                    'Campos pendentes: <strong>' + removerDuplicados(pendencias).join(', ') + '</strong>.'
                );

                var alerta = document.getElementById('alertaDinamico');
                if (alerta) alerta.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });
    }

    atualizarTotais();
});

function validarClientes(pendencias) {
    var remetente = document.getElementById('remetenteId');
    var destinatario = document.getElementById('destinatarioId');

    if (!remetente || !destinatario || !remetente.value || !destinatario.value) return;

    if (remetente.value === destinatario.value) {
        pendencias.push('Remetente e destinatário');
        marcarErroCampo(destinatario, 'O destinatário deve ser diferente do remetente.');
    }
}

function validarDatas(pendencias) {
    var dataEmissaoEl = document.getElementById('dataEmissao');
    var previsaoEl = document.getElementById('dataPrevisaoEntrega');

    if (!dataEmissaoEl || !previsaoEl || !dataEmissaoEl.value || !previsaoEl.value) return;

    var dataEmissao = criarDataLocal(dataEmissaoEl.value);
    var previsao = criarDataLocal(previsaoEl.value);

    if (!dataEmissao || !previsao) return;

    if (previsao <= dataEmissao) {
        pendencias.push('Previsão de entrega');
        marcarErroCampo(previsaoEl, 'A previsão deve ser posterior à data de emissão.');
    }
}

function validarMotorista(pendencias) {
    var motoristaEl = document.getElementById('motoristaId');
    var dataEmissaoEl = document.getElementById('dataEmissao');
    if (!motoristaEl || !motoristaEl.value) return;

    var option = motoristaEl.options[motoristaEl.selectedIndex];
    var status = option.getAttribute('data-status');
    var freteStatus = option.getAttribute('data-frete-status');
    var cnhValidadeValor = option.getAttribute('data-cnh-validade');

    if (status !== 'ATIVO') {
        pendencias.push('Motorista inativo');
        marcarErroCampo(motoristaEl, 'O motorista deve estar com status Ativo.');
    }

    if (freteStatus === 'SAIDA_CONFIRMADA' || freteStatus === 'EM_TRANSITO') {
        pendencias.push('Motorista já em frete');
        marcarErroCampo(motoristaEl, 'O motorista não pode estar em SAÍDA CONFIRMADA ou EM TRÂNSITO.');
    }

    if (dataEmissaoEl && dataEmissaoEl.value && cnhValidadeValor) {
        var dataEmissao = criarDataLocal(dataEmissaoEl.value);
        var cnhValidade = criarDataLocal(cnhValidadeValor);

        if (cnhValidade && dataEmissao && cnhValidade < dataEmissao) {
            pendencias.push('CNH vencida');
            marcarErroCampo(motoristaEl, 'A CNH deve estar válida na data de emissão do frete.');
        }
    }
}

function validarVeiculo(pendencias) {
    var veiculoEl = document.getElementById('veiculoId');
    if (!veiculoEl || !veiculoEl.value) return;

    var option = veiculoEl.options[veiculoEl.selectedIndex];
    var status = option.getAttribute('data-status');

    if (status !== 'DISPONIVEL') {
        pendencias.push('Veículo indisponível');
        marcarErroCampo(veiculoEl, 'O veículo deve estar com status Disponível.');
    }
}

function validarCarga(pendencias) {
    var pesoEl = document.getElementById('pesoKg');
    var volumesEl = document.getElementById('volumes');
    var veiculoEl = document.getElementById('veiculoId');

    if (pesoEl && pesoEl.value.trim()) {
        var peso = parseFloat(pesoEl.value.replace(',', '.'));
        if (isNaN(peso) || peso <= 0) {
            pendencias.push('Peso bruto inválido');
            marcarErroCampo(pesoEl, 'Informe um peso maior que zero.');
        }

        if (veiculoEl && veiculoEl.value) {
            var option = veiculoEl.options[veiculoEl.selectedIndex];
            var capacidade = parseFloat(option.getAttribute('data-capacidade-kg') || '0');

            if (!isNaN(peso) && capacidade > 0 && peso > capacidade) {
                pendencias.push('Peso acima da capacidade');
                marcarErroCampo(pesoEl, 'O peso bruto não pode exceder a capacidade do veículo.');
            }
        }
    }

    if (volumesEl && volumesEl.value.trim()) {
        var volumes = parseInt(volumesEl.value, 10);
        if (isNaN(volumes) || volumes <= 0) {
            pendencias.push('Volumes inválidos');
            marcarErroCampo(volumesEl, 'Informe uma quantidade de volumes maior que zero.');
        }
    }
}

function validarValores(pendencias) {
    validarNumeroPositivo('valorFrete', 'Valor do frete', pendencias);
    validarPercentual('aliquotaIcms', 'Alíquota ICMS', pendencias);
}

function validarNumeroPositivo(id, label, pendencias) {
    var el = document.getElementById(id);
    if (!el || !el.value.trim()) return;

    var valor = parseFloat(el.value.replace(',', '.'));
    if (isNaN(valor) || valor <= 0) {
        pendencias.push(label + ' inválido');
        marcarErroCampo(el, 'Informe um valor maior que zero.');
    }
}

function validarPercentual(id, label, pendencias) {
    var el = document.getElementById(id);
    if (!el || !el.value.trim()) return;

    var valor = parseFloat(el.value.replace(',', '.'));
    if (isNaN(valor) || valor < 0 || valor > 100) {
        pendencias.push(label + ' inválida');
        marcarErroCampo(el, 'Informe um percentual entre 0 e 100.');
    }
}

function atualizarTotais() {
    var valorFreteEl = document.getElementById('valorFrete');
    var aliquotaEl = document.getElementById('aliquotaIcms');
    var valorIcmsEl = document.getElementById('valorIcms');
    var valorTotalEl = document.getElementById('valorTotal');
    var valorTotalTexto = document.getElementById('valorTotalTexto');

    if (!valorFreteEl || !aliquotaEl || !valorIcmsEl || !valorTotalEl || !valorTotalTexto) return;

    var valorFrete = parseFloat((valorFreteEl.value || '').replace(',', '.'));
    var aliquota = parseFloat((aliquotaEl.value || '').replace(',', '.'));

    if (isNaN(valorFrete)) valorFrete = 0;
    if (isNaN(aliquota)) aliquota = 0;

    var valorIcms = valorFrete * aliquota / 100;
    var valorTotal = valorFrete + valorIcms;

    valorIcmsEl.value = formatarMoeda(valorIcms);
    valorTotalEl.value = valorTotal.toFixed(2);
    valorTotalTexto.textContent = formatarMoeda(valorTotal);
}

function sanitizarDecimal(valor) {
    var limpo = valor.replace(/[^0-9,.]/g, '');
    var partes = limpo.split(/[,.]/);

    if (partes.length <= 1) {
        return limpo;
    }

    return partes.shift() + '.' + partes.join('').substring(0, 2);
}

function criarDataLocal(valor) {
    var partes = valor.split('-');
    if (partes.length !== 3) return null;

    var ano = parseInt(partes[0], 10);
    var mes = parseInt(partes[1], 10) - 1;
    var dia = parseInt(partes[2], 10);

    if (isNaN(ano) || isNaN(mes) || isNaN(dia)) return null;
    return new Date(ano, mes, dia);
}

function dataAtualIso() {
    var hoje = new Date();
    var mes = String(hoje.getMonth() + 1).padStart(2, '0');
    var dia = String(hoje.getDate()).padStart(2, '0');

    return hoje.getFullYear() + '-' + mes + '-' + dia;
}

function formatarMoeda(valor) {
    return 'R$ ' + valor.toFixed(2).replace('.', ',');
}

function marcarErroCampo(el, mensagem) {
    el.classList.add('campo-invalido');

    var parent = el.parentElement;
    var msgExistente = parent.querySelector('.msg-campo-erro');
    if (!msgExistente) {
        var span = document.createElement('span');
        span.className = 'msg-campo-erro';
        span.innerHTML = '<span class="material-symbols-outlined">error</span>' + mensagem;
        parent.appendChild(span);
    }
}

function limparErroCampo(el) {
    el.classList.remove('campo-invalido');

    var parent = el.parentElement;
    var msgExistente = parent.querySelector('.msg-campo-erro');
    if (msgExistente) msgExistente.remove();

    var alertaDinamico = document.getElementById('alertaDinamico');
    if (alertaDinamico) alertaDinamico.remove();
}

function mostrarAlertaErro(titulo, detalhe) {
    var anterior = document.getElementById('alertaDinamico');
    if (anterior) anterior.remove();

    var div = document.createElement('div');
    div.id = 'alertaDinamico';
    div.className = 'alert alert-erro';
    div.innerHTML =
        '<span class="material-symbols-outlined alert-icon">error</span>' +
        '<div class="alert-body">' +
            '<strong>' + titulo + '</strong>' +
            '<p>' + detalhe + '</p>' +
        '</div>' +
        '<button type="button" class="alert-close" onclick="this.parentElement.remove()" title="Fechar">' +
            '<span class="material-symbols-outlined">close</span>' +
        '</button>';

    var content = document.querySelector('.frete-content');
    if (content) content.prepend(div);
}

function removerAlertas() {
    var alertaDinamico = document.getElementById('alertaDinamico');
    var alertaErro = document.getElementById('alertaErro');
    var alertaSucesso = document.getElementById('alertaSucesso');

    if (alertaDinamico) alertaDinamico.remove();
    if (alertaErro) alertaErro.remove();
    if (alertaSucesso) alertaSucesso.remove();
}

function removerDuplicados(lista) {
    return lista.filter(function (item, indice) {
        return lista.indexOf(item) === indice;
    });
}
