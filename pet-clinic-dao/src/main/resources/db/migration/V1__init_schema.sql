# Initial Flyway migration script: V1__init_schema.sql
CREATE TABLE customers (
    id CHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    created DATETIME NOT NULL,
    owner_token VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (owner_token, name)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE shopping_baskets (
    id CHAR(36) NOT NULL,
    created DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    status_date DATETIME NOT NULL,
    customer_id CHAR(36) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CHECK (status IN ('NEW', 'PAID', 'PROCESSED', 'UNKNOWN'))
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE items (
    id CHAR(36) NOT NULL,
    description VARCHAR(100) NOT NULL,
    amount INT NOT NULL,
    basket_id CHAR(36) NOT NULL,
    created DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (basket_id) REFERENCES shopping_baskets(id) ON DELETE CASCADE,
    CHECK (amount >= 1)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE OR REPLACE VIEW customer_basket_item_overview AS
SELECT
    c.id AS customer_id,
    c.name AS customer_name,
    c.timezone AS customer_timezone,
    c.owner_token AS owner_token,
    c.created AS customer_created,
    b.id AS basket_id,
    b.status AS basket_status,
    b.created AS basket_created,
    b.status_date AS basket_status_date,
    i.id AS item_id,
    i.description AS item_description,
    i.amount AS item_amount,
    i.created AS item_created
FROM
    customers c
LEFT JOIN
    shopping_baskets b ON c.id = b.customer_id
LEFT JOIN
    items i ON b.id = i.basket_id;