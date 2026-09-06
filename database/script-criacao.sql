-- Script de criação do banco de dados
-- Sistema de Banco de Alimentos

CREATE DATABASE IF NOT EXISTS banco_alimentos;
USE banco_alimentos;

CREATE TABLE doador (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    tipo_doador VARCHAR(20) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    email VARCHAR(150) NOT NULL
);

CREATE TABLE doacao_alimento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doador_id INT NOT NULL,
    data_doacao DATE NOT NULL,
    tipo_alimento VARCHAR(100) NOT NULL,
    quantidade_kg DECIMAL(6,2) NOT NULL,
    validade DATE NOT NULL,
    CONSTRAINT fk_doacao_doador
        FOREIGN KEY (doador_id) REFERENCES doador(id)
        ON DELETE RESTRICT
);
