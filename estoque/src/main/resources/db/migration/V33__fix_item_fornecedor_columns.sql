-- 1) adicionar preco_referencia (se não existir)
ALTER TABLE item_fornecedor
ADD COLUMN IF NOT EXISTS preco_referencia NUMERIC(12,2) NOT NULL DEFAULT 0;

-- 2) se existir a coluna antiga "preco", copiar o valor pra nova e depois remover
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema='public'
          AND table_name='item_fornecedor'
          AND column_name='preco'
    ) THEN
        UPDATE item_fornecedor
        SET preco_referencia = COALESCE(preco, 0);

        EXECUTE 'ALTER TABLE item_fornecedor DROP COLUMN preco';
    END IF;
END $$;

-- 3) unidade_compra faltando
ALTER TABLE item_fornecedor
ADD COLUMN IF NOT EXISTS unidade_compra VARCHAR(30);

-- 4) garantir observacao existe
ALTER TABLE item_fornecedor
ADD COLUMN IF NOT EXISTS observacao VARCHAR(255);

-- 5) garantir ativo existe
ALTER TABLE item_fornecedor
ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;

-- 6) (opcional) remover "link" se existir e não usar mais
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema='public'
          AND table_name='item_fornecedor'
          AND column_name='link'
    ) THEN
        EXECUTE 'ALTER TABLE item_fornecedor DROP COLUMN link';
    END IF;
END $$;
