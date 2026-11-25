package br.com.nani.erp.domain.vendas;

import br.com.nani.erp.domain.producao.LoteProducao;
import br.com.nani.erp.domain.shared.Dinheiro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Agregado de Pedido de Venda com transparência ao consumidor (CDC Lei 8.078/1990)
 * e vínculo obrigatório com LoteProducao para rastreabilidade e bloqueio de lotes vencidos/reprovados (RF-VEN-01).
 */
public final class PedidoVenda {

    public record ItemPedidoVenda(
            String codigoLoteProducao,
            String nomeProduto,
            LocalDate dataValidadeLote,
            int quantidadePotes,
            Dinheiro precoUnitarioVenda,
            Dinheiro custoUnitarioApurado
    ) {
        public Dinheiro calcularSubtotalVenda() {
            return precoUnitarioVenda.multiplicar(BigDecimal.valueOf(quantidadePotes));
        }

        public Dinheiro calcularSubtotalCusto() {
            return custoUnitarioApurado.multiplicar(BigDecimal.valueOf(quantidadePotes));
        }
    }

    private final String codigoPedido;
    private final String clienteId;
    private final LocalDate dataVenda;
    private final List<ItemPedidoVenda> itens = new ArrayList<>();

    public PedidoVenda(String codigoPedido, Cliente cliente, LocalDate dataVenda) {
        if (codigoPedido == null || codigoPedido.isBlank()) {
            throw new IllegalArgumentException("O código do pedido é obrigatório.");
        }
        Objects.requireNonNull(cliente, "O cliente do pedido é obrigatório.");
        Objects.requireNonNull(dataVenda, "A data da venda é obrigatória.");
        this.codigoPedido = codigoPedido;
        this.clienteId = cliente.getClienteId();
        this.dataVenda = dataVenda;
    }

    /**
     * Adiciona um item de conserva ao pedido validando se o lote está aprovado no controle sanitário
     * e dentro do prazo de validade na data da venda (RF-VEN-01).
     */
    public void adicionarItemDeLote(LoteProducao lote, int quantidadePotes, Dinheiro precoUnitarioVenda) {
        Objects.requireNonNull(lote, "O lote de produção é obrigatório.");
        Objects.requireNonNull(precoUnitarioVenda, "O preço unitário de venda é obrigatório.");
        if (quantidadePotes <= 0) {
            throw new IllegalArgumentException("A quantidade de potes vendida deve ser maior que zero.");
        }
        if (!lote.podeSerVendidoEm(dataVenda)) {
            throw new IllegalStateException(
                    "Venda bloqueada para o lote " + lote.getCodigoLote()
                            + " - Status: " + lote.getStatus().getRotuloAcessivelWcag()
                            + ", Validade: " + lote.getDataValidade()
            );
        }

        itens.add(new ItemPedidoVenda(
                lote.getCodigoLote(),
                lote.getReceita().getNomeProduto(),
                lote.getDataValidade(),
                quantidadePotes,
                precoUnitarioVenda,
                lote.getCustoUnitarioReal()
        ));
    }

    public Dinheiro calcularValorTotalPedido() {
        return itens.stream()
                .map(ItemPedidoVenda::calcularSubtotalVenda)
                .reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public Dinheiro calcularCustoTotalMercadoriaVendida() {
        return itens.stream()
                .map(ItemPedidoVenda::calcularSubtotalCusto)
                .reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public boolean contemLoteProducao(String codigoLoteProducao) {
        return itens.stream()
                .anyMatch(item -> item.codigoLoteProducao().equalsIgnoreCase(codigoLoteProducao));
    }

    public String getCodigoPedido() {
        return codigoPedido;
    }

    public String getClienteId() {
        return clienteId;
    }

    public LocalDate getDataVenda() {
        return dataVenda;
    }

    public List<ItemPedidoVenda> getItens() {
        return List.copyOf(itens);
    }
}
