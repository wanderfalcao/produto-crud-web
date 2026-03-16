package br.com.infnet.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class ProdutoListPage extends BasePage {

    @FindBy(id = "btn-novo-produto")
    private WebElement btnNovoProduto;

    @FindBy(css = "#tabela-produtos tbody tr")
    private List<WebElement> linhas;

    @FindBy(css = ".alert-success")
    private WebElement alertSucesso;

    @FindBy(css = ".alert-danger")
    private WebElement alertErro;

    public ProdutoListPage(WebDriver driver) {
        super(driver);
    }

    public ProdutoFormPage clicarNovoProduto() {
        aguardarClicavel(By.id("btn-novo-produto"));
        clicarComJs(btnNovoProduto);
        return new ProdutoFormPage(driver);
    }

    public int contarProdutos() {
        aguardarElemento(By.id("tabela-produtos"));
        return (int) linhas.stream()
                .filter(l -> !l.findElements(By.cssSelector(".btn-excluir")).isEmpty())
                .count();
    }

    public ProdutoFormPage clicarEditarNaLinha(int indice) {
        aguardarElemento(By.cssSelector("#tabela-produtos tbody tr"));
        WebElement btnEditar = linhas.get(indice).findElement(By.cssSelector(".btn-editar"));
        clicarComJs(btnEditar);
        return new ProdutoFormPage(driver);
    }

    /** Clica em Excluir na linha indicada e confirma o alerta de confirmação. */
    public ProdutoListPage clicarExcluirNaLinha(int indice) {
        aguardarElemento(By.cssSelector("#tabela-produtos tbody tr"));
        WebElement btnExcluir = linhas.get(indice).findElement(By.cssSelector(".btn-excluir"));
        clicarComJs(btnExcluir);
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        wait.until(ExpectedConditions.stalenessOf(btnExcluir));
        return new ProdutoListPage(driver);
    }

    /** Clica em Excluir na linha indicada e cancela o alerta — produto permanece. */
    public ProdutoListPage cancelarExcluirNaLinha(int indice) {
        aguardarElemento(By.cssSelector("#tabela-produtos tbody tr"));
        WebElement btnExcluir = linhas.get(indice).findElement(By.cssSelector(".btn-excluir"));
        clicarComJs(btnExcluir);
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().dismiss();
        return new ProdutoListPage(driver);
    }

    public ProdutoDetalhePage clicarDetalheNaLinha(int indice) {
        aguardarElemento(By.cssSelector("#tabela-produtos tbody tr"));
        WebElement link = linhas.get(indice).findElement(By.cssSelector("a[title='Detalhe']"));
        clicarComJs(link);
        return new ProdutoDetalhePage(driver);
    }

    public boolean alertaSucessoVisivel() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String textoAlertaSucesso() {
        aguardarElemento(By.cssSelector(".alert-success"));
        return alertSucesso.getText();
    }
}
