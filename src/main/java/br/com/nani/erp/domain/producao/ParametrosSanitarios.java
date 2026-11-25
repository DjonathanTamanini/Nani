package br.com.nani.erp.domain.producao;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Parâmetros críticos de segurança alimentar aferidos ou exigidos para conservas artesanais (RF-PROD-02).
 * Previne risco biológico (ex.: Clostridium botulinum em conservas acidificadas cujo limite seguro é pH <= 4.50).
 */
public record ParametrosSanitarios(
        BigDecimal phEquilibrio,
        BigDecimal temperaturaPasteurizacaoCelsius,
        int tempoPasteurizacaoMinutos
) {
    public static final BigDecimal LIMITE_MAXIMO_PH_ACIDIFICADO = new BigDecimal("4.50");

    public ParametrosSanitarios {
        Objects.requireNonNull(phEquilibrio, "O pH de equilíbrio é obrigatório.");
        Objects.requireNonNull(temperaturaPasteurizacaoCelsius, "A temperatura de pasteurização é obrigatória.");
        if (phEquilibrio.compareTo(BigDecimal.ZERO) <= 0 || phEquilibrio.compareTo(new BigDecimal("14.0")) > 0) {
            throw new IllegalArgumentException("Valor de pH fora da escala química válida (0 a 14).");
        }
        if (temperaturaPasteurizacaoCelsius.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A temperatura de pasteurização deve ser positiva.");
        }
        if (tempoPasteurizacaoMinutos <= 0) {
            throw new IllegalArgumentException("O tempo de pasteurização em minutos deve ser maior que zero.");
        }
    }

    /**
     * Verifica se os parâmetros aferidos em um lote atendem aos limites de segurança da receita.
     */
    public boolean atendeRequisitosSeguranca(ParametrosSanitarios padraoExigidoReceita) {
        Objects.requireNonNull(padraoExigidoReceita, "O padrão sanitário da receita é obrigatório.");
        boolean phSeguro = this.phEquilibrio.compareTo(padraoExigidoReceita.phEquilibrio) <= 0
                && this.phEquilibrio.compareTo(LIMITE_MAXIMO_PH_ACIDIFICADO) <= 0;
        boolean temperaturaSegura = this.temperaturaPasteurizacaoCelsius
                .compareTo(padraoExigidoReceita.temperaturaPasteurizacaoCelsius) >= 0;
        boolean tempoSeguro = this.tempoPasteurizacaoMinutos >= padraoExigidoReceita.tempoPasteurizacaoMinutos;

        return phSeguro && temperaturaSegura && tempoSeguro;
    }
}
