ALTER TABLE public.hearing_status_audit
    ALTER COLUMN source TYPE varchar(100),
    ALTER COLUMN target TYPE varchar(100);

ALTER TABLE public.linked_hearing_status_audit
    ALTER COLUMN source TYPE varchar(100),
    ALTER COLUMN target TYPE varchar(100);
