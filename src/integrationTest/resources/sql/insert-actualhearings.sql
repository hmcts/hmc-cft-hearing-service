insert into hearing ( hearing_id, status) values ('2000000000', 'HEARING_REQUESTED');
insert into hearing ( hearing_id, status) values ('2000000001', 'AWAITING_LISTING');
insert into hearing ( hearing_id, status) values ('2000000002', 'UPDATE_REQUESTED');
insert into hearing ( hearing_id, status) values ('2000000003', 'UPDATE_SUBMITTED');
insert into hearing ( hearing_id, status) values ('2000000004', 'LISTED');
insert into hearing ( hearing_id, status) values ('2000000005', 'COMPLETED');
insert into hearing ( hearing_id, status) values ('2000000006', 'ADJOURNED');
insert into hearing ( hearing_id, status) values ('2000000007', 'CANCELLED');
insert into hearing ( hearing_id, status) values ('2000000008', 'LISTED');
insert into hearing ( hearing_id, status) values ('2000000009', 'CANCELLED');
insert into hearing ( hearing_id, status) values ('2000000011', 'COMPLETED');
insert into hearing ( hearing_id, status) values ('2000000012', 'ADJOURNED');

insert into case_hearing_request (
auto_list_flag, is_a_panel_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ('t'	, true, 'hearingType1',	60,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000004	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (1, '2000000004', '2022-01-28 11:20:00', 'DRAFT', 'CASE_CREATED', 1);
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (2, '2000000004', '2022-01-28 11:20:00', 'PROVISIONAL', 'AWAITING_LISTING', 1, '2022-01-28 11:20:00');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (1, 1, '2022-01-29 11:20:00', '2022-01-29 11:20:00', 'venue1-1', 'room1-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (2, 1, '2022-01-29 12:20:00', '2022-01-29 12:20:00', 'venue1-1', 'room1-1');

insert into case_hearing_request (
auto_list_flag, is_a_panel_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ('t'	, true, 'hearingType1',	60,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000005	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (3, '2000000005', '2022-01-29 11:20:00', 'DRAFT', 'CASE_CREATED', 1);

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (3, 3, '2022-01-29 11:20:00', '2022-01-29 11:20:00', 'venue1-1', 'room1-1');


insert into hearing ( hearing_id, status) values ('2000000010', 'LISTED');
insert into case_hearing_request (
  auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType3',	63,	'Priority type3',	6,'t','t','AB123',null,'TEST'	,9372710950276233,	'2020-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC333',	't',	'2021-10-10 00:00:00',	1,	2000000010	,'f'	,	'Some listing comments3',	'Some judge3',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (4, '2000000010', '2020-08-10 11:20:00', 'listingStatus1-1', 'LISTED', '1');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (4, 4, '2021-01-10 11:20:00', '2021-08-10 11:20:00', 'venue1-1', 'room1-1');

insert into case_hearing_request (
auto_list_flag, is_a_panel_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ('t'	, false, 'hearingType1',	25,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000009	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (5, '2000000009', '2022-01-10 11:20:00', 'listingStatus1-1', 'LISTED', '2');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (5, 5, '2022-01-10 11:20:00', '2022-01-10 11:20:00', 'venue1-1', 'room1-1');

insert into case_hearing_request (
auto_list_flag, is_a_panel_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ('t'	, false, 'hearingType1',	25,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	2,	2000000011	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (6, '2000000011', '2022-01-10 11:20:00', 'listingStatus1-1', 'LISTED', '2');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (6, 6, '2022-01-10 11:20:00', '2022-01-10 11:20:00', 'venue1-1', 'room1-1');

insert into actual_hearing( actual_hearing_type, actual_hearing_is_final_flag, hearing_result_type, hearing_result_reason_type, hearing_result_date, hearing_response_id)
values ('Test Hearing Type', true, 'ADJOURNED', 'ADJOURNED reason', '2020-08-10 11:20:00', 6);

insert into case_hearing_request (
auto_list_flag, is_a_panel_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ('t'	, false, 'hearingType1',	30,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9372710950276233,	'2021-09-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'BCC123',	't',	'2021-10-10 00:00:00',	1,	2000000012	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (7, '2000000012', '2021-09-10 11:20:00', 'listingStatus1-1', 'LISTED', '1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (7, 7, '2021-09-10 11:20:00', '2021-09-10 11:20:00', 'venue1-1', 'room1-1');

insert into hearing ( hearing_id, status) values ('2000000013', 'CANCELLED');
insert into case_hearing_request (
auto_list_flag, is_a_panel_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ('t'	, false, 'hearingType1',	25,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	2,	2000000013	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (8, '2000000013', '2022-01-10 11:20:00', 'listingStatus1-1', 'LISTED', '2');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (8, 8, '2021-02-10 11:20:00', '2022-01-10 11:20:00', 'venue2-1', 'room2-1');

insert into actual_hearing( actual_hearing_type, actual_hearing_is_final_flag, hearing_result_type, hearing_result_reason_type, hearing_result_date, hearing_response_id)
values ('Test Hearing Type', false, 'CANCELLED', 'CANCELLED reason', '2020-02-01 11:20:00', 8);

insert into hearing ( hearing_id, status) values ('2000000014', 'ADJOURNED');
insert into case_hearing_request (
auto_list_flag, is_a_panel_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ('t'	, false, 'hearingType1',	25,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	2,	2000000014	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (9, '2000000014', '2021-01-10 11:20:00', 'listingStatus5-1', 'LISTED', '2');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (9, 9, '2021-02-10 11:20:00', '2021-01-10 11:20:00', 'venue7-1', 'room7-1');

insert into actual_hearing( actual_hearing_type, actual_hearing_is_final_flag, hearing_result_type, hearing_result_reason_type, hearing_result_date, hearing_response_id)
values ('Test Hearing Type', false, 'ADJOURNED', 'ADJOURNED reason', '2021-01-11 11:20:00', 9);
