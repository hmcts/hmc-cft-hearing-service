ALTER TABLE ONLY public.case_categories
  ADD COLUMN IF NOT EXISTS case_category_parent varchar(70);
