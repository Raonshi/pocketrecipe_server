ALTER TABLE recipes ADD COLUMN owner_id UUID;

CREATE INDEX recipes_owner_id_idx ON recipes (owner_id);

INSERT INTO schema_versions (version) VALUES ('002');
