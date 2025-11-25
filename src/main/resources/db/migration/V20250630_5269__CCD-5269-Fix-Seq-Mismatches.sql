-- Fix db sequence mismatches

---- Fix public.change_reasons_id_seq sequence
SELECT setval('public."change_reasons_id_seq"', (SELECT COALESCE(MAX(id), 0) + 1 FROM public.change_reasons));

ALTER TABLE ONLY public.change_reasons
ALTER COLUMN id SET DEFAULT nextval('public.change_reasons_id_seq');

-- Fix public.reasonable_adjustments.request_id sequence
SELECT setval('public."reasonable_adjustments_id_seq"', (SELECT COALESCE(MAX(id), 0) + 1 FROM public.reasonable_adjustments));

ALTER TABLE ONLY public.reasonable_adjustments
ALTER COLUMN id SET DEFAULT nextval('public.reasonable_adjustments_id_seq');
