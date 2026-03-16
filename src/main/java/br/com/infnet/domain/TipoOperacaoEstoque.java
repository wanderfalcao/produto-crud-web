package br.com.infnet.domain;

public enum TipoOperacaoEstoque {
    ENTRADA,  // aumenta o estoque (reposição, devolução)
    SAIDA     // diminui o estoque (venda, pedido confirmado)
}
