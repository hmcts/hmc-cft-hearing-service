INSERT INTO hearing ( hearing_id, status) VALUES ('2000000024', 'AWAITING_LISTING');

insert into case_hearing_request (
case_hearing_id, auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
VALUES (100, 't'	,'Some hearing type',	60,	'Priority type',	4,'f','f','AB123',null,'ABA1'	,1111222233334466,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000024	,'t',	'Some listing comments',	'Some judge',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

INSERT INTO case_categories(case_hearing_id, case_category_type, case_category_value, id) VALUES (100, 'CASESUBTYPE', 'PROBATE', 100);

INSERT INTO non_standard_durations(case_hearing_id, non_standard_hearing_duration_reason_type, id) VALUES (100, 'reason', 100);

INSERT INTO panel_authorisation_requirements(case_hearing_id, authorisation_type, authorisation_subtype, id) VALUES (100, 'AuthType1', null, 100);
INSERT INTO panel_authorisation_requirements(case_hearing_id, authorisation_type, authorisation_subtype, id) VALUES (100, null, 'Subtype1', 200);

INSERT INTO panel_requirements(case_hearing_id, role_type, id)  VALUES (100, 'roleType1', 100);
INSERT INTO panel_requirements(case_hearing_id, role_type, id)  VALUES (100, 'roleType1', 200);

INSERT INTO panel_specialisms(case_hearing_id, specialism_type, id) VALUES (100, 'specialism', 100);

INSERT INTO panel_user_requirements(case_hearing_id, judicial_user_id, user_type, requirement_type, id) VALUES (100, 'judge1', 'user1', 'MUSTINC', 100);

INSERT INTO required_facilities(case_hearing_id, facility_type, id) VALUES (100, 'facility1', 100);

INSERT INTO required_locations(case_hearing_id, location_level_type, location_id, id) VALUES (100, 'COURT', 'CLUSTER', 100);

INSERT INTO hearing_party(case_hearing_id, tech_party_id, party_reference, party_type, party_role_type) VALUES (100, 100, 'p1_1', 'IND', 'DEF');
INSERT INTO hearing_party(case_hearing_id, tech_party_id, party_reference, party_type, party_role_type) VALUES (100, 200, 'p2', 'IND', 'DEF');

INSERT INTO contact_details(tech_party_id, contact_type, contact_details, id) VALUES (100, 'email', 'abc@gmail.com', 100);

INSERT INTO reasonable_adjustments(tech_party_id, reasonable_adjustment_code, id) VALUES (100, 'Adjust1', 100);

INSERT INTO unavailability(tech_party_id, day_of_week_unavailable, day_of_week_unavailable_type, start_date, end_date, id, unavailability_type)
VALUES (100, 'TUESDAY', 'AM', null, null, 100, 'DOW');
INSERT INTO unavailability(tech_party_id, day_of_week_unavailable, day_of_week_unavailable_type, start_date, end_date, id, unavailability_type)
VALUES (100, null, null, '2022-10-20 00:00:00', '2022-10-22 00:00:00', 101, 'Range');

INSERT INTO individual_detail(tech_party_id, related_party_relationship_type, related_party_id, vulnerability_details, vulnerable_flag, interpreter_language, channel_type, last_name, first_name, title, id)
VALUES (100, 'sibling', 'P1', 'vulnerable', 'true', 'French', 'channel1', 'Bloggs', 'Joe', 'Mrs', 100);
INSERT INTO individual_detail(tech_party_id, related_party_relationship_type, related_party_id, vulnerability_details, vulnerable_flag, interpreter_language, channel_type, last_name, first_name, title, id)
VALUES (200, 'sibling', 'P2', null, 'false', null, null, 'Bloggs', 'Josh', 'Mr', 200);
