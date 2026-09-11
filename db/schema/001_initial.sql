CREATE TABLE schema_versions (
    version VARCHAR(32) PRIMARY KEY,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recipes (
    recipe_name TEXT NOT NULL,
    recipe_author TEXT NOT NULL DEFAULT 'guest',
    recipe_parts TEXT,
    recipe_energy INTEGER,
    recipe_nat INTEGER,
    recipe_cal INTEGER,
    recipe_pro INTEGER,
    recipe_fat INTEGER,
    recipe_manual TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    PRIMARY KEY (recipe_name, recipe_author)
);

CREATE INDEX recipes_recipe_name_prefix_idx ON recipes (recipe_name);

INSERT INTO schema_versions (version) VALUES ('001');
