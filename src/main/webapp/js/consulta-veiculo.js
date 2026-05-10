// ============================================================
//  consulta-veiculo.js
//  Funcionalidades de filtro e busca para tela de consulta
// ============================================================

document.addEventListener('DOMContentLoaded', function () {

    var usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    document.getElementById('nome-usuario').textContent = usuario.nome || 'Usuário';

    function atualizarTabela(lista) {
        var tbody = document.querySelector('#tblVeiculos tbody');
        tbody.innerHTML = '';

        if (!lista || lista.length === 0) {
            document.getElementById('lblResultados').textContent = 'Nenhum resultado encontrado';
            return;
        }

        lista.forEach(function (v) {
            var tr = document.createElement('tr');
            var contextPath = document.body.getAttribute('data-context-path') || '';
            var id = v.id || '';
            var placa = v.placa || '';
            var rntrc = v.rntrc || '';
            var tipo = v.tipo || v.categoria || '';
            var status = v.status || '';

            tr.innerHTML = '<td><strong>' + escaparHtml(placa) + '</strong></td>' +
                '<td>' + escaparHtml(rntrc) + '</td>' +
                '<td>' + escaparHtml(formatarTexto(tipo)) + '</td>' +
                '<td>' + montarBadgeStatus(status) + '</td>' +
                '<td class="acoes">' +
                '<a href="' + contextPath + '/VeiculoController?acao=visualizar&id=' + encodeURIComponent(id) + '" title="Visualizar"><span class="material-symbols-outlined">visibility</span></a>' +
                '&nbsp;' +
                '<a href="' + contextPath + '/VeiculoController?acao=editar&id=' + encodeURIComponent(id) + '" title="Editar"><span class="material-symbols-outlined">edit</span></a>' +
                '&nbsp;' +
                '<button type="button" class="btn-excluir" data-id="' + escaparHtml(id) + '" data-nome="' + escaparHtml(placa) + '" title="Excluir"><span class="material-symbols-outlined">close</span></button>' +
                '</td>';

            tbody.appendChild(tr);
        });

        document.getElementById('lblResultados').textContent = lista.length + ' resultados encontrados';
    }

    function buscarVeiculos() {
        var termo = document.getElementById('txtFiltro').value.trim();
        var campo = document.getElementById('selCampo').value;
        var status = document.getElementById('selStatus').value;
        var contextPath = document.body.getAttribute('data-context-path') || '';

        var url = contextPath + '/VeiculoController?acao=buscar'
            + (termo ? ('&filtro=' + encodeURIComponent(termo)) : '')
            + (campo && campo !== 'todos' ? ('&campo=' + encodeURIComponent(campo)) : '')
            + (status ? ('&status=' + encodeURIComponent(status)) : '')
            + '&pagina=1';

        fetch(url)
            .then(function (resp) {
                if (!resp.ok) throw new Error('Erro ao buscar');
                return resp.json();
            })
            .then(function (lista) {
                lista = aplicarFiltrosLocais(lista || [], termo, campo, status);
                atualizarTabela(lista);
            })
            .catch(function (err) {
                console.error('Erro na busca:', err);
                atualizarTabela([]);
            });
    }

    function aplicarFiltrosLocais(lista, termo, campo, status) {
        var termoNormalizado = normalizar(termo);

        return lista.filter(function (m) {
            var statusVeiculo = m.status || '';
            if (status && statusVeiculo !== status) return false;
            if (!termoNormalizado) return true;
                 
            var valores = {
                placa: m.placa || '',
                rntrc: m.rntrc || '',
                categoria: m.tipo || m.categoria || '',
                status: m.status || ''
            };

            if (campo && campo !== 'todos') {
                return normalizar(valores[campo] || '').indexOf(termoNormalizado) >= 0;
            }

            return Object.keys(valores).some(function (chave) {
                return normalizar(valores[chave]).indexOf(termoNormalizado) >= 0;
            });
        });
    }

    document.getElementById('btnBuscar').addEventListener('click', buscarVeiculos);

    document.querySelector('#tblVeiculos tbody').addEventListener('click', function (e) {
        var botao = e.target.closest('.btn-excluir');
        if (!botao) return;
        excluirRegistro('VeiculoController', botao.getAttribute('data-id'), botao.getAttribute('data-nome'), buscarVeiculos);
    });

    document.getElementById('btnLimpar').addEventListener('click', function () {
        document.getElementById('txtFiltro').value = '';
        document.getElementById('selCampo').value = 'todos';
        document.getElementById('selStatus').value = '';
        buscarVeiculos();
    });

    document.getElementById('txtFiltro').addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            buscarVeiculos();
        }
    });

    buscarVeiculos();
});

function montarBadgeStatus(status) {
    if (status === 'ATIVO') {
        return '<span class="badge-ativo">Ativo</span>';
    }

    if (status === 'MANUTENCAO') {
        return '<span class="badge-afastado">Em Manutenção</span>';
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
