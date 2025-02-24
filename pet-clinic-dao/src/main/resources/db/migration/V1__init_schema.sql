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