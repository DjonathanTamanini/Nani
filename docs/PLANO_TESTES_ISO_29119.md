# Plano e Estratégia de Testes (ISO/IEC/IEEE 29119) — Nani ERP

## 1. Escopo e Níveis de Teste

| Nível de Teste | Foco de Verificação | Ferramental | Critério de Aceite |
| :--- | :--- | :--- | :--- |
| **Unitário de Domínio** | Cálculos de custo unitário (`BigDecimal`), bloqueios sanitários (pH, pasteurização, validade), ledger de estoque e pseudonimização LGPD | JUnit 5 | 100% de aprovação nas invariantes de domínio sem dependência de banco ou rede |
| **Integração e Transação** | Persistência atômica de baixa de estoque + criação de lote + geração de pedido com *optimistic locking* | JUnit 5 + Testcontainers / JDBC | Zero condição de corrida (*race condition*) em saldo de estoque |
| **Segurança e Auditoria (27001)** | Rejeição de ajustes de estoque sem operador/justificativa e validação de perfis RBAC | Testes de caso de uso e API | Bloqueio integral de operações não autenticadas ou sem justificativa |
| **Acessibilidade (WCAG 2.1 AA / LBI)** | Navegação por teclado, foco semântico, contraste $\ge 4.5:1$ e mensagens textuais de status | axe-core / Playwright + revisão de contratos de UI | Zero violação crítica ou séria de WCAG 2.1 Nível AA nos fluxos de produção e venda |

## 2. Casos de Teste Base Implementados (`NaniDomainRulesTest`)

1. **CT-01 (`RF-PROD-01`):** Validação matemática do custo unitário real de um lote de conserva considerando insumos, perda técnica, embalagens (vidro, tampa, rótulo) e rateio de custos indiretos.
2. **CT-02 (`RF-PROD-02`):** Bloqueio automático de aprovação de lote quando o pH medido ultrapassa $4,50$ ou a pasteurização não atinge o tempo/temperatura exigidos.
3. **CT-03 (`RF-EST-01`):** Garantia de imutabilidade do movimento de estoque e obrigatoriedade de justificativa auditável em ajustes manuais.
4. **CT-04 (`RF-VEN-01` & `RF-PROD-03`):** Bloqueio de venda de lote vencido e verificação da cadeia de rastreabilidade bidirecional (fornecedor de insumo $\rightarrow$ lote $\rightarrow$ pedido).
5. **CT-05 (`RF-LGPD-01`):** Pseudonimização dos dados pessoais do cliente garantindo que o histórico de lotes vendidos permaneça íntegro para fins fiscais e de recall sanitário.
