# Nani — Sistema de Gestão de Conservas Artesanais

Repositório central dos sistemas desenvolvidos para auxiliar no controle de **produção, estoque, custos e vendas de conservas artesanais**.

## Estrutura do Projeto

```text
Nani/
├── dados/                  # Armazenamento de dados (banco de dados local, planilhas, backups)
│   ├── producao/           # Registros de lotes, receitas e insumos
│   ├── estoque/            # Controle de matérias-primas, potes, rótulos e produtos acabados
│   └── vendas/             # Histórico de pedidos, clientes e fluxo de caixa
├── src/                    # Código-fonte dos sistemas
│   ├── producao/           # Módulo de controle de produção, receitas e validade de lotes
│   ├── estoque/            # Módulo de movimentação de insumos e produtos prontos
│   ├── vendas/             # Módulo de registro de vendas, precificação e clientes
│   └── relatorios/         # Geração de relatórios financeiros e de produção
├── docs/                   # Documentação, fichas técnicas de receitas e boas práticas
└── tests/                  # Testes automatizados dos sistemas
```

## Principais Funcionalidades Planejadas

1. **Controle de Produção e Receitas**
   - Cadastro de fichas técnicas (ingredientes, rendimento, custo por pote).
   - Registro de lotes produzidos com data de fabricação e validade.
2. **Gestão de Estoque**
   - Controle de insumos (legumes, temperos, conservantes, vidros, tampas e rótulos).
   - Estoque de produtos acabados por sabor e lote.
3. **Controle de Vendas e Financeiro**
   - Registro de vendas, clientes e canais de venda.
   - Cálculo de margem de lucro e relatórios mensais.
