ALTER TABLE public.linked_group_details ALTER COLUMN reason_for_link DROP NOT NULL;
ALTER TABLE public.linked_group_details_audit ALTER COLUMN reason_for_link DROP NOT NULL;
