package br.com.nani.erp.domain.producao;

import br.com.nani.erp.domain.shared.Dinheiro;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Agregado de Lote de Produção de Conservas (RF-PROD-01, RF-PROD-02, RF-PROD-03, RNF-ACC-01).
 * Garante rastreabilidade bidirecional, validação sanitária antes da venda e cálculo do custo unitário real.
 */
public final class LoteProducao {

    public enum StatusLote {
        EM_QUARENTENA_QUALIDADE("[QUARENTENA - AGUARDANDO INSPEÇÃO SANITÁRIA]"),
        LIBERADO_PARA_VENDA("[APROVADO - DISPONÍVEL PARA VENDA]"),
        REPROVADO_CONTROLE_SANITARIO("[BLOQUEADO - REPROVADO NO CONTROLE SANITÁRIO]"),
        BLOQUEADO_PARA_RECALL("[CRÍTICO - LOTE BLOQUEADO PARA RECALL]");

        private final String rotuloAcessivelWcag;

        StatusLote(String rotuloAcessivelWcag) {
            this.rotuloAcessivelWcag = rotuloAcessivelWcag;
        }

        public String getRotuloAcessivelWcag() {
            return rotuloAcessivelWcag;
        }
    }

    private final String codigoLote;
    private final FichaTecnicaReceita receita;
    private final LocalDate dataFabricacao;
    private final LocalDate dataValidade;
    private final int totalPotesEnvasados;
    private final int potesAprovadosQualidade;
    private final Dinheiro custoUnitarioReal;
    private final List<InsumoConsumidoLote> insumosRastreados;
    private ParametrosSanitarios parametrosAferidos;
    private StatusLote status;

    public LoteProducao(
            String codigoLote,
            FichaTecnicaReceita receita,
            LocalDate dataFabricacao,
            int totalPotesEnvasados,
            int potesAprovadosQualidade,
            Dinheiro rateioCustosIndiretosLote,
            List<InsumoConsumidoLote> insumosRastreados
    ) {
        if (codigoLote == null || codigoLote.isBlank()) {
            throw new IllegalArgumentException("O código do lote de produção é obrigatório.");
        }
        this.receita = Objects.requireNonNull(receita, "A ficha técnica da receita é obrigatória.");
        this.dataFabricacao = Objects.requireNonNull(dataFabricacao, "A data de fabricação é obrigatória.");
        if (insumosRastreados == null || insumosRastreados.isEmpty()) {
            throw new IllegalArgumentException("A rastreabilidade exige o vínculo com os lotes de insumos consumidos.");
        }
        this.codigoLote = codigoLote;
        this.dataValidade = dataFabricacao.plusDays(receita.getPrazoValidadeDias());
        this.totalPotesEnvasados = totalPotesEnvasados;
        this.potesAprovadosQualidade = potesAprovadosQualidade;
        this.custoUnitarioReal = receita.calcularCustoUnitarioRealDoLote(
                totalPotesEnvasados, potesAprovadosQualidade, rateioCustosIndiretosLote
        );
        this.insumosRastreados = List.copyOf(insumosRastreados);
        this.status = StatusLote.EM_QUARENTENA_QUALIDADE;
    }

    /**
     * Registra a medição sanitária do lote (pH e pasteurização) e libera ou reprova o lote (RF-PROD-02).
     */
    public void registrarInspecaoSanitaria(ParametrosSanitarios medicaoReal) {
        Objects.requireNonNull(medicaoReal, "A medição sanitária não pode ser nula.");
        this.parametrosAferidos = medicaoReal;

        if (medicaoReal.atendeRequisitosSeguranca(receita.getParametrosSanitariosExigidos())) {
            this.status = StatusLote.LIBERADO_PARA_VENDA;
        } else {
            this.status = StatusLote.REPROVADO_CONTROLE_SANITARIO;
        }
    }

    public void acionarRecallSanitario() {
        this.status = StatusLote.BLOQUEADO_PARA_RECALL;
    }

    public boolean isVencidoEm(LocalDate dataReferencia) {
        Objects.requireNonNull(dataReferencia, "A data de referência é obrigatória.");
        return !dataValidade.isAfter(dataReferencia);
    }

    public boolean podeSerVendidoEm(LocalDate dataVenda) {
        return status == StatusLote.LIBERADO_PARA_VENDA && !isVencidoEm(dataVenda);
    }

    public boolean utilizouLoteFornecedor(String codigoLoteFornecedor) {
        return insumosRastreados.stream()
                .anyMatch(item -> item.codigoLoteFornecedor().equalsIgnoreCase(codigoLoteFornecedor));
    }

    public String getCodigoLote() {
        return codigoLote;
    }

    public FichaTecnicaReceita getReceita() {
        return receita;
    }

    public LocalDate getDataFabricacao() {
        return dataFabricacao;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public int getTotalPotesEnvasados() {
        return totalPotesEnvasados;
    }

    public int getPotesAprovadosQualidade() {
        return potesAprovadosQualidade;
    }

    public Dinheiro getCustoUnitarioReal() {
        return custoUnitarioReal;
    }

    public List<InsumoConsumidoLote> getInsumosRastreados() {
        return insumosRastreados;
    }

    public ParametrosSanitarios getParametrosAferidos() {
        return parametrosAferidos;
    }

    public StatusLote getStatus() {
        return status;
    }
}
