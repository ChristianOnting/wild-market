CREATE TABLE IF NOT EXISTS inventory (
    product_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    stock INT NOT NULL CHECK (stock >= 0)
);

CREATE TABLE IF NOT EXISTS orders (
    order_id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO inventory (product_id, name, stock) VALUES
('P100', 'Wireless Mouse', 25),
('P200', 'Mechanical Keyboard', 10),
('P300', 'USB-C Hub', 0)
ON CONFLICT (product_id) DO NOTHING;