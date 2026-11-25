# Arquitetura Base e Matriz de Conformidade Proporcional — Nani ERP

## 1. Tratamento de Inconsistências e Lacunas Metodológicas do Escopo

### 1.1. Recorte Proporcional de Governança (Prevenção de Burocracia Morta)
Operações artesanais não sustentam comitês documentais pesados. No **Nani ERP**, a aderência às normas **ISO/IEC 12207, 25010, 27001/27002, 27701, 29119 e 29148** é implementada por **evidências executáveis e artefatos vivos no repositório**:
- Requisitos (29148) mapeados diretamente para classes de domínio e métodos de teste (29119).
- Controles de segurança e privacidade (27001/27701) impostos pelo modelo de dados (RBAC, ledger imutável de estoque e pseudonimização em código).
- Ciclo de vida (12207) gerido via Git, commits atômicos, versionamento semântico (`SemVer`) e migrations versionadas.

### 1.2. Segurança Alimentar e Rastreabilidade Sanitária (Domínio de Conservas)
Para conservas artesanais acidificadas ou pasteurizadas, o risco biológico crítico (como *Clostridium botulinum*) exige validação sistêmica antes da liberação do lote para estoque de vendas:
- **Bloqueio de Liberação por Parâmetro Sanitário:** Nenhum `LoteProducao` pode transitar para o status `LIBERADO_PARA_VENDA` se o **pH de equilíbrio for $> 4,50$** (salvo receitas com processo térmico esterilizante validado) ou se o tempo/temperatura de pasteurização estiver abaixo do mínimo da ficha técnica.
- **Rastreabilidade Bidirecional Completa:**
  - *Forward Traceability (Para frente):* Dado o lote de um insumo (ex.: lote de palmito, pimenta, vidro ou tampa), o sistema identifica todos os lotes de conserva produzidos e todos os pedidos/clientes que receberam unidades daquele lote.
  - *Backward Traceability (Para trás):* Dada uma reclamação de cliente (CDC) ou unidade vendida, o sistema recupera o lote de produção, os parâmetros térmicos/pH aferidos, a data de envase e os lotes exatos de cada matéria-prima e fornecedor.

### 1.3. Desacoplamento LGPD vs. Retenção Fiscal e Sanitária
Para resolver o conflito entre o direito de eliminação (Art. 18, VI da LGPD) e o cumprimento de obrigação legal/regulatória (Art. 16, I da LGPD — retenção fiscal e sanitária):
- A entidade `Cliente` isola os atributos pessoais identificáveis (nome, CPF/CNPJ, telefone, e-mail, endereço) de seu identificador interno imutável (`clienteId`).
- Quando solicitada a anonimização/eliminação sob a LGPD (e não havendo bloqueio fiscal ativo para dados estritamente tributários), o método `Cliente.pseudonimizarParaLgpd()` substitui os dados de contato/marketing por hashes irreversíveis, **mantendo intactos os registros de `PedidoVenda`, valores contábeis e vínculos com `LoteProducao`**.

### 1.4. Acessibilidade em Fluxos Densos de ERP (LBI e WCAG 2.1 Nível AA)
- **Independência de Cor (WCAG 1.4.1):** Estados críticos nunca usam apenas verde/amarelo/vermelho. Todo status possui código textual, ícone explícito e atributo `aria-label` (ex.: `[CRÍTICO - LOTE VENCIDO]`, `[ALERTA - ESTOQUE ABAIXO DO MÍNIMO]`).
- **Operação por Teclado e Foco Visível (WCAG 2.1.1 / 2.4.7):** Fluxos de lançamento de lote, pesagem de insumos e fechamento de pedido operáveis 100% sem mouse (atalhos previsíveis, ordem de tabulação lógica e mensagens de erro associadas via `aria-describedby`).

### 1.5. Governança de Licenças e Direitos Autorais (Leis 9.609/1998 e 9.610/1998)
- Uso exclusivo de dependências sob licenças permissivas ou compatíveis (Apache-2.0, MIT, EPL-2.0) verificadas via geração de **SBOM (CycloneDX)** no build Maven (`pom.xml`), garantindo segurança jurídica sobre a titularidade do código-fonte e dos ativos (templates de rótulos e relatórios).

---

## 2. Matriz de Rastreabilidade Normativa

| Norma / Lei | Escopo no Nani ERP | Artefato / Implementação Verificável |
| :--- | :--- | :--- |
| **ISO/IEC/IEEE 29148** | Engenharia de Requisitos | `docs/REQUISITOS_ISO_29148.md` com critérios determinísticos de aceite |
| **ABNT NBR ISO/IEC 12207** | Processos de Ciclo de Vida | Controle de versão Git, estrutura Maven padronizada e ADRs em `docs/` |
| **ABNT NBR ISO/IEC 25010** | Qualidade de Produto | Precisão `BigDecimal` (adequação funcional), imutabilidade de ledger (confiabilidade), domínio desacoplado (manutenibilidade) |
| **ABNT NBR ISO/IEC 29119** | Teste de Software | `docs/PLANO_TESTES_ISO_29119.md` e suíte JUnit 5 em `src/test/java/` |
| **ABNT NBR ISO/IEC 27001 / 27002** | Segurança da Informação | Trilha de auditoria obrigatória em `EstoqueMovimento`, justificativa em ajustes manuais e segredos via variáveis de ambiente |
| **ABNT NBR ISO/IEC 27701 & LGPD (Lei 13.709/18)** | Privacidade e Proteção de Dados | Minimização de dados e método `pseudonimizarParaLgpd()` em `Cliente.java` |
| **Marco Civil (Lei 12.965/14)** | Registros de Acesso à Aplicação | Metadados de autoria, timestamp UTC (`Instant`) e rastreabilidade de operação |
| **Leis 9.609/98 e 9.610/98** | Software e Direitos Autorais | Geração de SBOM no `pom.xml` e cabeçalhos/registros de titularidade |
| **LBI (Lei 13.146/15) & WCAG 2.1** | Acessibilidade Digital | Requisitos RNF-ACC-01 a 04 e contratos semânticos de status na camada de apresentação |
| **CDC (Lei 8.078/90)** | Defesa do Consumidor | Transparência de lote, validade, preço unitário, composição e rastreamento para recall em `PedidoVenda` |
