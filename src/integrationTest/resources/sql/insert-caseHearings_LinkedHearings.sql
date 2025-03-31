INSERT INTO public.linked_group_details(
	linked_group_id, linked_group_latest_version, request_id, request_name, request_date_time, link_type, reason_for_link, status, linked_comments)
	VALUES (21, 1, 11, 'HMAN-56 group 1', '2022-03-08T10:33:01', 'SAME_SLOT', 'Test 1', 'ACTIVE', 'What to go here');
INSERT INTO public.linked_group_details_audit(
	linked_group_id, linked_group_version, request_id, request_name, request_date_time, link_type, reason_for_link, status, linked_comments)
	VALUES (21, 1, 11, 'HMAN-56 group 1', '2022-03-08T10:33:01', 'SAME_SLOT', 'Test 1', 'ACTIVE', 'What to go here');

INSERT INTO public.linked_group_details(
	linked_group_id, linked_group_latest_version, request_id, request_name, request_date_time, link_type, reason_for_link, status, linked_comments)
	VALUES (22, 1, 12, 'HMAN-56 group 2', '2022-03-11T20:33:01', 'ORDERED', 'Test 2', 'ACTIVE', 'commented');
INSERT INTO public.linked_group_details_audit(
	linked_group_id, linked_group_version, request_id, request_name, request_date_time, link_type, reason_for_link, status, linked_comments)
	VALUES (22, 1, 12, 'HMAN-56 group 2', '2022-03-11T20:33:01', 'ORDERED', 'Test 2', 'ACTIVE', 'commented');

INSERT INTO public.linked_group_details(
	linked_group_id, linked_group_latest_version, request_id, request_name, request_date_time, link_type, reason_for_link, status, linked_comments)
	VALUES (23, 1, 13, 'HMAN-56 group 3', '2022-03-11T20:33:01', 'ORDERED', 'Test 3', 'PENDING', 'commented');
INSERT INTO public.linked_group_details_audit(
	linked_group_id, linked_group_version, request_id, request_name, request_date_time, link_type, reason_for_link, status, linked_comments)
	VALUES (23, 1, 13, 'HMAN-56 group 3', '2022-03-11T20:33:01', 'ORDERED', 'Test 3', 'PENDING', 'commented');

INSERT INTO public.linked_group_details(
	linked_group_id, linked_group_latest_version, request_id, request_name, request_date_time, link_type, reason_for_link, status, linked_comments)
	VALUES (24, 1, 24, 'HMAN-56 group 4', '2022-03-11T20:33:01', 'ORDERED', 'Test 4', 'ERROR', 'commented');
INSERT INTO public.linked_group_details_audit(
	linked_group_id, linked_group_version, request_id, request_name, request_date_time, link_type, reason_for_link, status, linked_comments)
	VALUES (24, 1, 24, 'HMAN-56 group 4', '2022-03-11T20:33:01', 'ORDERED', 'Test 4', 'ERROR', 'commented');

insert into hearing ( hearing_id, status, is_linked_flag) values ('2000000000', 'HEARING_REQUESTED', false);
insert into hearing ( hearing_id, status, is_linked_flag) values ('2000000004', 'HEARING_REQUESTED', false);
insert into hearing ( hearing_id, status, is_linked_flag, linked_group_id) values ('2000000005', 'HEARING_REQUESTED', true, 21);
insert into hearing ( hearing_id, status, is_linked_flag, linked_group_id) values ('2000000006', 'HEARING_REQUESTED', true, 21);
insert into hearing ( hearing_id, status, is_linked_flag, linked_group_id, linked_order) values ('2000000007', 'HEARING_REQUESTED', true, 22, 1);
insert into hearing ( hearing_id, status, is_linked_flag, linked_group_id, linked_order) values ('2000000008', 'HEARING_REQUESTED', true, 22, 2);
insert into hearing ( hearing_id, status, is_linked_flag) values ('2000000010', 'UPDATE_SUBMITTED', true);
insert into hearing ( hearing_id, status, is_linked_flag, linked_group_id) values ('2000000009', 'HEARING_REQUESTED', true, 22);
insert into hearing ( hearing_id, status, is_linked_flag) values ('2000000011', 'INVALID_STATE', true);
insert into hearing ( hearing_id, status, is_linked_flag) values ('2000000012', 'HEARING_REQUESTED', true);
insert into hearing ( hearing_id, status, is_linked_flag) values ('2000000013', 'HEARING_REQUESTED', true);
insert into hearing ( hearing_id, status, is_linked_flag) values ('2000000014', 'HEARING_REQUESTED', true);

INSERT INTO public.linked_hearing_details_audit(
	linked_hearing_details_audit_id, linked_group_id, linked_group_version, hearing_id, linked_order)
	VALUES (91, 21, 2, 2000000005, 1);
INSERT INTO public.linked_hearing_details_audit(
	linked_hearing_details_audit_id, linked_group_id, linked_group_version, hearing_id, linked_order)
	VALUES (92, 21, 3, 2000000006, 2);

INSERT INTO public.linked_hearing_details_audit(
	linked_hearing_details_audit_id, linked_group_id, linked_group_version, hearing_id, linked_order)
	VALUES (93, 22, 1, 2000000007, 3);
INSERT INTO public.linked_hearing_details_audit(
	linked_hearing_details_audit_id, linked_group_id, linked_group_version, hearing_id, linked_order)
	VALUES (94, 22, 2, 2000000008, 2);
INSERT INTO public.linked_hearing_details_audit(
	linked_hearing_details_audit_id, linked_group_id, linked_group_version, hearing_id, linked_order)
	VALUES (95, 22, 1, 2000000009, 1);

insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000000	,'t'	,	'Some listing comments1',	'Some judge1',	'2022-04-01 00:00:00',	'2022-04-12 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType2',	61,	'Priority type2',	6,'t','t','AB123',null,'TEST'	,9372710950276233,	'2020-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC333',	't',	'2021-10-10 00:00:00',	2,	2000000010	,'f'	,	'Some listing comments2',	'Some judge2',	'2022-05-01 00:00:00',	'2022-05-12 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType3',	63,	'Priority type3',	6,'t','t','AB123',null,'TEST'	,9372710950276233,	'2020-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC333',	't',	'2021-10-10 00:00:00',	1,	2000000009	,'f'	,	'Some listing comments3',	'Some judge3',	'2022-04-14 00:00:00',	'2022-04-19 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	2,	2000000000	,'t'	,	'Some listing comments1',	'Some judge1',	'2022-04-01 00:00:00',	'2022-04-12 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType2',	61,	'Priority type2',	6,'t','t','AB123',null,'TEST'	,9372710950276233,	'2020-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC333',	't',	'2021-10-10 00:00:00',	3,	2000000010	,'f'	,	'Some listing comments2',	'Some judge2',	'2022-05-01 00:00:00',	'2022-05-12 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType3',	63,	'Priority type3',	6,'t','t','AB123',null,'TEST'	,9372710950276233,	'2020-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC333',	't',	'2021-10-10 00:00:00',	2,	2000000009	,'f'	,	'Some listing comments3',	'Some judge3',	'2022-04-14 00:00:00',	'2022-04-19 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	61,	'Priority type2',	6,'t','t','AB123',null,'TEST'	,9372710950276233,	'2020-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC333',	't',	'2021-10-10 00:00:00',	2,	2000000007	,'f'	,	'Some listing comments3',	'Some judge2',	'2022-04-10 00:00:00',	'2022-04-15 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType2',	62,	'Priority type1',	6,'t','t','AB123',null,'TEST'	,9372710950276233,	'2020-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC333',	't',	'2021-10-10 00:00:00',	2,	2000000008	,'f'	,	'Some listing comments3',	'Some judge1',	'2022-05-14 00:00:00',	'2022-05-19 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000012	,'t'	,	'Some listing comments1',	'Some judge1',	'2022-04-15 00:00:00',	'2022-04-19 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000013	,'t'	,	'Some listing comments1',	'Some judge1',	'2022-06-01 00:00:00',	'2022-06-12 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000014	,'t'	,	'Some listing comments1',	'Some judge1',	'2022-06-01 00:00:00',	'2022-06-12 00:00:00');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)

values (1, '2000000000', '2033-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', 1);
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (2, '2000000010', '2033-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', 1, '2020-08-10 11:20:00');
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (3, '2000000009', '2033-08-10 11:20:00', 'listingStatus3-1', 'caselistingStatus3-1', 1);
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (4, '2000000012', '2021-08-10 11:20:00', 'listingStatus3-1', 'caselistingStatus3-1', 1);
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (5, '2000000013', '2033-08-10 11:20:00', 'listingStatus3-1', 'caselistingStatus3-1', 1);
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (6, '2000000007', '2022-06-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', 1, '2022-06-05 11:20:00');
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (7, '2000000008', '2022-05-03 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', 1, '2022-05-01 11:20:00');
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (8, '2000000014', '2023-08-10 11:20:00', 'listingStatus3-1', 'caselistingStatus3-1', 1);
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (9, '2000000007', '2022-06-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', 2, '2022-06-05 11:20:00');
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (10, '2000000008', '2022-05-03 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', 2, '2022-05-01 11:20:00');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (1, 1, '2022-05-10 11:20:00', '2022-05-18 11:20:00', 'venue1-1', 'room1-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (2, 1, '2022-05-10 11:20:00', '2022-05-11 11:20:00', 'venue1-2', 'room1-2');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (3, 2, '2022-05-10 11:20:00', '2022-05-12 11:20:00', 'venue2-1', 'room2-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (4, 3, '2099-01-10 11:20:00', '2099-01-11 11:20:00', 'venue3-1', 'room3-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (5, 6, '2099-08-10 11:20:00', '2099-08-11 13:20:00', 'venue3-1', 'room3-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (6, 7, '2099-08-10 11:20:00', '2099-08-10 14:20:00', 'venue3-1', 'room3-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (7, 4, '2099-08-10 11:20:00', '2099-08-10 13:20:00', 'venue3-1', 'room3-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (8, 5, '2099-08-10 11:20:00', '2099-08-10 14:20:00', 'venue3-1', 'room3-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (9, 6, '2099-01-10 11:20:00', '2099-08-10 11:20:00', 'venue3-1', 'room3-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (10, 7, '2099-01-10 11:20:00', '2099-08-10 11:20:00', 'venue3-1', 'room3-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (11, 8, '2022-01-10 11:20:00', '2022-08-10 11:20:00', 'venue3-1', 'room3-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (12, 9, '2099-01-10 11:20:00', '2099-08-10 11:20:00', 'venue3-1', 'room3-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (13, 10, '2099-08-10 11:20:00', '2099-08-14 11:20:00', 'venue3-1', 'room3-1');

insert into hearing_day_panel(id, hearing_day_id, panel_user_id,is_presiding) values (1, 1, 'panel1-1','true');
insert into hearing_day_panel(id, hearing_day_id, panel_user_id,is_presiding) values (2, 1, 'panel1-2','false');
insert into hearing_day_panel(id, hearing_day_id, panel_user_id,is_presiding) values (3, 2, 'panel1-2',null);
insert into hearing_day_panel(id, hearing_day_id, panel_user_id,is_presiding) values (4, 3, 'panel2-1','true');
insert into hearing_day_panel(id, hearing_day_id, panel_user_id,is_presiding) values (5, 4, 'panel3-1','false');

insert into hearing_attendee_details(id, hearing_day_id, party_id, party_sub_channel_type) values (1, 1, 'party1-1','subChannel1-1');
insert into hearing_attendee_details(id, hearing_day_id, party_id, party_sub_channel_type) values (2, 1, 'party1-2','subChannel1-2');
insert into hearing_attendee_details(id, hearing_day_id, party_id, party_sub_channel_type) values (3, 3, 'party2-1','subChannel2-1');
insert into hearing_attendee_details(id, hearing_day_id, party_id, party_sub_channel_type) values (4, 4, 'party3-1','subChannel3-1');
