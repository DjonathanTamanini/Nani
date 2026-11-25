package br.com.nani.erp.domain.producao;

import br.com.nani.erp.domain.shared.Dinheiro;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Ficha Técnica de Receita de Conserva Artesanal (RF-PROD-01 e RF-PROD-02).
 * Concentra ingredientes, custo de embalagem por pote (vidro, tampa, rótulo, lacre),
 * parâmetros sanitários exigidos e cálculo determinístico de custo unitário real.
 */
public final class FichaTecnicaReceita {

    private final String codigoReceita;
    private final String nomeProduto;
    private final int rendimentoPadraoPotes;
    private final int prazoValidadeDias;
    private final ParametrosSanitarios parametrosSanitariosExigidos;
    private final Dinheiro custoEmbalagemUnitaria; // Vidro + Tampa + Rótulo + Lacre por pote
    private final List<ItemReceita> itensIngredientes;

    public FichaTecnicaReceita(
            String codigoReceita,
            String nomeProduto,
            int rendimentoPadraoPotes,
            int prazoValidadeDias,
            ParametrosSanitarios parametrosSanitariosExigidos,
            Dinheiro custoEmbalagemUnitaria,
            List<ItemReceita> itensIngredientes
    ) {
        if (codigoReceita == null || codigoReceita.isBlank()) {
            throw new IllegalArgumentException("O código da receita é obrigatório.");
        }
        if (nomeProduto == null || nomeProduto.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (rendimentoPadraoPotes <= 0) {
            throw new IllegalArgumentException("O rendimento padrão de potes deve ser maior que zero.");
        }
        if (prazoValidadeDias <= 0) {
            throw new IllegalArgumentException("O prazo de validade em dias deve ser maior que zero.");
        }
        this.parametrosSanitariosExigidos = Objects.requireNonNull(
                parametrosSanitariosExigidos, "Os parâmetros sanitários exigidos são obrigatórios."
        );
        this.custoEmbalagemUnitaria = Objects.requireNonNull(
                custoEmbalagemUnitaria, "O custo de embalagem unitária é obrigatório."
        );
        if (itensIngredientes == null || itensIngredientes.isEmpty()) {
            throw new IllegalArgumentException("A ficha técnica deve possuir ao menos um ingrediente.");
        }
        this.codigoReceita = codigoReceita;
        this.nomeProduto = nomeProduto;
        this.rendimentoPadraoPotes = rendimentoPadraoPotes;
        this.prazoValidadeDias = prazoValidadeDias;
        this.itensIngredientes = List.copyOf(itensIngredientes);
    }

    /**
     * Calcula o custo unitário real por pote aprovado em um lote (RF-PROD-01),
     * somando ingredientes (com perda técnica), embalagens utilizadas (incluindo quebras se houver)
     * e rateio de custos indiretos (gás, energia, água, sanitizantes).
     */
    public Dinheiro calcularCustoUnitarioRealDoLote(
            int totalPotesEnvasados,
            int potesAprovadosQualidade,
            Dinheiro rateioCustosIndiretosLote
    ) {
        if (totalPotesEnvasados <= 0 || potesAprovadosQualidade <= 0) {
            throw new IllegalArgumentException("A quantidade de potes envasados e aprovados deve ser maior que zero.");
        }
        if (potesAprovadosQualidade > totalPotesEnvasados) {
            throw new IllegalArgumentException("Potes aprovados não podem exceder o total envasado.");
        }
        Objects.requireNonNull(rateioCustosIndiretosLote, "O rateio de custos indiretos não pode ser nulo.");

        Dinheiro custoTotalIngredientes = itensIngredientes.stream()
                .map(ItemReceita::calcularCustoBrutoNaBatelada)
                .reduce(Dinheiro.ZERO, Dinheiro::somar);

        Dinheiro custoTotalEmbalagens = custoEmbalagemUnitaria.multiplicar(BigDecimal.valueOf(totalPotesEnvasados));

        Dinheiro custoTotalLote = custoTotalIngredientes
                .somar(custoTotalEmbalagens)
                .somar(rateioCustosIndiretosLote);

        return custoTotalLote.dividir(potesAprovadosQualidade);
    }

    public String getCodigoReceita() {
        return codigoReceita;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public int getRendimentoPadraoPotes() {
        return rendimentoPadraoPotes;
    }

    public int getPrazoValidadeDias() {
        return prazoValidadeDias;
    }

    public ParametrosSanitarios getParametrosSanitariosExigidos() {
        return parametrosSanitariosExigidos;
    }

    public Dinheiro getCustoEmbalagemUnitaria() {
        return custoEmbalagemUnitaria;
    }

    public List<ItemReceita> getItensIngredientes() {
        return itensIngredientes;
    }
}
