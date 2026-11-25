package br.com.nani.erp.domain.producao;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Registro de rastreabilidade que vincula um lote de matéria-prima/embalagem do fornecedor
 * a um lote de produção de conserva (RF-PROD-03).
 */
public record InsumoConsumidoLote(
        String codigoInsumo,
        String codigoLoteFornecedor,
        String cnpjOuIdentificadorFornecedor,
        BigDecimal quantidadeConsumida
) {
    public InsumoConsumidoLote {
        if (codigoInsumo == null || codigoInsumo.isBlank()) {
            throw new IllegalArgumentException("O código do insumo consumido é obrigatório.");
        }
        if (codigoLoteFornecedor == null || codigoLoteFornecedor.isBlank()) {
            throw new IllegalArgumentException("O código do lote do fornecedor é obrigatório para rastreabilidade.");
        }
        if (cnpjOuIdentificadorFornecedor == null || cnpjOuIdentificadorFornecedor.isBlank()) {
            throw new IllegalArgumentException("O identificador do fornecedor é obrigatório.");
        }
        Objects.requireNonNull(quantidadeConsumida, "A quantidade consumida é obrigatória.");
        if (quantidadeConsumida.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade consumida deve ser maior que zero.");
        }
    }
}
