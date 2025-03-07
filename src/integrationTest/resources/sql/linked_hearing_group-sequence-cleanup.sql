DELETE FROM linked_group_details_audit;
DELETE FROM linked_group_details;

ALTER SEQUENCE public.linked_group_details_id_seq RESTART WITH 1;
