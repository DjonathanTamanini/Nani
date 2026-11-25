# Especificação de Requisitos do Sistema (ISO/IEC/IEEE 29148) — Nani ERP

Todos os requisitos abaixo seguem os critérios da **ISO/IEC/IEEE 29148**: são singulares, não ambíguos, completos, consistentes, classificáveis e verificáveis por teste automatizado.

---

## 1. Requisitos Funcionais (RF)

### RF-PROD-01 — Cálculo Determinístico de Custo Unitário Real por Pote
- **Descrição:** O sistema deve calcular o custo unitário real de cada pote produzido a partir da `FichaTecnicaReceita`, somando:
  1. Custo total dos ingredientes brutos ajustado pelo fator de perda técnica;
  2. Custo unitário de embalagem (pote de vidro + tampa + rótulo + lacre) multiplicado pelo rendimento;
  3. Rateio de custos indiretos do lote (gás, energia elétrica, água, sanitizantes);
  dividindo o custo total do lote pela quantidade real de unidades aprovadas no controle de qualidade, com precisão interna de 4 casas decimais e arredondamento comercial de 2 casas (`RoundingMode.HALF_EVEN`).
- **Critério de Verificação:** Teste unitário `deveCalcularCustoUnitarioRealComPerdasEEmbalagens()`.

### RF-PROD-02 — Bloqueio Sanitário de Lote Fora dos Parâmetros de Segurança Alimentar
- **Descrição:** O sistema deve impedir a transição de um `LoteProducao` para o status `LIBERADO_PARA_VENDA` caso:
  - O pH de equilíbrio aferido seja superior ao limite máximo de segurança da receita (padrão $\le 4,50$ para conservas acidificadas); ou
  - A temperatura ou o tempo de pasteurização aferidos sejam inferiores aos mínimos definidos na `FichaTecnicaReceita`.
- **Critério de Verificação:** Teste unitário `deveBloquearLiberacaoDeLoteComPhInseguroOuPasteurizacaoInsuficiente()`.

### RF-PROD-03 — Rastreabilidade Bidirecional de Lote e Insumos
- **Descrição:** Todo `LoteProducao` deve registrar obrigatoriamente a lista de `InsumoConsumidoLote` contendo o identificador do insumo, o código do lote do fornecedor e a quantidade utilizada, permitindo rastrear quais lotes de conserva utilizaram determinado lote de matéria-prima e quais pedidos de clientes receberam unidades daquele lote.
- **Critério de Verificação:** Teste unitário `deveGarantirRastreabilidadeBidirecionalEntreInsumoLoteEPedido()`.

### RF-EST-01 — Razão Imutável de Movimentação de Estoque (Inventory Ledger)
- **Descrição:** Nenhuma quantidade de estoque pode ser sobrescrita sem registro de `EstoqueMovimento`. Todo movimento de ajuste manual (`AJUSTE_AUDITADO`) exige obrigatoriamente identificador do operador responsável, data/hora UTC e justificativa textual com no mínimo 10 caracteres.
- **Critério de Verificação:** Teste unitário `deveExigirJustificativaEOperadorEmAjusteManualDeEstoque()`.

### RF-VEN-01 — Bloqueio de Venda de Lote Vencido ou Não Liberado (CDC / Segurança Alimentar)
- **Descrição:** O sistema deve rejeitar a inclusão ou baixa de itens em `PedidoVenda` cujo `LoteProducao` associado esteja com status diferente de `LIBERADO_PARA_VENDA` ou cuja data de validade seja anterior ou igual à data da venda.
- **Critério de Verificação:** Teste unitário `deveImpedirVendaDeLoteVencidoOuEmQuarentena()`.

### RF-LGPD-01 — Pseudonimização de Titular com Preservação de Rastreabilidade Sanitária e Fiscal
- **Descrição:** Ao processar uma solicitação válida de eliminação/anonimização de dados pessoais de um `Cliente` (Lei 13.709/2018), o sistema deve remover/pseudonimizar nome, documento, e-mail, telefone e endereço, preservando o identificador técnico do cliente nos registros de `PedidoVenda` para manter a consistência contábil e de histórico de lotes vendidos.
- **Critério de Verificação:** Teste unitário `devePseudonimizarDadosPessoaisMantendoRastreabilidadeDoPedido()`.

---

## 2. Requisitos Não Funcionais (RNF)

### RNF-QUAL-01 — Precisão Monetária e Quantitativa (ISO/IEC 25010 - Adequação Funcional)
- **Descrição:** É proibido o uso de `float` ou `double` para valores monetários, pesos de receita ou saldos de estoque. Todos os cálculos financeiros e de proporção devem utilizar `BigDecimal`.

### RNF-SEG-01 — Trilha de Auditoria e Imutabilidade (ISO/IEC 27001 / 27002 e Marco Civil)
- **Descrição:** Entidades de razão (`EstoqueMovimento`) e eventos críticos de lote devem ser imutáveis após a criação e registrar `Instant` (UTC) e identificação do usuário autenticado.

### RNF-ACC-01 — Acessibilidade Semântica e Independência de Cor (LBI Lei 13.146/2015 e WCAG 2.1 AA)
- **Descrição:** Todos os indicadores de status operacional (estoque crítico, lote vencido, lote reprovado no pH) devem expor rótulo textual descritivo e código semântico independente de cor para leitura por tecnologias assistivas.

### RNF-LIC-01 — Inventário de Dependências e Conformidade Autoral (Leis 9.609/1998 e 9.610/1998)
- **Descrição:** O build do projeto deve gerar manifesto SBOM (CycloneDX) listando todas as dependências diretas e transitivas e suas respectivas licenças.
