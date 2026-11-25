package br.com.nani.erp.domain.estoque;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Registro imutável do Razão de Estoque (Inventory Ledger - RF-EST-01 e ISO/IEC 27001/27002).
 * Impede alterações silenciosas de saldo e exige justificativa formal e operador em ajustes manuais.
 */
public record EstoqueMovimento(
        String idMovimento,
        String codigoItemOuLote,
        TipoMovimentoEstoque tipoMovimento,
        BigDecimal quantidade,
        String operadorResponsavelId,
        Instant dataHoraUtc,
        String justificativaAuditoria
) {
    public EstoqueMovimento {
        if (idMovimento == null || idMovimento.isBlank()) {
            throw new IllegalArgumentException("O identificador do movimento de estoque é obrigatório.");
        }
        if (codigoItemOuLote == null || codigoItemOuLote.isBlank()) {
            throw new IllegalArgumentException("O código do item ou lote movimentado é obrigatório.");
        }
        Objects.requireNonNull(tipoMovimento, "O tipo de movimento é obrigatório.");
        Objects.requireNonNull(quantidade, "A quantidade movimentada é obrigatória.");
        if (quantidade.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("A quantidade movimentada não pode ser zero.");
        }
        if (operadorResponsavelId == null || operadorResponsavelId.isBlank()) {
            throw new IllegalArgumentException("O identificador do operador responsável é obrigatório para auditoria.");
        }
        Objects.requireNonNull(dataHoraUtc, "O carimbo de tempo UTC da operação é obrigatório.");

        if (tipoMovimento == TipoMovimentoEstoque.AJUSTE_AUDITADO) {
            if (justificativaAuditoria == null || justificativaAuditoria.trim().length() < 10) {
                throw new IllegalArgumentException(
                        "Ajustes manuais de estoque exigem justificativa de auditoria com ao menos 10 caracteres."
                );
            }
        }
    }
}
