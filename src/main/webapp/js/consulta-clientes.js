// ============================================================
//  consulta-clientes.js
//  Funcionalidades de filtro e busca para tela de consulta
// ============================================================

document.addEventListener('DOMContentLoaded', function () {

    
    var usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    document.getElementById('nome-usuario').textContent = usuario.nome || 'Usuário';

    function atualizarTabela(lista) {
        var tbody = document.querySelector('#tblClientes tbody');
        tbody.innerHTML = '';
        
        if (!lista || lista.length === 0) {
            document.getElementById('lblResultados').textContent = 'Nenhum resultado encontrado';
            return;
        }
        
        lista.forEach(function (c) {
            var tr = document.createElement('tr');
            var contextPath = document.body.getAttribute('data-context-path') || '';
            
            tr.innerHTML = '<td><strong>' + c.empresa + '</strong></td>' +
                '<td>' + c.razao + '</td>' +
                '<td>' + c.cnpj + '</td>' +
                '<td>' + c.ie + '</td>' +
                '<td>' + ((c.municipio || '') + ' / ' + (c.uf || '')) + '</td>' +
                '<td>' + (c.status === 'ATIVO' ? '<span class="badge-ativo">Ativo</span>' : '<span class="badge-inativo">Inativo</span>') + '</td>' +
                '<td class="acoes">' +
                '<a href="' + contextPath + '/ClienteController?acao=visualizar&id=' + c.id + '" title="Visualizar"><span class="material-symbols-outlined">visibility</span></a>' +
                '&nbsp;' +
                '<a href="' + contextPath + '/ClienteController?acao=editar&id=' + c.id + '" title="Editar"><span class="material-symbols-outlined">edit</span></a>' +
                '&nbsp;' +
                '<button type="button" class="btn-excluir" data-id="' + c.id + '" data-nome="' + escaparHtml(c.empresa || c.razao || '') + '" title="Excluir"><span class="material-symbols-outlined">close</span></button>' +
                '</td>';
            
            tbody.appendChild(tr);
        });
        
        document.getElementById('lblResultados').textContent = lista.length + ' resultados encontrados';
    }

    function excluirCliente(id, nome) {
        if (!confirm('Confirma a exclusão do cliente ' + (nome || '') + '?')) {
            return;
        }

        var contextPath = document.body.getAttribute('data-context-path') || '';

        fetch(contextPath + '/ClienteController', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: 'acao=excluir&id=' + encodeURIComponent(id)
        })
            .then(function(resp) {
                return resp.text().then(function(texto) {
                    var dados = {};
                    try {
                        dados = texto ? JSON.parse(texto) : {};
                    } catch (e) {
                        dados.mensagem = texto || 'Erro ao excluir cliente.';
                    }
                    if (!resp.ok) {
                        throw new Error(dados.mensagem || 'Erro ao excluir cliente.');
                    }
                    return dados;
                });
            })
            .then(function(dados) {
                alert(dados.mensagem || 'Cliente excluído com sucesso.');
                buscarClientes();
            })
            .catch(function(err) {
                alert(err.message || 'Erro ao excluir cliente.');
            });
    }

    function buscarClientes() {
        var termo = document.getElementById('txtFiltro').value.trim();
        var campo = document.getElementById('selCampo').value;
        var status = document.getElementById('selStatus').value;
        var contextPath = document.body.getAttribute('data-context-path') || '';
        
        // filtro baseado no campo selecionado
        var filtroFinal = termo;
        
        
        var url = contextPath + '/ClienteController?acao=buscar'
            + (filtroFinal ? ('&filtro=' + encodeURIComponent(filtroFinal)) : '')
            + (status ? ('&status=' + encodeURIComponent(status)) : '')
            + '&pagina=1';
        
        fetch(url)
            .then(function(resp) {
                if (!resp.ok) throw new Error('Erro ao buscar');
                return resp.json();
            })
            .then(function(lista) {
                if (status) {
                    lista = lista.filter(function(c) { return c.status === status; });
                }
                atualizarTabela(lista);
            })
            .catch(function(err) {
                console.error('Erro na busca:', err);
                atualizarTabela([]);
            });
    }

    document.getElementById('btnBuscar').addEventListener('click', buscarClientes);

    document.querySelector('#tblClientes tbody').addEventListener('click', function (e) {
        var botao = e.target.closest('.btn-excluir');
        if (!botao) return;
        excluirCliente(botao.getAttribute('data-id'), botao.getAttribute('data-nome'));
    });
    
    document.getElementById('btnLimpar').addEventListener('click', function () {
        document.getElementById('txtFiltro').value = '';
        document.getElementById('selCampo').value = 'todos';
        document.getElementById('selStatus').value = '';
        buscarClientes();
    });
    
    document.getElementById('txtFiltro').addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            buscarClientes();
        }
    });

    buscarClientes();

});

function escaparHtml(valor) {
    return String(valor || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
