document.addEventListener('DOMContentLoaded', function () {

    var modoVisualizacao = document.getElementById('modoVisualizacao');
    if (modoVisualizacao && modoVisualizacao.value === 'true') {
        document.querySelectorAll('input, select, textarea').forEach(function (el) {
            el.disabled = true;
        });
        modoVisualizacao.disabled = false;
        return;
    }

    var form = document.getElementById('formVeiculo');
    var btnLimpar = document.getElementById('btnLimpar');
    var placaInput = document.getElementById('placa');
    var rntrcInput = document.getElementById('rntrc');
    var anoInput = document.getElementById('anoFabricacao');
    var camposNumericos = ['taraKg', 'capacidadeKg', 'volumeM3'];

    if (placaInput) {
        placaInput.addEventListener('input', function () {
            this.value = this.value.toUpperCase()
                .replace(/[^A-Z0-9-]/g, '')
                .substring(0, 8);
        });
    }

    if (rntrcInput) {
        rntrcInput.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').substring(0, 8);
        });
    }

    if (anoInput) {
        anoInput.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').substring(0, 4);
        });
    }

    camposNumericos.forEach(function (id) {
        var el = document.getElementById(id);
        if (!el) return;

        el.addEventListener('input', function () {
            this.value = sanitizarDecimal(this.value);
        });
    });

    document.querySelectorAll('input, select').forEach(function (el) {
        el.addEventListener('input', function () { limparErroCampo(el); });
        el.addEventListener('change', function () { limparErroCampo(el); });
    });

    if (btnLimpar && form) {
        btnLimpar.addEventListener('click', function () {
            form.reset();
            removerAlertas();
            document.querySelectorAll('.campo-invalido').forEach(function (el) {
                limparErroCampo(el);
            });
            if (placaInput) placaInput.focus();
        });
    }

    if (form) {
        form.addEventListener('submit', function (e) {
            var camposObrigatorios = [
                { id: 'placa', label: 'Placa' },
                { id: 'rntrc', label: 'RNTRC' },
                { id: 'anoFabricacao', label: 'Ano de fabricação' },
                { id: 'tipo', label: 'Tipo' },
                { id: 'status', label: 'Status' },
                { id: 'taraKg', label: 'Tara' },
                { id: 'capacidadeKg', label: 'Capacidade de carga' },
                { id: 'volumeM3', label: 'Volume' }
            ];

            var pendencias = [];

            camposObrigatorios.forEach(function (campo) {
                var el = document.getElementById(campo.id);
                if (!el) return;

                if (!el.value || el.value.trim() === '') {
                    pendencias.push(campo.label);
                    marcarErroCampo(el, 'Este campo é obrigatório.');
                }
            });

            validarPlaca(pendencias);
            validarRntrc(pendencias);
            validarAno(pendencias);
            validarTipo(pendencias);
            validarStatus(pendencias);
            validarNumeroPositivo('taraKg', 'Tara', pendencias);
            validarNumeroPositivo('capacidadeKg', 'Capacidade de carga', pendencias);
            validarNumeroPositivo('volumeM3', 'Volume', pendencias);

            if (pendencias.length > 0) {
                e.preventDefault();
                mostrarAlertaErro(
                    'Por favor, revise os dados do veículo antes de salvar.',
                    'Campos pendentes: <strong>' + pendencias.join(', ') + '</strong>.'
                );

                var alerta = document.getElementById('alertaDinamico');
                if (alerta) alerta.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });
    }

    var alertaSucesso = document.getElementById('alertaSucesso');
    if (alertaSucesso) {
        setTimeout(function () {
            alertaSucesso.style.transition = 'opacity 0.5s';
            alertaSucesso.style.opacity = '0';
            setTimeout(function () { alertaSucesso.remove(); }, 500);
        }, 5000);
    }
});

function sanitizarDecimal(valor) {
    var limpo = valor.replace(/[^0-9,.]/g, '');
    var partes = limpo.split(/[,.]/);

    if (partes.length <= 1) {
        return limpo;
    }

    return partes.shift() + '.' + partes.join('').substring(0, 3);
}

function validarPlaca(pendencias) {
    var el = document.getElementById('placa');
    if (!el || !el.value.trim()) return;

    var placa = el.value.trim().toUpperCase();
    var placaAntiga = /^[A-Z]{3}-[0-9]{4}$/.test(placa);
    var placaMercosul = /^[A-Z]{3}[0-9][A-Z][0-9]{2}$/.test(placa);

    if (!placaAntiga && !placaMercosul) {
        pendencias.push('Placa inválida');
        marcarErroCampo(el, 'Use o padrão ABC-1234 ou ABC1D23.');
    }
}

function validarAno(pendencias) {
    var el = document.getElementById('anoFabricacao');
    if (!el || !el.value.trim()) return;

    var ano = parseInt(el.value, 10);
    var anoAtual = new Date().getFullYear();

    if (isNaN(ano) || ano < 1950 || ano > anoAtual + 1) {
        pendencias.push('Ano inválido');
        marcarErroCampo(el, 'Informe um ano entre 1950 e ' + (anoAtual + 1) + '.');
    }
}

function validarRntrc(pendencias) {
    var el = document.getElementById('rntrc');
    if (!el || !el.value.trim()) return;

    var rntrc = el.value.trim();
    if (!/^\d{8}$/.test(rntrc)) {
        pendencias.push('RNTRC inválido');
        marcarErroCampo(el, 'O RNTRC deve conter exatamente 8 dígitos numéricos.');
    }
}

function validarNumeroPositivo(id, label, pendencias) {
    var el = document.getElementById(id);
    if (!el || !el.value.trim()) return;

    var valor = parseFloat(el.value.replace(',', '.'));
    if (isNaN(valor) || valor <= 0) {
        pendencias.push(label + ' inválida');
        marcarErroCampo(el, 'Informe um valor maior que zero.');
    }
}

function validarTipo(pendencias) {
    var el = document.getElementById('tipo');
    if (!el || !el.value.trim()) return;

    var permitidos = ['TRUCK', 'CARRETA', 'VAN', 'UTILITARIO'];
    if (permitidos.indexOf(el.value) < 0) {
        pendencias.push('Tipo inválido');
        marcarErroCampo(el, 'Selecione Truck, Carreta, Van ou Utilitário.');
    }
}

function validarStatus(pendencias) {
    var el = document.getElementById('status');
    if (!el || !el.value.trim()) return;

    var permitidos = ['DISPONIVEL', 'EM_VIAGEM', 'MANUTENCAO'];
    if (permitidos.indexOf(el.value) < 0) {
        pendencias.push('Status inválido');
        marcarErroCampo(el, 'Selecione Disponível, Em Viagem ou Em Manutenção.');
    }
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

    var content = document.querySelector('.veiculo-content');
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
