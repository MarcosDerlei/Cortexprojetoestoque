ALTER TABLE item_fornecedor
ADD COLUMN IF NOT EXISTS preco_referencia numeric(12,2) NOT NULL DEFAULT 0;
