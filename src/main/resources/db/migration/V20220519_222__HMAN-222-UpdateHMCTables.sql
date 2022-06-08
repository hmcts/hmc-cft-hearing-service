ALTER TABLE ONLY public.hearing ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.case_hearing_request ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.linked_group_details ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.linked_group_details_audit ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.linked_hearing_details_audit ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.cancellation_reasons ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.non_standard_durations ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.required_locations ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.required_facilities ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.panel_requirements ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.panel_specialisms ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.panel_authorisation_requirements ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.panel_user_requirements ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.hearing_party ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.case_categories ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.party_relationship_details ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.individual_detail ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.organisation_detail ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.hearing_day_panel ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.hearing_day_details ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.hearing_attendee_details ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.reasonable_adjustments ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.unavailability ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.contact_details ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.hearing_response ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.actual_hearing ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.actual_hearing_day ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.actual_hearing_day_pauses ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.actual_hearing_party ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.actual_party_relationship_detail ADD COLUMN created_date_time timestamp without time zone;
ALTER TABLE ONLY public.actual_attendee_individual_detail ADD COLUMN created_date_time timestamp without time zone;

--
-- Adding updated_date_time column
--

ALTER TABLE ONLY public.hearing ADD COLUMN updated_date_time timestamp without time zone;
