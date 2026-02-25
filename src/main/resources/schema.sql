CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- Schema
CREATE SCHEMA IF NOT EXISTS person;

-- Sequences (only if you still use them)
CREATE SEQUENCE IF NOT EXISTS person.hibernate_sequence
    AS BIGINT
    INCREMENT BY 1
    START WITH 1
    MINVALUE 1;

CREATE SEQUENCE IF NOT EXISTS hibernate_sequence
    AS BIGINT
    INCREMENT BY 1
    START WITH 1
    MINVALUE 1;

CREATE TABLE IF NOT EXISTS person.person_note_embedding (
                                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                   content TEXT NOT NULL,
                                                   metadata JSONB,
                                                   embedding VECTOR(768)
);

CREATE INDEX IF NOT EXISTS person_note_embedding_idx
    ON person.person_note_embedding
        USING hnsw (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS person_note_embedding_metadata_idx
    ON person.person_note_embedding
        USING GIN (metadata);

-- Table
CREATE TABLE IF NOT EXISTS person.person (
                                             id UUID NOT NULL,
                                             email VARCHAR(255),
                                             age VARCHAR(5),
                                             favorite_color VARCHAR(255),
                                             first_name VARCHAR(255),
                                             last_name VARCHAR(255),
                                             created_by VARCHAR(255),
                                             creation_date TIMESTAMP,
                                             last_modified_by VARCHAR(255),
                                             last_modified_date TIMESTAMP,
                                             action VARCHAR(10),

                                             note VARCHAR(255),
--                                              note_sentiment VARCHAR(255),
--                                              note_topic VARCHAR(255),
                                             CONSTRAINT pk_person PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS person.person_note_analysis (
                                      id           UUID PRIMARY KEY,
                                      person_id    UUID NOT NULL,
                                      topic        VARCHAR(50),
                                      sentiment    VARCHAR(50),
                                      model_name   VARCHAR(100),
                                      analyzed_at  TIMESTAMP,

                                      CONSTRAINT fk_person_analysis
                                          FOREIGN KEY (person_id)
                                              REFERENCES person.person(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT uq_person_analysis
                                          UNIQUE (person_id)
);

-- Cleanup vector store before data.sql inserts (order: schema = create + truncate, then data)
TRUNCATE TABLE person.person_note_embedding;