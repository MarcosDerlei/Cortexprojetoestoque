-- FORNECEDORES
CREATE TABLE IF NOT EXISTS fornecedores (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL UNIQUE,
    whatsapp VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- ITEM_FORNECEDOR
CREATE TABLE IF NOT EXISTS item_fornecedor (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    fornecedor_id BIGINT NOT NULL,
    preco NUMERIC(12,2),
    link VARCHAR(500),
    observacao VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_item_fornecedor_item
        FOREIGN KEY (item_id) REFERENCES itens(id),

    CONSTRAINT fk_item_fornecedor_fornecedor
        FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id),

    CONSTRAINT uk_item_fornecedor_unique
        UNIQUE (item_id, fornecedor_id)
);

-- PEDIDO_COMPRA
CREATE TABLE IF NOT EXISTS pedido_compra (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(30) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    data_atualizacao TIMESTAMP,
    observacao VARCHAR(255)
);

-- PEDIDO_COMPRA_ITEM
CREATE TABLE IF NOT EXISTS pedido_compra_item (
    id BIGSERIAL PRIMARY KEY,
    pedido_compra_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    fornecedor_id BIGINT,
    quantidade NUMERIC(12,2) NOT NULL,
    preco_unitario NUMERIC(12,2),
    observacao VARCHAR(255),

    CONSTRAINT fk_pedido_item_pedido
        FOREIGN KEY (pedido_compra_id) REFERENCES pedido_compra(id),

    CONSTRAINT fk_pedido_item_item
        FOREIGN KEY (item_id) REFERENCES itens(id),

    CONSTRAINT fk_pedido_item_fornecedor
        FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id)
);

-- CARRINHO
CREATE TABLE IF NOT EXISTS carrinho (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(30) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    data_atualizacao TIMESTAMP,
    total_estimado NUMERIC(12,2) NOT NULL DEFAULT 0
);

-- CARRINHO_ITEM
CREATE TABLE IF NOT EXISTS carrinho_item (
    id BIGSERIAL PRIMARY KEY,
    carrinho_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    fornecedor_id BIGINT,
    quantidade NUMERIC(12,2) NOT NULL,
    preco_estimado NUMERIC(12,2),
    observacao VARCHAR(255),

    CONSTRAINT fk_carrinho_item_carrinho
        FOREIGN KEY (carrinho_id) REFERENCES carrinho(id),

    CONSTRAINT fk_carrinho_item_item
        FOREIGN KEY (item_id) REFERENCES itens(id),

    CONSTRAINT fk_carrinho_item_fornecedor
        FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id)
);
