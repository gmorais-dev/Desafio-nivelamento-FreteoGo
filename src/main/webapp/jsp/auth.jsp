<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FreteGo</title>
    <link rel="stylesheet"
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body>

<div class="auth-card">

    <div class="auth-toggle">
        <button id="btn-entrar" class="active">Entrar</button>
        <button id="btn-criar-conta">Criar conta</button>
    </div>

    <!-- LOGIN -->
    <div id="form-login">
        <div class="auth-header">
            <h1>Bem-vindo de volta</h1>
            <p>Entre para gerenciar seus fretes.</p>
        </div>

        <div id="erro-login" class="mensagem-erro hidden"></div>

        <form id="frm-login" novalidate>
            <div class="form-group">
                <label for="login-email">E-mail</label>
                <div class="input-wrapper">
                    <span class="icon material-symbols-outlined">mail</span>
                    <input type="email" id="login-email" name="email"
                           placeholder="voce@exemplo.com" autocomplete="email" required>
                </div>
            </div>
            <div class="form-group">
                <label for="login-senha">Senha</label>
                <div class="input-wrapper">
                    <span class="icon material-symbols-outlined">lock</span>
                    <input type="password" id="login-senha" name="senha"
                           placeholder="••••••••" autocomplete="current-password" required>
                </div>
            </div>
            <button type="submit" class="btn-primary" id="btn-submit-login">
                Entrar no sistema
                <span class="material-symbols-outlined">arrow_forward</span>
            </button>
        </form>

        <p class="auth-footer">
            Novo no FreteGo
            <a href="#" id="link-criar-conta">Crie uma conta</a>
        </p>
    </div>

    <!-- CADASTRO -->
    <div id="form-cadastro" class="hidden">
        <div class="auth-header">
            <h1>Comece agora</h1>
            <p>Crie sua conta gratuita em segundos.</p>
        </div>

        <div id="erro-cadastro" class="mensagem-erro hidden"></div>

        <form id="frm-cadastro" novalidate>
            <div class="form-group">
                <label for="cad-nome">Nome completo</label>
                <div class="input-wrapper">
                    <span class="icon material-symbols-outlined">person</span>
                    <input type="text" id="cad-nome" name="nome"
                           placeholder="Seu nome completo" autocomplete="name" required>
                </div>
            </div>
            <div class="form-group">
                <label for="cad-email">E-mail</label>
                <div class="input-wrapper">
                    <span class="icon material-symbols-outlined">mail</span>
                    <input type="email" id="cad-email" name="email"
                           placeholder="seuemail@exemplo.com" autocomplete="email" required>
                </div>
            </div>
            <div class="form-group">
                <label for="cad-senha">Senha</label>
                <div class="input-wrapper">
                    <span class="icon material-symbols-outlined">lock</span>
                    <input type="password" id="cad-senha" name="senha"
                           placeholder="minimo 6 caracteres" autocomplete="new-password" required minlength="6">
                </div>
            </div>
            <button type="submit" class="btn-primary" id="btn-submit-cadastro">
                Criar minha conta
                <span class="material-symbols-outlined">arrow_forward</span>
            </button>
        </form>

        <p class="auth-footer">
            Já tem conta?
            <a href="#" id="link-fazer-login">Faça login</a>
        </p>
    </div>

</div>

<script>
    // Passa o contextPath do servidor para o JS
    var CTX_PATH = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/js/auth.js"></script>

</body>
</html>
