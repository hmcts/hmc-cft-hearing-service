ALTER TABLE ONLY public.case_categories
ALTER COLUMN case_category_type SET NOT NULL;

ALTER TABLE ONLY public.case_categories
ALTER COLUMN case_category_value SET NOT NULL;

ALTER TABLE ONLY public.hearing_party
ALTER COLUMN party_role_type SET NOT NULL;
