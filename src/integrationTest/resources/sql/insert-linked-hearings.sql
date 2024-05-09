INSERT INTO linked_group_details (linked_group_id, status, link_type, request_id, request_date_time, reason_for_link, linked_group_latest_version)
VALUES ('7600000000', 'ACTIVE', 'ORDERED', '44444', '2021-08-10 11:20:00', 'good reason', '1');
INSERT INTO hearing (hearing_id, is_linked_flag, linked_group_id, linked_order, status)
VALUES ('2000000005', 't', '7600000000', '1', 'HEARING_REQUESTED');
INSERT INTO hearing (hearing_id, is_linked_flag, linked_group_id, linked_order, status)
VALUES ('2000000006', 't', '7600000000', '2', 'HEARING_REQUESTED');

INSERT INTO linked_group_details (linked_group_id, status, link_type, request_id, request_date_time, reason_for_link, linked_group_latest_version)
VALUES ('7600000501', 'PENDING', 'ORDERED', '44445', '2021-08-10 11:20:00', 'bad reason', '1');
INSERT INTO linked_group_details (linked_group_id, status, link_type, request_id, request_date_time, reason_for_link, linked_group_latest_version)
VALUES ('7600000502', 'ERROR', 'ORDERED', '44446', '2021-08-10 11:20:00', 'bad reason', '1');


INSERT INTO linked_group_details (linked_group_id, status, link_type, request_id, request_date_time, reason_for_link, linked_group_latest_version)
VALUES ('7600000300', 'ACTIVE', 'ORDERED', '44447', '2021-08-10 11:20:00', 'good reason', '1');
INSERT INTO hearing (hearing_id, is_linked_flag, linked_group_id, linked_order, status)
VALUES ('2000000301', 't', '7600000300', '1', 'HEARING_REQUESTED');

INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
VALUES (1, '2000000301', '2020-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '1');
INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
VALUES (2, '2000000301', '2021-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '1');

-- start date in the future, but hearing_response version is not latest
INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
VALUES (1, '1', '2920-01-10 11:20:00', '2020-08-10 11:20:00', 'venue1-2', 'room1-2');

-- start date in the past for the latest hearing_response version
INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
VALUES (2, '2', '2021-01-10 11:20:00', '2021-08-10 11:20:00', 'venue1-1', 'room1-1');
INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
VALUES (3, '2', '2920-01-10 11:20:00', '2020-08-10 11:20:00', 'venue1-2', 'room1-2');

INSERT INTO linked_group_details (linked_group_id, status, link_type, request_id, request_date_time, reason_for_link, linked_group_latest_version)
VALUES ('7600000301', 'ACTIVE', 'ORDERED', '44448', '2021-08-10 11:20:00', 'good reason', '1');
INSERT INTO hearing (hearing_id, is_linked_flag, linked_group_id, linked_order, status)
VALUES ('2000000302', 't', '7600000301', '2', 'UPDATE_REQUESTED');

INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
VALUES (3, '2000000302', '2021-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '1');

-- start date in the past for the latest hearing_response version
INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
VALUES (4, '3', '2021-01-10 11:20:00', '2021-08-10 11:20:00', 'venue1-1', 'room1-1');

INSERT INTO linked_group_details (linked_group_id, status, link_type, request_id, request_date_time, reason_for_link, linked_group_latest_version) VALUES ('7700000000', 'ACTIVE', 'ORDERED', '12345', '2021-08-10 11:20:00', 'good reason', '1');
INSERT INTO hearing (hearing_id, is_linked_flag, linked_group_id, linked_order, status) VALUES ('2100000005', 't', '7700000000', '1', 'HEARING_REQUESTED');
INSERT INTO hearing (hearing_id, is_linked_flag, linked_group_id, linked_order, status) VALUES ('2100000006', 't', '7700000000', '2', 'HEARING_REQUESTED');

insert into case_hearing_request (
  auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
VALUES ( 't'	,'Some hearing type',	60,	'Priority type',	4,'f','f','AB123',null,'ABA1'	,1111222233334444,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2100000005	,'t'	,	'Some listing comments',	'Some judge',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');
insert into case_hearing_request (
  auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
VALUES ( 't'	,'Some hearing type',	60,	'Priority type',	4,'f','f','AB123',null,'ABA1'	,1111222233334444,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2100000006	,'t'	,	'Some listing comments',	'Some judge',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');
insert into case_hearing_request (
  auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
VALUES ( 't'	,'Some hearing type',	60,	'Priority type',	4,'f','f','AB123',null,'ABA1'	,1111222233334444,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000302	,'t'	,	'Some listing comments',	'Some judge',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');
insert into case_hearing_request (
  auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
VALUES ( 't'	,'Some hearing type',	60,	'Priority type',	4,'f','f','AB123',null,'ABA1'	,1111222233334444,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000301	,'t'	,	'Some listing comments',	'Some judge',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');
