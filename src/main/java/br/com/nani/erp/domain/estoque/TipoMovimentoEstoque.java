package br.com.nani.erp.domain.estoque;

/**
 * Tipos de movimentação no razão imutável de estoque (RF-EST-01).
 */
public enum TipoMovimentoEstoque {
    ENTRADA_COMPRA,
    ENTRADA_LOTE_PRODUZIDO,
    CONSUMO_PRODUCAO,
    RESERVA_PEDIDO,
    BAIXA_VENDA,
    AJUSTE_AUDITADO
}
