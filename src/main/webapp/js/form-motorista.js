document.addEventListener('DOMContentLoaded', function () {

    var modoVisualizacao = document.getElementById('modoVisualizacao');
    if (modoVisualizacao && modoVisualizacao.value === 'true') {
        document.querySelectorAll('input, select, textarea').forEach(function (el) {
            el.disabled = true;
        });
        modoVisualizacao.disabled = false;
        return;
    }

    var form = document.getElementById('formMotorista');
    var btnLimpar = document.getElementById('btnLimpar');
    var cpfInput = document.getElementById('cpf');
    var telefoneInput = document.getElementById('telefone');
    var cnhNumeroInput = document.getElementById('cnhNumero');
    var cnhCategoriaInput = document.getElementById('cnhCategoria');

    if (cpfInput) {
        cpfInput.addEventListener('input', function () {
            var v = this.value.replace(/\D/g, '').substring(0, 11);
            v = v.replace(/^(\d{3})(\d)/, '$1.$2');
            v = v.replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3');
            v = v.replace(/\.(\d{3})(\d)/, '.$1-$2');
            this.value = v;
        });
    }

    if (telefoneInput) {
        telefoneInput.addEventListener('input', function () {
            var v = this.value.replace(/\D/g, '').substring(0, 11);
            v = v.replace(/^(\d{2})(\d)/, '($1) $2');
            v = v.replace(/(\d{5})(\d{1,4})$/, '$1-$2');
            this.value = v;
        });
    }

    if (cnhNumeroInput) {
        cnhNumeroInput.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').substring(0, 11);
        });
    }

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

            var nomeInput = document.getElementById('nome');
            if (nomeInput) nomeInput.focus();
        });
    }

    if (form) {
        form.addEventListener('submit', function (e) {
            var pendencias = [];
            var camposObrigatorios = [
                { id: 'nome', label: 'Nome completo' },
                { id: 'cpf', label: 'CPF' },
                { id: 'dataNascimento', label: 'Data de nascimento' },
                { id: 'telefone', label: 'Telefone' },
                { id: 'cnhNumero', label: 'Número da CNH' },
                { id: 'cnhCategoria', label: 'Categoria da CNH' },
                { id: 'cnhValidade', label: 'Validade da CNH' },
                { id: 'tipoVinculo', label: 'Tipo de vínculo' },
                { id: 'status', label: 'Status' }
            ];

            camposObrigatorios.forEach(function (campo) {
                var el = document.getElementById(campo.id);
                if (!el) return;

                if (!el.value || el.value.trim() === '') {
                    pendencias.push(campo.label);
                    marcarErroCampo(el, 'Este campo é obrigatório.');
                }
            });

            validarCpf(pendencias);
            validarTelefone(pendencias);
            validarDataNascimento(pendencias);
            validarCnhNumero(pendencias);
            validarCnhCategoria(pendencias);
            validarCnhValidadeInformada(pendencias);

            if (pendencias.length > 0) {
                e.preventDefault();
                mostrarAlertaErro(
                    'Por favor, revise os dados do motorista antes de salvar.',
                    'Campos pendentes: <strong>' + removerDuplicados(pendencias).join(', ') + '</strong>.'
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

function validarCpf(pendencias) {
    var el = document.getElementById('cpf');
    if (!el || !el.value.trim()) return;

    if (!cpfValido(el.value)) {
        pendencias.push('CPF inválido');
        marcarErroCampo(el, 'Informe um CPF válido.');
    }
}

function cpfValido(valor) {
    var cpf = valor.replace(/\D/g, '');

    if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
        return false;
    }

    var soma = 0;
    for (var i = 0; i < 9; i++) {
        soma += parseInt(cpf.charAt(i), 10) * (10 - i);
    }

    var primeiroDigito = 11 - (soma % 11);
    if (primeiroDigito >= 10) primeiroDigito = 0;
    if (primeiroDigito !== parseInt(cpf.charAt(9), 10)) {
        return false;
    }

    soma = 0;
    for (var j = 0; j < 10; j++) {
        soma += parseInt(cpf.charAt(j), 10) * (11 - j);
    }

    var segundoDigito = 11 - (soma % 11);
    if (segundoDigito >= 10) segundoDigito = 0;

    return segundoDigito === parseInt(cpf.charAt(10), 10);
}

function validarTelefone(pendencias) {
    var el = document.getElementById('telefone');
    if (!el || !el.value.trim()) return;

    var digitos = el.value.replace(/\D/g, '');
    if (digitos.length < 10 || digitos.length > 11) {
        pendencias.push('Telefone inválido');
        marcarErroCampo(el, 'Informe DDD e telefone completos.');
    }
}

function validarDataNascimento(pendencias) {
    var el = document.getElementById('dataNascimento');
    if (!el || !el.value.trim()) return;

    var data = criarDataLocal(el.value);
    var hoje = dataAtualSemHorario();

    if (!data || data >= hoje) {
        pendencias.push('Data de nascimento inválida');
        marcarErroCampo(el, 'A data de nascimento deve ser anterior à data atual.');
    }
}

function validarCnhNumero(pendencias) {
    var el = document.getElementById('cnhNumero');
    if (!el || !el.value.trim()) return;

    var digitos = el.value.replace(/\D/g, '');
    if (digitos.length !== 11) {
        pendencias.push('Número da CNH inválido');
        marcarErroCampo(el, 'Informe os 11 dígitos da CNH.');
    }
}

function validarCnhCategoria(pendencias) {
    var el = document.getElementById('cnhCategoria');
    if (!el || !el.value.trim()) return;

    var categoriasPermitidas = ['A', 'B', 'C', 'D', 'E'];
    var valor = el.value.toUpperCase();

    if (categoriasPermitidas.indexOf(valor) < 0) {
        pendencias.push('Categoria da CNH inválida');
        marcarErroCampo(el, 'Selecione uma categoria válida.');
    }
}

function validarCnhValidadeInformada(pendencias) {
    var el = document.getElementById('cnhValidade');
    if (!el || !el.value.trim()) return;

    var validade = criarDataLocal(el.value);

    if (!validade) {
        pendencias.push('Validade da CNH inválida');
        marcarErroCampo(el, 'Informe uma data de validade válida.');
    }
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

function dataAtualSemHorario() {
    var hoje = new Date();
    return new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
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

    var content = document.querySelector('.motorista-content');
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
