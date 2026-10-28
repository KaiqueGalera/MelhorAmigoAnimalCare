CREATE TABLE IF NOT EXISTS atendimento (
    id                       TEXT PRIMARY KEY,
    animal_id                TEXT NOT NULL,
    agendamento_id           TEXT,
    status                   TEXT NOT NULL,
    data_hora_atendimento    TEXT NOT NULL,
    data_hora_conclusao      TEXT,
    justificativa            TEXT,
    temperatura_c            REAL,
    frequencia_cardiaca      INTEGER,
    frequencia_respiratoria  INTEGER
);

CREATE INDEX IF NOT EXISTS idx_atendimento_animal ON atendimento (animal_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_atendimento_agendamento
    ON atendimento (agendamento_id) WHERE agendamento_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_atendimento_animal_em_andamento
    ON atendimento (animal_id) WHERE status = 'EM_ANDAMENTO';

CREATE TABLE IF NOT EXISTS diagnostico (
    atendimento_id  TEXT    NOT NULL,
    ordem           INTEGER NOT NULL,
    codigo          TEXT    NOT NULL,
    descricao       TEXT    NOT NULL,
    tipo            TEXT    NOT NULL,
    PRIMARY KEY (atendimento_id, ordem)
);

CREATE TABLE IF NOT EXISTS prescricao (
    id                 TEXT PRIMARY KEY,
    atendimento_id     TEXT    NOT NULL,
    ordem              INTEGER NOT NULL,
    status             TEXT    NOT NULL,
    data_hora_emissao  TEXT    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_prescricao_atendimento ON prescricao (atendimento_id);

CREATE TABLE IF NOT EXISTS item_prescricao (
    id             TEXT PRIMARY KEY,
    prescricao_id  TEXT    NOT NULL,
    ordem          INTEGER NOT NULL,
    medicamento    TEXT    NOT NULL,
    dosagem        INTEGER NOT NULL,
    via            TEXT    NOT NULL,
    frequencia     TEXT    NOT NULL,
    dias_duracao   INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_item_prescricao_prescricao ON item_prescricao (prescricao_id);