package br.com.nani.erp.domain.vendas;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Entidade de Cliente com separação entre identificador transacional/fiscal e Dados Pessoais (PII)
 * para conformidade simultânea com a LGPD (Lei 13.709/2018) e retenção de rastreabilidade (RF-LGPD-01).
 */
public final class Cliente {

    private final String clienteId;
    private String nomeCompleto;
    private String documentoFiscal;
    private String emailContato;
    private String telefoneContato;
    private boolean dadosPseudonimizadosLgpd;
    private Instant dataHoraPseudonimizacaoUtc;

    public Cliente(
            String clienteId,
            String nomeCompleto,
            String documentoFiscal,
            String emailContato,
            String telefoneContato
    ) {
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("O identificador interno do cliente é obrigatório.");
        }
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        this.clienteId = clienteId;
        this.nomeCompleto = nomeCompleto;
        this.documentoFiscal = documentoFiscal;
        this.emailContato = emailContato;
        this.telefoneContato = telefoneContato;
        this.dadosPseudonimizadosLgpd = false;
    }

    /**
     * Aplica pseudonimização irreversível aos dados pessoais identificáveis (Art. 18, VI da LGPD),
     * mantendo o clienteId íntegro para não quebrar a rastreabilidade de lotes vendidos nem o livro caixa.
     */
    public void pseudonimizarParaLgpd(Instant momentoSolicitacaoUtc) {
        Objects.requireNonNull(momentoSolicitacaoUtc, "A data/hora UTC da pseudonimização é obrigatória.");
        String hashIdentificador = gerarHashSha256(clienteId + ":" + momentoSolicitacaoUtc.toString());
        this.nomeCompleto = "TITULAR_ANONIMIZADO_LGPD_" + hashIdentificador.substring(0, 12);
        this.documentoFiscal = "ANONIMIZADO";
        this.emailContato = "anonimizado+" + hashIdentificador.substring(0, 12) + "@privacidade.nani.local";
        this.telefoneContato = "ANONIMIZADO";
        this.dadosPseudonimizadosLgpd = true;
        this.dataHoraPseudonimizacaoUtc = momentoSolicitacaoUtc;
    }

    private static String gerarHashSha256(String entrada) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(entrada.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível no ambiente Java.", e);
        }
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getDocumentoFiscal() {
        return documentoFiscal;
    }

    public String getEmailContato() {
        return emailContato;
    }

    public String getTelefoneContato() {
        return telefoneContato;
    }

    public boolean isDadosPseudonimizadosLgpd() {
        return dadosPseudonimizadosLgpd;
    }

    public Instant getDataHoraPseudonimizacaoUtc() {
        return dataHoraPseudonimizacaoUtc;
    }
}
