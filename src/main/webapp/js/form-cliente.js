
document.addEventListener('DOMContentLoaded', function () {

    // Verificar modo visualização
    var modoVisualizacao = document.getElementById('modoVisualizacao');
    if (modoVisualizacao && modoVisualizacao.value === 'true') {
        // Desabilitar todos os campos de input e select em modo visualização
        document.querySelectorAll('input, select, textarea').forEach(function (el) {
            el.disabled = true;
        });
        
        // Excluir o campo oculto de desabilitação
        var campooculto = document.getElementById('modoVisualizacao');
        if (campooculto) campooculto.disabled = false;
        
        return; // Pular validações
    }

    var cnpjInput = document.getElementById('cnpj');
    if (cnpjInput) {
        cnpjInput.addEventListener('input', function () {
            var v = this.value.replace(/\D/g, '').substring(0, 14);
            v = v.replace(/^(\d{2})(\d)/, '$1.$2');
            v = v.replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3');
            v = v.replace(/\.(\d{3})(\d)/, '.$1/$2');
            v = v.replace(/(\d{4})(\d)/, '$1-$2');
            this.value = v;
        });
    }

    var cepInput = document.getElementById('cep');
    if (cepInput) {
        cepInput.addEventListener('input', function () {
            var v = this.value.replace(/\D/g, '').substring(0, 8);
            v = v.replace(/(\d{5})(\d)/, '$1-$2');
            this.value = v;
        });

        cepInput.addEventListener('blur', function () {
            var cepLimpo = this.value.replace(/\D/g, '');
            if (cepLimpo.length !== 8) return;

            setCepCarregando(true);

            fetch('https://viacep.com.br/ws/' + cepLimpo + '/json/')
                .then(function (res) { return res.json(); })
                .then(function (dados) {
                    setCepCarregando(false);

                    if (dados.erro) {
                        marcarErroCampo(cepInput, 'CEP não encontrado. Verifique e tente novamente.');
                        return;
                    }

                    preencherCampo('logradouro', dados.logradouro);
                    preencherCampo('bairro',     dados.bairro);
                    preencherCampo('municipio',  dados.localidade);
                    preencherCampo('uf',         dados.uf);

                    var numeroEl = document.getElementById('numero');
                    if (numeroEl) numeroEl.focus();
                })
                .catch(function () {
                    setCepCarregando(false);
                    marcarErroCampo(cepInput, 'Nao foi possivel consultar o CEP. Verifique sua conexao.');
                });
        });
    }

    var telInput = document.getElementById('telefone');
    if (telInput) {
        telInput.addEventListener('input', function () {
            var v = this.value.replace(/\D/g, '').substring(0, 11);
            v = v.replace(/^(\d{2})(\d)/, '($1) $2');
            v = v.replace(/(\d{5})(\d{1,4})$/, '$1-$2');
            this.value = v;
        });
    }

    var ufInput = document.getElementById('uf');
    if (ufInput) {
        ufInput.addEventListener('input', function () {
            this.value = this.value.toUpperCase().substring(0, 2);
        });
    }

    document.querySelectorAll('input, select').forEach(function (el) {
        el.addEventListener('input', function () { limparErroCampo(el); });
        el.addEventListener('change', function () { limparErroCampo(el); });
    });

    var form = document.getElementById('formCliente');
    if (form) {
        var btnLimpar = document.getElementById('btnLimpar');
        if (btnLimpar) {
            btnLimpar.addEventListener('click', function () {
                form.reset();
                removerAlertas();
                document.querySelectorAll('.campo-invalido').forEach(function (el) {
                    limparErroCampo(el);
                });

                var razaoSocialInput = document.getElementById('razaoSocial');
                if (razaoSocialInput) razaoSocialInput.focus();
            });
        }

        form.addEventListener('submit', function (e) {
            var campos = [
                { id: 'razaoSocial',  label: 'Razão Social' },
                { id: 'nomeFantasia', label: 'Nome Fantasia' },
                { id: 'cnpj',        label: 'CNPJ' },
                { id: 'tipo',        label: 'Tipo de cliente' },
                { id: 'logradouro',  label: 'Logradouro' },
                { id: 'numero',      label: 'Número' },
                { id: 'bairro',      label: 'Bairro' },
                { id: 'municipio',   label: 'Município' },
                { id: 'uf',          label: 'UF' },
                { id: 'cep',         label: 'CEP' },
                { id: 'telefone',    label: 'Telefone' },
                { id: 'email',       label: 'E-mail' }
            ];

            var faltando = [];

            campos.forEach(function (c) {
                var el = document.getElementById(c.id);
                if (!el) return;
                var val = el.value.trim();
                if (!val || val === '') {
                    faltando.push(c.label);
                    marcarErroCampo(el, 'Este campo é obrigatório.');
                }
            });

            if (cnpjInput && cnpjInput.value.trim().length > 0 && cnpjInput.value.trim().length < 18) {
                if (!faltando.includes('CNPJ')) {
                    faltando.push('CNPJ (incompleto)');
                    marcarErroCampo(cnpjInput, 'Digite o CNPJ completo: 00.000.000/0000-00');
                }
            }

            var ufEl = document.getElementById('uf');
            if (ufEl && ufEl.value.trim().length > 0 && ufEl.value.trim().length < 2) {
                if (!faltando.includes('UF')) {
                    faltando.push('UF (inválida)');
                    marcarErroCampo(ufEl, 'Informe a sigla do estado com 2 letras (ex: SP).');
                }
            }


            //validacao cep 
            var cepEl = document.getElementById('cep');
            if (cepEl && cepEl.value.trim().length > 0 && cepEl.value.trim().length < 9) {
                if (!faltando.includes('CEP')) {
                    faltando.push('CEP (incompleto)');
                    marcarErroCampo(cepEl, 'Digite o CEP completo: 00000-000');
                }
            }

            if (faltando.length > 0) {
                e.preventDefault();
                mostrarAlertaErro(
                    'Por favor, preencha os campos obrigatórios antes de salvar.',
                    'Campos pendentes: <strong>' + faltando.join(', ') + '</strong>.'
                );
                // Scroll suave até o alerta
                var alerta = document.getElementById('alertaDinamico');
                if (alerta) alerta.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });
    }

    // Auto-fechar alertas de sucesso após 5 segundos
    var alertaSucesso = document.getElementById('alertaSucesso');
    if (alertaSucesso) {
        setTimeout(function () {
            alertaSucesso.style.transition = 'opacity 0.5s';
            alertaSucesso.style.opacity = '0';
            setTimeout(function () { alertaSucesso.remove(); }, 500);
        }, 5000);
    }
});

// ?? Funções auxiliares ?????????????????????????????????????

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

    // Remove também o alerta dinâmico se existir
    var alertaDinamico = document.getElementById('alertaDinamico');
    if (alertaDinamico) alertaDinamico.remove();
}

// Preenche um campo e limpa erro caso estivesse marcado
function preencherCampo(id, valor) {
    var el = document.getElementById(id);
    if (!el || !valor) return;
    el.value = valor;
    limparErroCampo(el);
}

// Feedback visual no campo CEP enquanto consulta a API
function setCepCarregando(ativo) {
    var el = document.getElementById('cep');
    if (!el) return;
    if (ativo) {
        el.disabled = true;
        el.style.opacity = '0.6';
        el.placeholder = 'Buscando...';
    } else {
        el.disabled = false;
        el.style.opacity = '';
        el.placeholder = '00000-000';
    }
}

function mostrarAlertaErro(titulo, detalhe) {
    // Remove alerta anterior se houver
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

    var content = document.querySelector('.cliente-content');
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
