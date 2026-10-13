/* * Snapshot interno da tabela job antes da alteração da coluna description. * * Atenção: * Esta tabela é uma cópia de segurança operacional. * Ela não substitui um backup externo feito com pg_dump * ou pelo provedor do banco. */
CREATE TABLE job_backup_before_v6 ( LIKE job INCLUDING ALL );
INSERT INTO job_backup_before_v6 SELECT * FROM job;