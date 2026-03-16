package br.com.infnet.config;

import br.com.infnet.domain.CategoriaProduto;
import br.com.infnet.domain.Produto;
import br.com.infnet.domain.SkuGenerator;
import br.com.infnet.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Popula o banco com dados de exemplo em qualquer perfil exceto {@code prod}.
 * Idempotente em reinicializações.
 */
@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final ProdutoRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            log.info("DataLoader: banco já possui dados — seed ignorado.");
            return;
        }

        log.info("DataLoader: inserindo produtos de exemplo...");

        Produto monitor = produto("Monitor 4K UHD 27\"", new BigDecimal("2499.90"), 8, CategoriaProduto.MONITORES);
        monitor.ativarPromocao(new BigDecimal("15"), LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        repository.save(monitor);

        repository.save(produto("Teclado Mecânico RGB",  new BigDecimal("349.00"),  15, CategoriaProduto.PERIFERICOS));
        repository.save(produto("Mouse Gamer 16000 DPI", new BigDecimal("199.90"),  20, CategoriaProduto.PERIFERICOS));

        Produto headset = produto("Headset Surround 7.1", new BigDecimal("279.00"), 12, CategoriaProduto.AUDIO_VIDEO);
        headset.ativarPromocao(new BigDecimal("10"), null, null);
        repository.save(headset);

        repository.save(produto("Webcam Full HD 1080p",  new BigDecimal("89.90"),   30, CategoriaProduto.PERIFERICOS));
        repository.save(produto("SSD NVMe 1TB",          new BigDecimal("449.00"),  18, CategoriaProduto.ARMAZENAMENTO));
        repository.save(produto("Memória RAM DDR5 32GB", new BigDecimal("589.90"),  10, CategoriaProduto.COMPONENTES));
        repository.save(produto("Placa de Vídeo RTX",    new BigDecimal("3799.00"),  5, CategoriaProduto.COMPONENTES));

        log.info("DataLoader: {} produtos inseridos.", repository.count());
    }

    private Produto produto(String nome, BigDecimal preco, int estoque, CategoriaProduto categoria) {
        Produto p = Produto.novo(nome, SkuGenerator.fromNome(nome), preco);
        p.setEstoque(estoque);
        p.setCategoria(categoria);
        return p;
    }
}
