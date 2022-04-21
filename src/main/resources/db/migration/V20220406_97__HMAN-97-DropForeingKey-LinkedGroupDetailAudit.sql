ALTER TABLE ONLY public.linked_group_details_audit
DROP CONSTRAINT IF EXISTS fk_linked_group_details_audit_linked_group_id;

ALTER TABLE ONLY public.linked_hearing_details_audit
DROP CONSTRAINT IF EXISTS fk_linked_hearing_details_audit_linked_group_id;

ALTER TABLE ONLY public.linked_group_details_audit ALTER COLUMN linked_group_details_audit_id SET DEFAULT nextval('public.linked_group_details_audit_id_seq'::regclass);
