-- Sistema de Banco de Alimentos (AEP 4º semestre - 2026.2)
-- Execute este script uma vez antes de rodar a aplicação.

CREATE DATABASE IF NOT EXISTS banco_alimentos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE banco_alimentos;

CREATE TABLE IF NOT EXISTS doador (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    tipo_doador VARCHAR(10)  NOT NULL,
    telefone    VARCHAR(20)  NOT NULL,
    email       VARCHAR(120) NOT NULL,
    CONSTRAINT chk_tipo_doador CHECK (tipo_doador IN ('FISICA', 'JURIDICA'))
);

CREATE TABLE IF NOT EXISTS doacao_alimento (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    doador_id     INT           NOT NULL,
    data_doacao   DATE          NOT NULL,
    tipo_alimento VARCHAR(100)  NOT NULL,
    quantidade_kg DECIMAL(10,2) NOT NULL,
    validade      DATE          NOT NULL,
    distribuida   BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_doacao_doador FOREIGN KEY (doador_id)
        REFERENCES doador (id) ON DELETE RESTRICT,
    CONSTRAINT chk_quantidade_positiva CHECK (quantidade_kg > 0)
);
