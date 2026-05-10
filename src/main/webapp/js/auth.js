// ============================================================
//  auth.js - Autenticacao FreteGo
//  Autentica via API externa; em sucesso redireciona ao menu.
// ============================================================

// ── URLs da API ────────────────────────────────────────────
var API_LOGIN    = 'http://localhost:8080/auth/login';
var API_CADASTRO = 'http://localhost:8080/auth/cadastro';


var MENU_URL = (typeof CTX_PATH !== 'undefined' ? CTX_PATH : '') + '/jsp/menu.jsp';

document.addEventListener('DOMContentLoaded', function () {

    var btnEntrar     = document.getElementById('btn-entrar');
    var btnCriarConta = document.getElementById('btn-criar-conta');
    var formLogin     = document.getElementById('form-login');
    var formCadastro  = document.getElementById('form-cadastro');

    function mostrarLogin() {
        btnEntrar.classList.add('active');
        btnCriarConta.classList.remove('active');
        formLogin.classList.remove('hidden');
        formCadastro.classList.add('hidden');
        ocultarErro('erro-login');
    }

    function mostrarCadastro() {
        btnCriarConta.classList.add('active');
        btnEntrar.classList.remove('active');
        formCadastro.classList.remove('hidden');
        formLogin.classList.add('hidden');
        ocultarErro('erro-cadastro');
    }

    function mostrarErro(id, msg) {
        var el = document.getElementById(id);
        el.textContent = msg;
        el.classList.remove('hidden');
    }

    function ocultarErro(id) {
        var el = document.getElementById(id);
        if (el) el.classList.add('hidden');
    }

    function setBtnCarregando(btn, ativo) {
        if (ativo) {
            btn.disabled = true;
            btn.dataset.textoOriginal = btn.innerText;
            btn.textContent = 'Aguarde...';
            btn.style.opacity = '0.7';
        } else {
            btn.disabled = false;
            btn.textContent = btn.dataset.textoOriginal || btn.textContent;
            btn.style.opacity = '';
        }
    }

    btnEntrar.addEventListener('click', mostrarLogin);
    btnCriarConta.addEventListener('click', mostrarCadastro);
    document.getElementById('link-criar-conta').addEventListener('click', function (e) { e.preventDefault(); mostrarCadastro(); });
    document.getElementById('link-fazer-login').addEventListener('click', function (e)  { e.preventDefault(); mostrarLogin();   });

    document.getElementById('frm-login').addEventListener('submit', function (e) {
        e.preventDefault();
        ocultarErro('erro-login');

        var email = document.getElementById('login-email').value.trim();
        var senha = document.getElementById('login-senha').value;
        var btn   = document.getElementById('btn-submit-login');

        // Validacoes
        if (!email) { mostrarErro('erro-login', 'Informe seu e-mail para continuar.'); document.getElementById('login-email').focus(); return; }
        if (!senha)  { mostrarErro('erro-login', 'Informe sua senha para continuar.');  document.getElementById('login-senha').focus(); return; }

        setBtnCarregando(btn, true);

        fetch(API_LOGIN, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, senha: senha })
        })
        .then(function (res) {
            var status = res.status;
            return res.text().then(function (text) {
                var data = {};
                try { data = text ? JSON.parse(text) : {}; } catch (e) {}
                if (status === 401 || status === 403) {
                    throw new Error(data.message || 'E-mail ou senha invalidos. Tente novamente.');
                }
                if (status < 200 || status >= 300) {
                    throw new Error(data.message || 'Falha na autenticacao (status ' + status + '). Tente mais tarde.');
                }
                return data;
            });
        })
        .then(function (data) {
            if (data.token) localStorage.setItem('token', data.token);
            localStorage.setItem('usuario', JSON.stringify({
                nome:  data.nome  || data.name  || email.split('@')[0],
                email: data.email || email
            }));
            window.location.href = MENU_URL;
        })
        .catch(function (err) {
            setBtnCarregando(btn, false);
            mostrarErro('erro-login', err.message || 'Erro ao conectar. Verifique sua conexao.');
        });
    });

    document.getElementById('frm-cadastro').addEventListener('submit', function (e) {
        e.preventDefault();
        ocultarErro('erro-cadastro');

        var nome  = document.getElementById('cad-nome').value.trim();
        var email = document.getElementById('cad-email').value.trim();
        var senha = document.getElementById('cad-senha').value;
        var btn   = document.getElementById('btn-submit-cadastro');

        // Validacoes
        if (!nome)         { mostrarErro('erro-cadastro', 'Informe seu nome completo.');                    document.getElementById('cad-nome').focus();  return; }
        if (!email)        { mostrarErro('erro-cadastro', 'Informe seu e-mail.');                           document.getElementById('cad-email').focus(); return; }
        if (senha.length < 6) { mostrarErro('erro-cadastro', 'A senha deve ter pelo menos 6 caracteres.'); document.getElementById('cad-senha').focus(); return; }

        setBtnCarregando(btn, true);

        fetch(API_CADASTRO, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nome: nome, email: email, senha: senha })
        })
        .then(function (res) {
            var status = res.status;
            return res.text().then(function (text) {
                var data = {};
                try { data = text ? JSON.parse(text) : {}; } catch (e) {}
                if (status < 200 || status >= 300) {
                    throw new Error(data.message || 'Erro ao criar conta (status ' + status + '). Tente novamente.');
                }
                return data;
            });
        })
        .then(function () {
            // Cadastro ok → faz login automatico para obter o token
            return fetch(API_LOGIN, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: email, senha: senha })
            });
        })
        .then(function (res) {
            var status = res.status;
            return res.text().then(function (text) {
                var data = {};
                try { data = text ? JSON.parse(text) : {}; } catch (e) {}
                if (status < 200 || status >= 300) {
                    // Login automatico falhou, mas cadastro foi ok — redireciona mesmo assim
                    console.warn('Login automatico pos-cadastro falhou (status ' + status + ')');
                }
                return data;
            });
        })
        .then(function (data) {
            if (data.token) localStorage.setItem('token', data.token);
            localStorage.setItem('usuario', JSON.stringify({
                nome:  data.nome  || nome,
                email: data.email || email
            }));
            window.location.href = MENU_URL;
        })
        .catch(function (err) {
            setBtnCarregando(btn, false);
            mostrarErro('erro-cadastro', err.message || 'Erro ao conectar. Verifique sua conexao.');
        });
    });
});
