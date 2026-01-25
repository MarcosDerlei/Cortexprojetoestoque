ALTER TABLE pedido_compra_item
ADD COLUMN IF NOT EXISTS data_envio TIMESTAMP;

COMMENT ON COLUMN pedido_compra_item.data_envio IS
'Data/hora em que o pedido foi enviado via WhatsApp. NULL = RASCUNHO, preenchido = ENVIADO';