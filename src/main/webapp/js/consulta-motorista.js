// ============================================================
//  consulta-motorista.js
//  Funcionalidades de filtro e busca para tela de consulta
// ============================================================

document.addEventListener('DOMContentLoaded', function () {

    var usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    document.getElementById('nome-usuario').textContent = usuario.nome || 'Usuário';

    function atualizarTabela(lista) {
        var tbody = document.querySelector('#tblMotoristas tbody');
        tbody.innerHTML = '';

        if (!lista || lista.length === 0) {
            document.getElementById('lblResultados').textContent = 'Nenhum resultado encontrado';
            return;
        }

        lista.forEach(function (m) {
            var tr = document.createElement('tr');
            var contextPath = document.body.getAttribute('data-context-path') || '';
            var id = m.id || '';
            var nome = m.nome || m.nomeCompleto || '';
            var cpf = m.cpf || '';
            var telefone = m.telefone || '';
            var cnhNumero = m.cnhNumero || m.cnh || '';
            var cnhCategoria = m.cnhCategoria || m.categoria || '';
            var cnhValidade = formatarData(m.cnhValidade || m.validadeCnh || '');
            var tipoVinculo = m.tipoVinculo || m.vinculo || '';
            var status = m.status || '';

            tr.innerHTML = '<td><strong>' + escaparHtml(nome) + '</strong></td>' +
                '<td>' + escaparHtml(cpf) + '</td>' +
                '<td>' + escaparHtml(telefone) + '</td>' +
                '<td>' + escaparHtml(cnhNumero) + '</td>' +
                '<td>' + escaparHtml(cnhCategoria) + '</td>' +
                '<td>' + escaparHtml(cnhValidade) + '</td>' +
                '<td>' + escaparHtml(formatarTexto(tipoVinculo)) + '</td>' +
                '<td>' + montarBadgeStatus(status) + '</td>' +
                '<td class="acoes">' +
                '<a href="' + contextPath + '/MotoristaController?acao=visualizar&id=' + encodeURIComponent(id) + '" title="Visualizar"><span class="material-symbols-outlined">visibility</span></a>' +
                '&nbsp;' +
                '<a href="' + contextPath + '/MotoristaController?acao=editar&id=' + encodeURIComponent(id) + '" title="Editar"><span class="material-symbols-outlined">edit</span></a>' +
                '&nbsp;' +
                '<button type="button" class="btn-excluir" data-id="' + escaparHtml(id) + '" data-nome="' + escaparHtml(nome) + '" title="Excluir"><span class="material-symbols-outlined">close</span></button>' +
                '</td>';

            tbody.appendChild(tr);
        });

        document.getElementById('lblResultados').textContent = lista.length + ' resultados encontrados';
    }

    function buscarMotoristas() {
        var termo = document.getElementById('txtFiltro').value.trim();
        var campo = document.getElementById('selCampo').value;
        var status = document.getElementById('selStatus').value;
        var contextPath = document.body.getAttribute('data-context-path') || '';

        var url = contextPath + '/MotoristaController?acao=buscar'
            + (termo ? ('&filtro=' + encodeURIComponent(termo)) : '')
            + (campo && campo !== 'todos' ? ('&campo=' + encodeURIComponent(campo)) : '')
            + (status ? ('&status=' + encodeURIComponent(status)) : '')
            + '&pagina=1';

        fetch(url)
            .then(function (resp) {
                return resp.text().then(function (texto) {
                    var dados = texto ? JSON.parse(texto) : [];
                    if (!resp.ok) throw new Error(dados.mensagem || 'Erro ao buscar');
                    return dados;
                });
            })
            .then(function (lista) {
                lista = aplicarFiltrosLocais(lista || [], termo, campo, status);
                atualizarTabela(lista);
            })
            .catch(function (err) {
                console.error('Erro na busca:', err);
                atualizarTabela([]);
                document.getElementById('lblResultados').textContent = err.message || 'Erro ao buscar motoristas';
            });
    }

    function aplicarFiltrosLocais(lista, termo, campo, status) {
        var termoNormalizado = normalizar(termo);

        return lista.filter(function (m) {
            var statusMotorista = m.status || '';
            if (status && statusMotorista !== status) return false;
            if (!termoNormalizado) return true;

            var valores = {
                nome: m.nome || m.nomeCompleto || '',
                cpf: m.cpf || '',
                cnh: m.cnhNumero || m.cnh || '',
                categoria: m.cnhCategoria || m.categoria || '',
                vinculo: m.tipoVinculo || m.vinculo || ''
            };

            if (campo && campo !== 'todos') {
                return normalizar(valores[campo] || '').indexOf(termoNormalizado) >= 0;
            }

            return Object.keys(valores).some(function (chave) {
                return normalizar(valores[chave]).indexOf(termoNormalizado) >= 0;
            });
        });
    }

    document.getElementById('btnBuscar').addEventListener('click', function () {
        buscarMotoristas();
    });

    document.querySelector('#tblMotoristas tbody').addEventListener('click', function (e) {
        var botao = e.target.closest('.btn-excluir');
        if (!botao) return;
        excluirRegistro('MotoristaController', botao.getAttribute('data-id'), botao.getAttribute('data-nome'), buscarMotoristas);
    });

    document.getElementById('btnLimpar').addEventListener('click', function () {
        document.getElementById('txtFiltro').value = '';
        document.getElementById('selCampo').value = 'todos';
        document.getElementById('selStatus').value = '';
        buscarMotoristas();
    });

    document.getElementById('txtFiltro').addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            buscarMotoristas();
        }
    });

    buscarMotoristas();
});

function montarBadgeStatus(status) {
    if (status === 'ATIVO') {
        return '<span class="badge-ativo">Ativo</span>';
    }

    if (status === 'SUSPENSO' || status === 'AFASTADO') {
        return '<span class="badge-afastado">Suspenso</span>';
    }

    return '<span class="badge-inativo">' + escaparHtml(formatarTexto(status || 'INATIVO')) + '</span>';
}

function formatarData(valor) {
    if (!valor) return '';

    var partes = valor.split('-');
    if (partes.length !== 3) return valor;

    return partes[2] + '/' + partes[1] + '/' + partes[0];
}

function formatarTexto(valor) {
    if (!valor) return '';

    return String(valor)
        .replace(/_/g, ' ')
        .toLowerCase()
        .replace(/(^|\s)\S/g, function (letra) {
            return letra.toUpperCase();
        });
}

function normalizar(valor) {
    return String(valor || '')
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '');
}

function escaparHtml(valor) {
    return String(valor || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function excluirRegistro(controller, id, nome, callback) {
    if (!confirm('Confirma a exclusão de ' + (nome || 'este registro') + '?')) return;

    var contextPath = document.body.getAttribute('data-context-path') || '';

    fetch(contextPath + '/' + controller, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
        body: 'acao=excluir&id=' + encodeURIComponent(id)
    })
        .then(function (resp) {
            return resp.text().then(function (texto) {
                var dados = {};
                try { dados = texto ? JSON.parse(texto) : {}; } catch (e) { dados.mensagem = texto; }
                if (!resp.ok) throw new Error(dados.mensagem || 'Erro ao excluir.');
                return dados;
            });
        })
        .then(function (dados) {
            alert(dados.mensagem || 'Registro excluído com sucesso.');
            callback();
        })
        .catch(function (err) {
            alert(err.message || 'Erro ao excluir.');
        });
}
