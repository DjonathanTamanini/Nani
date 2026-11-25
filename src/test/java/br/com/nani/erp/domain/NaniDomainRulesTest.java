package br.com.nani.erp.domain;

import br.com.nani.erp.domain.estoque.EstoqueMovimento;
import br.com.nani.erp.domain.estoque.TipoMovimentoEstoque;
import br.com.nani.erp.domain.producao.FichaTecnicaReceita;
import br.com.nani.erp.domain.producao.InsumoConsumidoLote;
import br.com.nani.erp.domain.producao.ItemReceita;
import br.com.nani.erp.domain.producao.LoteProducao;
import br.com.nani.erp.domain.producao.ParametrosSanitarios;
import br.com.nani.erp.domain.shared.Dinheiro;
import br.com.nani.erp.domain.vendas.Cliente;
import br.com.nani.erp.domain.vendas.PedidoVenda;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Suíte de Testes Unitários de Domínio (ISO/IEC/IEEE 29119) verificando os requisitos ISO/IEC/IEEE 29148.
 */
class NaniDomainRulesTest {

    private FichaTecnicaReceita criarReceitaPadraoPepinoAgridoce() {
        ParametrosSanitarios padraoSanitario = new ParametrosSanitarios(
                new BigDecimal("4.20"),
                new BigDecimal("85.0"),
                20
        );

        List<ItemReceita> ingredientes = List.of(
                // 10 kg líquidos de pepino com 10% de perda técnica = 11 kg brutos * R$ 6,00 = R$ 66,00
                new ItemReceita("INS-PEP-01", "Pepino Japonês Fresco",
                        new BigDecimal("10.0"), new BigDecimal("10.0"), Dinheiro.of("6.00")),
                // 5 litros de vinagre/salmoura sem perda = 5 * R$ 4,00 = R$ 20,00
                new ItemReceita("INS-VIN-01", "Salmoura e Vinagre de Álcool",
                        new BigDecimal("5.0"), BigDecimal.ZERO, Dinheiro.of("4.00")),
                // Especiarias (mostarda, endro, açúcar) = 1 kit * R$ 14,00 = R$ 14,00
                new ItemReceita("INS-ESP-01", "Mix de Especiarias",
                        BigDecimal.ONE, BigDecimal.ZERO, Dinheiro.of("14.00"))
        );
        // Custo total ingredientes = 66 + 20 + 14 = R$ 100,00
        // Embalagem (pote vidro 500ml + tampa + rótulo + lacre) = R$ 3,50 por unidade

        return new FichaTecnicaReceita(
                "REC-PEP-500",
                "Conserva de Pepino Agridoce 500g",
                20,
                365,
                padraoSanitario,
                Dinheiro.of("3.50"),
                ingredientes
        );
    }

    @Test
    @DisplayName("RF-PROD-01: Deve calcular custo unitário real determinístico com perdas, embalagens e rateio")
    void deveCalcularCustoUnitarioRealComPerdasEEmbalagens() {
        FichaTecnicaReceita receita = criarReceitaPadraoPepinoAgridoce();

        // 20 potes envasados (20 * R$ 3,50 = R$ 70,00 de embalagem)
        // Ingredientes = R$ 100,00
        // Rateio indireto (gás/energia/água) = R$ 20,00
        // Custo total lote = 100 + 70 + 20 = R$ 190,00
        // Se 19 potes foram aprovados no controle de qualidade (1 quebra térmica), custo unitário = 190 / 19 = R$ 10,0000
        Dinheiro custoUnitario = receita.calcularCustoUnitarioRealDoLote(20, 19, Dinheiro.of("20.00"));

        assertEquals(new BigDecimal("10.0000"), custoUnitario.valorCusto());
        assertEquals(new BigDecimal("10.00"), custoUnitario.valorComercial());
    }

    @Test
    @DisplayName("RF-PROD-02: Deve bloquear liberação de lote com pH inseguro (> 4.50) ou pasteurização insuficiente")
    void deveBloquearLiberacaoDeLoteComPhInseguroOuPasteurizacaoInsuficiente() {
        FichaTecnicaReceita receita = criarReceitaPadraoPepinoAgridoce();
        LoteProducao lote = new LoteProducao(
                "LOTE-202611-01",
                receita,
                LocalDate.of(2026, 11, 25),
                20,
                20,
                Dinheiro.of("20.00"),
                List.of(new InsumoConsumidoLote("INS-PEP-01", "FORN-LOTE-998", "12.345.678/0001-90", new BigDecimal("11.0")))
        );

        // Medição com pH 4.65 (acima do limite seguro de 4.20 da receita e 4.50 de segurança alimentar)
        ParametrosSanitarios medicaoPhInseguro = new ParametrosSanitarios(
                new BigDecimal("4.65"),
                new BigDecimal("86.0"),
                22
        );
        lote.registrarInspecaoSanitaria(medicaoPhInseguro);

        assertEquals(LoteProducao.StatusLote.REPROVADO_CONTROLE_SANITARIO, lote.getStatus());
        assertFalse(lote.podeSerVendidoEm(LocalDate.of(2026, 11, 26)));
        assertTrue(lote.getStatus().getRotuloAcessivelWcag().contains("BLOQUEADO"));
    }

    @Test
    @DisplayName("RF-EST-01: Deve exigir justificativa formal e operador em ajuste manual de estoque (ISO 27001)")
    void deveExigirJustificativaEOperadorEmAjusteManualDeEstoque() {
        assertThrows(IllegalArgumentException.class, () -> new EstoqueMovimento(
                "MOV-001",
                "LOTE-202611-01",
                TipoMovimentoEstoque.AJUSTE_AUDITADO,
                new BigDecimal("-1"),
                "op-djonathan",
                Instant.now(),
                "curta"
        ));

        EstoqueMovimento movimentoValido = new EstoqueMovimento(
                "MOV-002",
                "LOTE-202611-01",
                TipoMovimentoEstoque.AJUSTE_AUDITADO,
                new BigDecimal("-1"),
                "op-djonathan",
                Instant.now(),
                "Baixa auditada por quebra física de 1 pote durante inventário mensal"
        );
        assertEquals(TipoMovimentoEstoque.AJUSTE_AUDITADO, movimentoValido.tipoMovimento());
    }

    @Test
    @DisplayName("RF-VEN-01 e RF-PROD-03: Deve bloquear venda de lote vencido e garantir rastreabilidade bidirecional")
    void deveGarantirRastreabilidadeBidirecionalEBloquearLoteVencido() {
        FichaTecnicaReceita receita = criarReceitaPadraoPepinoAgridoce();
        LoteProducao lote = new LoteProducao(
                "LOTE-202611-02",
                receita,
                LocalDate.of(2026, 11, 25),
                20,
                20,
                Dinheiro.of("20.00"),
                List.of(new InsumoConsumidoLote("INS-PEP-01", "LOTE-FORNECEDOR-ABC", "12.345.678/0001-90", new BigDecimal("11.0")))
        );

        lote.registrarInspecaoSanitaria(new ParametrosSanitarios(
                new BigDecimal("4.10"),
                new BigDecimal("86.0"),
                20
        ));

        Cliente cliente = new Cliente("CLI-001", "Maria Oliveira", "111.222.333-44", "maria@exemplo.com", "47999990000");
        PedidoVenda pedidoValido = new PedidoVenda("PED-2026-001", cliente, LocalDate.of(2026, 12, 1));
        pedidoValido.adicionarItemDeLote(lote, 3, Dinheiro.of("24.00"));

        // Verifica rastreabilidade bidirecional: Fornecedor -> LoteProducao -> PedidoVenda
        assertTrue(lote.utilizouLoteFornecedor("LOTE-FORNECEDOR-ABC"));
        assertTrue(pedidoValido.contemLoteProducao("LOTE-202611-02"));
        assertEquals(new BigDecimal("72.00"), pedidoValido.calcularValorTotalPedido().valorComercial());

        // Tentativa de venda após a data de validade (365 dias após 25/11/2026)
        PedidoVenda pedidoComLoteVencido = new PedidoVenda("PED-2028-001", cliente, LocalDate.of(2027, 12, 1));
        assertThrows(IllegalStateException.class,
                () -> pedidoComLoteVencido.adicionarItemDeLote(lote, 1, Dinheiro.of("24.00")));
    }

    @Test
    @DisplayName("RF-LGPD-01: Deve pseudonimizar dados pessoais do cliente preservando rastreabilidade fiscal e de lote")
    void devePseudonimizarDadosPessoaisMantendoRastreabilidadeDoPedido() {
        FichaTecnicaReceita receita = criarReceitaPadraoPepinoAgridoce();
        LoteProducao lote = new LoteProducao(
                "LOTE-202611-03",
                receita,
                LocalDate.of(2026, 11, 25),
                20,
                20,
                Dinheiro.of("20.00"),
                List.of(new InsumoConsumidoLote("INS-PEP-01", "LOTE-FORN-777", "12.345.678/0001-90", new BigDecimal("11.0")))
        );
        lote.registrarInspecaoSanitaria(new ParametrosSanitarios(new BigDecimal("4.05"), new BigDecimal("85.0"), 20));

        Cliente cliente = new Cliente("CLI-LGPD-01", "João da Silva", "999.888.777-66", "joao@exemplo.com", "48988887777");
        PedidoVenda pedido = new PedidoVenda("PED-2026-050", cliente, LocalDate.of(2026, 11, 26));
        pedido.adicionarItemDeLote(lote, 2, Dinheiro.of("25.00"));

        cliente.pseudonimizarParaLgpd(Instant.parse("2026-11-25T14:00:00Z"));

        assertTrue(cliente.isDadosPseudonimizadosLgpd());
        assertNotEquals("João da Silva", cliente.getNomeCompleto());
        assertEquals("ANONIMIZADO", cliente.getDocumentoFiscal());
        // Vínculo com o pedido e lote continua íntegro para obrigações fiscais e sanitárias
        assertEquals(cliente.getClienteId(), pedido.getClienteId());
        assertTrue(pedido.contemLoteProducao("LOTE-202611-03"));
    }
}
