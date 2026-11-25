package br.com.nani.erp.domain.producao;

import br.com.nani.erp.domain.shared.Dinheiro;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Item que compõe a ficha técnica de uma receita de conserva (ingrediente + perda técnica esperada).
 */
public record ItemReceita(
        String codigoInsumo,
        String nomeInsumo,
        BigDecimal quantidadeLiquidaPorBatelada,
        BigDecimal percentualPerdaTecnica,
        Dinheiro custoPorUnidadeMedida
) {
    public ItemReceita {
        if (codigoInsumo == null || codigoInsumo.isBlank()) {
            throw new IllegalArgumentException("O código do insumo é obrigatório.");
        }
        if (nomeInsumo == null || nomeInsumo.isBlank()) {
            throw new IllegalArgumentException("O nome do insumo é obrigatório.");
        }
        Objects.requireNonNull(quantidadeLiquidaPorBatelada, "A quantidade líquida é obrigatória.");
        Objects.requireNonNull(percentualPerdaTecnica, "O percentual de perda técnica é obrigatório.");
        Objects.requireNonNull(custoPorUnidadeMedida, "O custo por unidade de medida é obrigatório.");

        if (quantidadeLiquidaPorBatelada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade líquida do insumo deve ser maior que zero.");
        }
        if (percentualPerdaTecnica.compareTo(BigDecimal.ZERO) < 0
                || percentualPerdaTecnica.compareTo(new BigDecimal("100")) >= 0) {
            throw new IllegalArgumentException("O percentual de perda técnica deve estar entre 0 e 99.99%.");
        }
        if (custoPorUnidadeMedida.isNegativo()) {
            throw new IllegalArgumentException("O custo do insumo não pode ser negativo.");
        }
    }

    /**
     * Calcula o custo bruto do insumo na batelada aplicando o fator de perda técnica:
     * Quantidade Bruta = Quantidade Líquida * (1 + percentualPerda / 100).
     */
    public Dinheiro calcularCustoBrutoNaBatelada() {
        BigDecimal fatorPerda = BigDecimal.ONE.add(
                percentualPerdaTecnica.divide(new BigDecimal("100"), 6, Dinheiro.MODO_ARREDONDAMENTO)
        );
        BigDecimal quantidadeBruta = quantidadeLiquidaPorBatelada.multiply(fatorPerda);
        return custoPorUnidadeMedida.multiplicar(quantidadeBruta);
    }
}
