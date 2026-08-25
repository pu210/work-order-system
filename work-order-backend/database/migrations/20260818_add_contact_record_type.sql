If COL_LENGTH('dbo.contact_records', 'record_type') is null
begin

ALTER TABLE dbo.contact_records
    ADD record_type VARCHAR(30) NOT NULL
    CONSTRAINT DF_contact_records_record_type DEFAULT 'COMMENT' WITH VALUES;


end;
