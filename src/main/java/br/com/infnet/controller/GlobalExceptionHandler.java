package br.com.infnet.controller;

import br.com.infnet.domain.exception.DomainException;
import br.com.infnet.domain.exception.ProdutoNaoEncontradoException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Centraliza o tratamento de exceções de domínio para toda a camada web,
 * redirecionando o usuário à lista com mensagem de erro legível.
 * DomainException lançada dentro de try/catch no controller não chega aqui;
 * este handler atua como fallback para exceções não capturadas localmente.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public String handleNaoEncontrado(ProdutoNaoEncontradoException ex, RedirectAttributes attrs) {
        attrs.addFlashAttribute("erro", ex.getMessage());
        return "redirect:/produtos";
    }

    @ExceptionHandler(DomainException.class)
    public String handleDomain(DomainException ex, RedirectAttributes attrs) {
        attrs.addFlashAttribute("erro", ex.getMessage());
        return "redirect:/produtos";
    }
}
