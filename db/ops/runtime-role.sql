-- Run once with a Supabase database administrator account, not the application account.
-- Set the generated password separately through the provider's secret-management workflow.
CREATE ROLE pocketrecipe_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
GRANT CONNECT ON DATABASE postgres TO pocketrecipe_app;
GRANT USAGE ON SCHEMA public TO pocketrecipe_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE recipes TO pocketrecipe_app;
REVOKE CREATE ON SCHEMA public FROM pocketrecipe_app;
