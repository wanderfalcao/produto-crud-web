package br.com.infnet.domain.exception;

import java.util.UUID;

public class ProdutoNaoEncontradoException extends RuntimeException {
    public ProdutoNaoEncontradoException(UUID id) {
        super("Produto nao encontrado: " + id);
    }
}
