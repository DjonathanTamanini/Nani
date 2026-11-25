# Nani ERP — Gestão de Produção, Rastreabilidade e Venda de Conservas Artesanais

ERP em **Java 21 (LTS)** estruturado em **Arquitetura Hexagonal / Clean Architecture** para controle rigoroso de produção em lotes, ficha técnica e custo unitário real, razão imutável de estoque (*inventory ledger*), controle sanitário e vendas de conservas artesanais, com conformidade normativa verificável e proporcional.

---

## 1. Arquitetura do Sistema (Java LTS + Clean / Hexagonal)

```text
Nani/
├── pom.xml                                      # Build Maven (Java 21, JUnit 5, CycloneDX SBOM)
├── docs/                                        # Engenharia de Requisitos, Arquitetura e Conformidade
│   ├── ARQUITETURA_E_CONFORMIDADE.md            # Recorte normativo proporcional, ADRs e Matriz Legal/ISO
│   ├── REQUISITOS_ISO_29148.md                  # Especificação formal de requisitos (ISO/IEC/IEEE 29148)
│   └── PLANO_TESTES_ISO_29119.md                # Estratégia e critérios de teste (ISO/IEC/IEEE 29119)
├── dados/                                       # Artefatos operacionais, cargas iniciais e backups locais
│   ├── producao/
│   ├── estoque/
│   └── vendas/
└── src/
    ├── main/java/br/com/nani/erp/
    │   ├── domain/                              # Núcleo puro (sem dependências de framework)
    │   │   ├── shared/                          # Value Objects (Dinheiro, AuditoriaOperacao)
    │   │   ├── producao/                        # Receita, ParametrosSanitarios, LoteProducao, Insumo
    │   │   ├── estoque/                         # EstoqueMovimento (Ledger imutável), TipoMovimentoEstoque
    │   │   └── vendas/                          # Cliente (com pseudonimização LGPD), PedidoVenda
    │   ├── application/                         # Casos de uso, portas de entrada/saída e orquestração transacional
    │   ├── infrastructure/                      # Adaptadores de persistência relacional, segurança (RBAC) e logs
    │   └── interfaces/                          # APIs REST e UI acessível (WCAG 2.1 AA / LBI)
    └── test/java/br/com/nani/erp/
        └── domain/                              # Testes unitários determinísticos (ISO/IEC/IEEE 29119)
```

---

## 2. Pilares de Engenharia e Resolução de Lacunas Críticas

1. **Governança Normativa Proporcional:** Aplicação enxuta e automatizada das normas **ISO/IEC 29148, 12207, 25010, 29119, 27001/27002 e 27701**, evitando documentação morta por meio de *compliance-as-code* (regras no domínio, testes automatizados, SBOM no build e trilhas de auditoria no banco).
2. **Segurança Alimentar e Rastreabilidade Bidirecional:** Controle de parâmetros críticos por lote (**pH de equilíbrio $\le 4,5$**, tempo/temperatura de pasteurização e concentração de salmoura) e vínculo bidirecional: `Lote do Fornecedor de Insumo` $\rightarrow$ `Lote de Produção da Conserva` $\rightarrow$ `Pedido / Cliente` (permitindo *recall* imediato).
3. **Custo Unitário Real Determinístico:** Apuração exata com `BigDecimal` contemplando matérias-primas, embalagens (pote de vidro, tampa metálica, rótulo, lacre), percentual de perda técnica e rateio de custos indiretos (gás/energia/água) dividido pelo rendimento real envasado.
4. **Desacoplamento entre LGPD (Lei 13.709/2018) e Retenção Fiscal/Sanitária:** Separação entre dados identificáveis de contato/marketing e registros fiscais/rastreabilidade de lotes via **pseudonimização irreversível**, preservando a integridade contábil e de *recall* sem violar o direito de eliminação do titular.
5. **Acessibilidade Real em Fluxos Críticos (LBI Lei 13.146/2015 e WCAG 2.1 AA):** Interfaces de operação desenhadas para navegação integral por teclado, semântica ARIA em tabelas de lotes/estoque, contraste mínimo 4.5:1 e alertas visuais não dependentes exclusivamente de cor (uso combinado de ícone, texto e padrão visual para "Lote Vencido", "pH Fora da Faixa" ou "Estoque Crítico").
6. **Conformidade de Licenciamento e Propriedade Intelectual (Leis 9.609/1998 e 9.610/1998):** Inventário automatizado de dependências transitivas (SBOM) para impedir contaminação por licenças incompatíveis e registro explícito de titularidade.
