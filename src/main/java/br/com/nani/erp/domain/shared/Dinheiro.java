package br.com.nani.erp.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object imutável para representação monetária determinística (RNF-QUAL-01 / ISO 25010).
 * Mantém 4 casas decimais para cálculos intermediários de custo unitário e 2 casas para valor comercial.
 */
public final class Dinheiro {

    public static final int ESCALA_CUSTO = 4;
    public static final int ESCALA_COMERCIAL = 2;
    public static final RoundingMode MODO_ARREDONDAMENTO = RoundingMode.HALF_EVEN;
    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    private final BigDecimal valor;

    private Dinheiro(BigDecimal valor) {
        Objects.requireNonNull(valor, "O valor monetário não pode ser nulo.");
        this.valor = valor.setScale(ESCALA_CUSTO, MODO_ARREDONDAMENTO);
    }

    public static Dinheiro of(BigDecimal valor) {
        return new Dinheiro(valor);
    }

    public static Dinheiro of(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public Dinheiro somar(Dinheiro outro) {
        Objects.requireNonNull(outro, "Valor a somar não pode ser nulo.");
        return new Dinheiro(this.valor.add(outro.valor));
    }

    public Dinheiro multiplicar(BigDecimal fator) {
        Objects.requireNonNull(fator, "Fator de multiplicação não pode ser nulo.");
        return new Dinheiro(this.valor.multiply(fator));
    }

    public Dinheiro dividir(int quantidadeUnidades) {
        if (quantidadeUnidades <= 0) {
            throw new IllegalArgumentException("A quantidade de unidades para rateio deve ser maior que zero.");
        }
        return new Dinheiro(this.valor.divide(BigDecimal.valueOf(quantidadeUnidades), ESCALA_CUSTO, MODO_ARREDONDAMENTO));
    }

    public BigDecimal valorCusto() {
        return valor;
    }

    public BigDecimal valorComercial() {
        return valor.setScale(ESCALA_COMERCIAL, MODO_ARREDONDAMENTO);
    }

    public boolean isNegativo() {
        return valor.compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dinheiro dinheiro)) return false;
        return valor.compareTo(dinheiro.valor) == 0;
    }

    @Override
    public int hashCode() {
        return valor.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return "R$ " + valorComercial().toPlainString();
    }
}
