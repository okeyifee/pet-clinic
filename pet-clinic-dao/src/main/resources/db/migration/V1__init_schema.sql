# Initial Flyway migration script: V1__init_schema.sql
CREATE TABLE customers (
    id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    created DATETIME NOT NULL,
    owner_token VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE shopping_baskets (
    id CHAR(36) NOT NULL,
    created DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    status_date DATETIME NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    CHECK (status IN ('NEW', 'PAID', 'PROCESSED', 'UNKNOWN'))
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE items (
    id CHAR(36) NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount INT NOT NULL,
    basket_id VARCHAR(36) NOT NULL,
    created DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (basket_id) REFERENCES shopping_baskets(id),
    CHECK (amount >= 1)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;