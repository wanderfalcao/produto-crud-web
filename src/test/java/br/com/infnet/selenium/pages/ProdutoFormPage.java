package br.com.infnet.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ProdutoFormPage extends BasePage {

    @FindBy(id = "nome")
    private WebElement campoNome;

    @FindBy(id = "preco")
    private WebElement campoPreco;

    @FindBy(id = "descricao")
    private WebElement campoDescricao;

    @FindBy(id = "estoque")
    private WebElement campoEstoque;

    @FindBy(id = "btn-salvar")
    private WebElement btnSalvar;

    @FindBy(id = "btn-cancelar")
    private WebElement btnCancelar;

    public ProdutoFormPage(WebDriver driver) {
        super(driver);
    }

    // ── Ações de sucesso ──────────────────────────────────────────────────────

    /** Preenche o formulário e submete esperando redirecionamento para a lista. */
    public ProdutoListPage preencherESalvar(String nome, String preco) {
        aguardarElemento(By.id("nome"));
        campoNome.clear();
        campoNome.sendKeys(nome);
        campoPreco.clear();
        campoPreco.sendKeys(preco);
        clicarComJs(btnSalvar);
        return new ProdutoListPage(driver);
    }

    public ProdutoListPage cancelar() {
        clicarComJs(btnCancelar);
        return new ProdutoListPage(driver);
    }

    // ── Ações para cenários negativos ─────────────────────────────────────────

    /**
     * Preenche o formulário e submete esperando que o formulário seja
     * re-exibido com mensagem de erro (dados inválidos).
     */
    public ProdutoFormPage preencherESubmeterComErro(String nome, String preco) {
        aguardarElemento(By.id("nome"));
        // Define valores via JS para ignorar validação HTML5 e chegar à validação do servidor
        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('nome').value = arguments[0];" +
                "document.getElementById('preco').value = arguments[1];",
                nome, preco);
        // Submete o formulário diretamente, contornando a constraint validation do browser
        ((JavascriptExecutor) driver).executeScript(
                "document.querySelector('form').submit();");
        aguardarElemento(By.id("mensagem-erro"));
        return new ProdutoFormPage(driver);
    }

    /** Verifica se a mensagem de erro está visível no formulário. */
    public boolean erroEstaVisivel() {
        List<WebElement> elementos = driver.findElements(By.id("mensagem-erro"));
        return !elementos.isEmpty() && elementos.get(0).isDisplayed();
    }

    /** Retorna o texto da mensagem de erro exibida no formulário. */
    public String obterMensagemDeErro() {
        aguardarElemento(By.id("mensagem-erro"));
        return driver.findElement(By.id("mensagem-erro")).getText();
    }

    /** Preenche todos os campos (incluindo descrição e estoque) e salva. */
    public ProdutoListPage preencherCompleto(String nome, String preco, String descricao, String estoque) {
        aguardarElemento(By.id("nome"));
        campoNome.clear();
        campoNome.sendKeys(nome);
        campoPreco.clear();
        campoPreco.sendKeys(preco);
        if (descricao != null && campoDescricao != null) {
            campoDescricao.clear();
            campoDescricao.sendKeys(descricao);
        }
        if (estoque != null && campoEstoque != null) {
            campoEstoque.clear();
            campoEstoque.sendKeys(estoque);
        }
        clicarComJs(btnSalvar);
        return new ProdutoListPage(driver);
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public String getValorNome() {
        aguardarElemento(By.id("nome"));
        return campoNome.getAttribute("value");
    }

    public String getValorPreco() {
        aguardarElemento(By.id("preco"));
        return campoPreco.getAttribute("value");
    }
}
