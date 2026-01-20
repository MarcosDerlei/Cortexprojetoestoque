-- =========================
-- FIX pedido_compra
-- =========================

-- 1) Ajustar tamanho de status e observacao
ALTER TABLE pedido_compra
    ALTER COLUMN status TYPE VARCHAR(20);

ALTER TABLE pedido_compra
    ALTER COLUMN observacao TYPE VARCHAR(500);

-- 2) Criar coluna data_envio (faltando)
ALTER TABLE pedido_compra
    ADD COLUMN IF NOT EXISTS data_envio TIMESTAMP;

-- 3) Criar user_id (faltando)
ALTER TABLE pedido_compra
    ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- Se tiver pedidos já criados, isso evita quebrar o NOT NULL.
-- Vamos setar user_id como 1 se existir esse usuário (ajusta se necessário)
UPDATE pedido_compra
SET user_id = 1
WHERE user_id IS NULL;

-- 4) Tornar user_id NOT NULL (igual no Java)
ALTER TABLE pedido_compra
    ALTER COLUMN user_id SET NOT NULL;

-- 5) Criar FK com users(id)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_pedido_compra_user'
          AND table_name = 'pedido_compra'
    ) THEN
        ALTER TABLE pedido_compra
            ADD CONSTRAINT fk_pedido_compra_user
            FOREIGN KEY (user_id)
            REFERENCES users(id);
    END IF;
END $$;


-- =========================
-- FIX pedido_compra_item
-- =========================

-- 6) criar item_fornecedor_id
ALTER TABLE pedido_compra_item
    ADD COLUMN IF NOT EXISTS item_fornecedor_id BIGINT;

-- 7) criar preco_no_momento
ALTER TABLE pedido_compra_item
    ADD COLUMN IF NOT EXISTS preco_no_momento NUMERIC(12,2) NOT NULL DEFAULT 0;

-- 8) se existir preco_unitario antigo, copiar pra preco_no_momento
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'pedido_compra_item'
          AND column_name = 'preco_unitario'
    ) THEN
        UPDATE pedido_compra_item
        SET preco_no_momento = COALESCE(preco_unitario, 0);
    END IF;
END $$;

-- 9) preencher item_fornecedor_id se o schema antigo existir (item_id + fornecedor_id)
-- ele tenta encontrar o relacionamento em item_fornecedor
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name='pedido_compra_item'
          AND column_name='item_id'
    )
    AND EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name='pedido_compra_item'
          AND column_name='fornecedor_id'
    ) THEN

        UPDATE pedido_compra_item pci
        SET item_fornecedor_id = ifo.id
        FROM item_fornecedor ifo
        WHERE ifo.item_id = pci.item_id
          AND ifo.fornecedor_id = pci.fornecedor_id
          AND pci.item_fornecedor_id IS NULL;

    END IF;
END $$;

-- 10) item_fornecedor_id NOT NULL
ALTER TABLE pedido_compra_item
    ALTER COLUMN item_fornecedor_id SET NOT NULL;

-- 11) criar FK item_fornecedor_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_pedido_compra_item_item_fornecedor'
          AND table_name = 'pedido_compra_item'
    ) THEN
        ALTER TABLE pedido_compra_item
            ADD CONSTRAINT fk_pedido_compra_item_item_fornecedor
            FOREIGN KEY (item_fornecedor_id)
            REFERENCES item_fornecedor(id);
    END IF;
END $$;

-- 12) limpar colunas antigas se existirem (não usadas no Java)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='pedido_compra_item'
          AND column_name='item_id'
    ) THEN
        ALTER TABLE pedido_compra_item DROP COLUMN item_id;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='pedido_compra_item'
          AND column_name='fornecedor_id'
    ) THEN
        ALTER TABLE pedido_compra_item DROP COLUMN fornecedor_id;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='pedido_compra_item'
          AND column_name='preco_unitario'
    ) THEN
        ALTER TABLE pedido_compra_item DROP COLUMN preco_unitario;
    END IF;
END $$;
