ALTER TABLE ONLY public.non_standard_durations DROP CONSTRAINT IF EXISTS non_standard_durations_pkey;

ALTER TABLE ONLY public.non_standard_durations
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.non_standard_durations
    ADD CONSTRAINT non_standard_durations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.required_facilities DROP CONSTRAINT IF EXISTS required_facilities_pkey;

ALTER TABLE ONLY public.required_facilities
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.required_facilities
    ADD CONSTRAINT required_facilities_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.required_locations DROP CONSTRAINT IF EXISTS required_locations_pkey;

ALTER TABLE ONLY public.required_locations
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.required_locations
    ADD CONSTRAINT required_locations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.panel_requirements DROP CONSTRAINT IF EXISTS panel_requirements_pkey;

ALTER TABLE ONLY public.panel_requirements
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.panel_requirements
    ADD CONSTRAINT panel_requirements_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.panel_authorisation_requirements DROP CONSTRAINT IF EXISTS panel_authorisation_requirements_pkey;

ALTER TABLE ONLY public.panel_authorisation_requirements
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.panel_authorisation_requirements
    ADD CONSTRAINT panel_authorisation_requirements_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.panel_specialisms DROP CONSTRAINT IF EXISTS panel_specialisms_pkey;

ALTER TABLE ONLY public.panel_specialisms
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.panel_specialisms
    ADD CONSTRAINT panel_specialisms_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.panel_user_requirements DROP CONSTRAINT IF EXISTS panel_user_requirements_pkey;

ALTER TABLE ONLY public.panel_user_requirements
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.panel_user_requirements
    ADD CONSTRAINT panel_user_requirements_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.case_categories DROP CONSTRAINT IF EXISTS case_categories_pkey;

ALTER TABLE ONLY public.case_categories
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.case_categories
    ADD CONSTRAINT case_categories_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.individual_detail DROP CONSTRAINT IF EXISTS individual_detail_pkey;

ALTER TABLE ONLY public.individual_detail
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.individual_detail
    ADD CONSTRAINT individual_detail_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.reasonable_adjustments DROP CONSTRAINT IF EXISTS reasonable_adjustments_pkey;

ALTER TABLE ONLY public.reasonable_adjustments
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.reasonable_adjustments
    ADD CONSTRAINT reasonable_adjustments_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.organisation_detail DROP CONSTRAINT IF EXISTS organisation_detail_pkey;

ALTER TABLE ONLY public.organisation_detail
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.organisation_detail
    ADD CONSTRAINT organisation_detail_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.unavailability DROP CONSTRAINT IF EXISTS unavailability_pkey;

ALTER TABLE ONLY public.unavailability
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.unavailability
    ADD CONSTRAINT unavailability_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.contact_details DROP CONSTRAINT IF EXISTS contact_details_pkey;

ALTER TABLE ONLY public.contact_details
    ADD COLUMN id bigint not null;

ALTER TABLE ONLY public.contact_details
    ADD CONSTRAINT contact_details_pkey PRIMARY KEY (id);
