package br.com.infnet.selenium;

import br.com.infnet.repository.ProdutoRepository;
import br.com.infnet.selenium.pages.ProdutoDetalhePage;
import br.com.infnet.selenium.pages.ProdutoFormPage;
import br.com.infnet.selenium.pages.ProdutoListPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProdutoSeleniumTest {

    @LocalServerPort
    private int porta;

    @Autowired
    private ProdutoRepository repository;

    private static WebDriver driver;

    @BeforeAll
    static void setUpDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1280,800"
        );
        driver = new ChromeDriver(options);
    }

    @AfterAll
    static void tearDownDriver() {
        if (driver != null) driver.quit();
    }

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    private ProdutoListPage abrirLista() {
        driver.get("http://localhost:" + porta + "/produtos");
        return new ProdutoListPage(driver);
    }

    // ── Testes de fluxo completo (CRUD) ───────────────────────────────────────

    @Test
    @Order(1)
    void deveCadastrarProdutoEExibirNaLista() {
        ProdutoListPage lista = abrirLista();
        ProdutoFormPage form = lista.clicarNovoProduto();
        lista = form.preencherESalvar("Monitor 4K", "2500.00");

        assertThat(lista.contarProdutos()).isEqualTo(1);
    }

    @Test
    @Order(2)
    void deveCadastrarMultiplosProdutosEContabilizarCorretamente() {
        ProdutoListPage lista = abrirLista();

        ProdutoFormPage form = lista.clicarNovoProduto();
        lista = form.preencherESalvar("Teclado Mecanico", "350.00");

        form = lista.clicarNovoProduto();
        lista = form.preencherESalvar("Mouse Gamer", "199.90");

        form = lista.clicarNovoProduto();
        lista = form.preencherESalvar("Webcam HD", "89.90");

        assertThat(lista.contarProdutos()).isEqualTo(3);
    }

    @Test
    @Order(3)
    void deveEditarProdutoEExibirMensagemDeSucesso() {
        ProdutoListPage lista = abrirLista();
        ProdutoFormPage form = lista.clicarNovoProduto();
        lista = form.preencherESalvar("Produto Original", "100.00");

        form = lista.clicarEditarNaLinha(0);
        assertThat(form.getValorNome()).isEqualTo("Produto Original");

        lista = form.preencherESalvar("Produto Editado", "200.00");
        assertThat(lista.alertaSucessoVisivel()).isTrue();
    }

    @Test
    @Order(4)
    void deveExcluirProdutoEDecrementarContagem() {
        ProdutoListPage lista = abrirLista();

        ProdutoFormPage form = lista.clicarNovoProduto();
        lista = form.preencherESalvar("Produto para Excluir", "10.00");
        int antes = lista.contarProdutos();

        lista = lista.clicarExcluirNaLinha(0);

        assertThat(lista.contarProdutos()).isLessThan(antes);
    }

    @Test
    @Order(5)
    void deveCancelarFormularioEVoltarParaLista() {
        ProdutoListPage lista = abrirLista();
        ProdutoFormPage form = lista.clicarNovoProduto();
        lista = form.cancelar();

        assertThat(driver.getCurrentUrl()).contains("/produtos");
    }

    // ── Testes negativos: entradas inválidas via interface ────────────────────

    @Test
    @Order(6)
    void deveExibirErroAoCadastrarComNomeVazio() {
        ProdutoListPage lista = abrirLista();
        ProdutoFormPage form = lista.clicarNovoProduto();

        form = form.preencherESubmeterComErro("", "100.00");

        assertThat(form.erroEstaVisivel()).isTrue();
        assertThat(form.obterMensagemDeErro()).contains("Nome obrigatorio");
    }

    @Test
    @Order(7)
    void deveExibirErroAoCadastrarComPrecoNegativo() {
        ProdutoListPage lista = abrirLista();
        ProdutoFormPage form = lista.clicarNovoProduto();

        form = form.preencherESubmeterComErro("Produto Valido", "-5.00");

        assertThat(form.erroEstaVisivel()).isTrue();
        assertThat(form.obterMensagemDeErro()).contains("Preco deve ser maior que zero");
    }

    // ── Testes parametrizados ─────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
        "Teclado Gamer,    299.90",
        "Mouse Sem Fio,    149.50",
        "Headset Pro,      599.00"
    })
    @Order(8)
    void deveCadastrarProdutosParametrizados(String nome, String preco) {
        ProdutoListPage lista = abrirLista();
        ProdutoFormPage form = lista.clicarNovoProduto();
        lista = form.preencherESalvar(nome.trim(), preco.trim());

        assertThat(lista.contarProdutos()).isEqualTo(1);
    }

    // ── Teste de alerta de confirmação ────────────────────────────────────────

    @Test
    @Order(9)
    void deveManterProdutoAoCancelarConfirmacaoDeExclusao() {
        ProdutoListPage lista = abrirLista();
        ProdutoFormPage form = lista.clicarNovoProduto();
        lista = form.preencherESalvar("Produto Cancelado", "50.00");
        int antes = lista.contarProdutos();

        lista = lista.cancelarExcluirNaLinha(0);

        assertThat(lista.contarProdutos()).isEqualTo(antes);
    }

    // ── Novos testes: descricao, estoque e promoção ───────────────────────────

    @Test
    @Order(10)
    void devePersistirDescricaoEEstoqueAoCadastrar() {
        ProdutoListPage lista = abrirLista();
        ProdutoFormPage form = lista.clicarNovoProduto();
        lista = form.preencherCompleto("Monitor Full HD", "1200.00", "Monitor de alta resolução", "15");

        assertThat(lista.contarProdutos()).isEqualTo(1);
        // Navega para o detalhe e verifica os campos
        ProdutoDetalhePage detalhe = lista.clicarDetalheNaLinha(0);
        assertThat(detalhe.getNome()).isEqualTo("Monitor Full HD");
        assertThat(detalhe.getEstoque()).isEqualTo("15");
        assertThat(detalhe.getDescricao()).contains("alta resolução");
    }

    @Test
    @Order(11)
    void deveNavegaParaDetalheProduto() {
        ProdutoListPage lista = abrirLista();
        ProdutoFormPage form = lista.clicarNovoProduto();
        lista = form.preencherESalvar("Produto Detalhe Teste", "299.00");

        ProdutoDetalhePage detalhe = lista.clicarDetalheNaLinha(0);

        assertThat(detalhe.getNome()).isEqualTo("Produto Detalhe Teste");
        assertThat(driver.getCurrentUrl()).contains("/produtos/");
    }

    @Test
    @Order(12)
    void deveExibirPreviewCorretoAoDigitarPercentualDePromocao() {
        ProdutoListPage lista = abrirLista();
        ProdutoFormPage form = lista.clicarNovoProduto();
        lista = form.preencherESalvar("Produto Promo", "1000.00");

        ProdutoDetalhePage detalhe = lista.clicarDetalheNaLinha(0);
        detalhe.ativarFormPromo();
        String preview = detalhe.preencherPromocaoEObterPreview("10");

        // 1000 - 10% = 900
        assertThat(preview).isEqualTo("900.00");
    }
}
