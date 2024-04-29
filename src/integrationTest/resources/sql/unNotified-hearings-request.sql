insert into hearing ( hearing_id, status) values ('2100000000', 'LISTED');
insert into hearing ( hearing_id, status) values ('2100000001', 'LISTED');
insert into hearing ( hearing_id, status) values ('2100000003', 'LISTED');
insert into hearing ( hearing_id, status) values ('2100000004', 'LISTED');
insert into hearing ( hearing_id, status) values ('2100000005', 'CANCELLED');

insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag,  listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','ACA2',null,'ACA2'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	2,	2100000000	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag,  listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','ACA2',null,'ACA2'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2100000001	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag,  listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','AAA2',null,'AAA2'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	2,	2100000003	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag,  listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','AAA2',null,'AAA2'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	2,	2100000004	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');
insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag,  listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','AAA2',null,'AAA2'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2100000005	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');


insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (1, '2100000000', '2020-08-10 11:00:00', 'listingStatus1-1', 'caselistingStatus1-1', 2, null);
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (2, '2100000000', '2021-08-10 11:00:00', 'listingStatus1-1', 'caselistingStatus1-1', 2, null);

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (3, '2100000001', '2019-08-10 11:00:00', 'listingStatus1-1', 'caselistingStatus1-1', 1, null);
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (4, '2100000003', '2019-08-10 11:00:00', 'listingStatus1-1', 'caselistingStatus1-1', 1, null);
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (5, '2100000003', '2019-08-10 11:00:00', 'listingStatus1-1', 'caselistingStatus1-1', 2, '2019-08-10 11:00:00');
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (6, '2100000004', '2019-08-10 11:00:00', 'listingStatus1-1', 'caselistingStatus1-1', 1, null);
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (7, '2100000004', '2019-08-10 11:00:00', 'listingStatus1-1', 'caselistingStatus1-1', 2, null);

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (8, '2100000003', '2019-08-10 11:00:00', 'listingStatus1-1', 'caselistingStatus1-1', 2, null);
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (9, '2100000003', '2019-08-10 11:00:00', 'listingStatus1-1', 'caselistingStatus1-1', 2, null);

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version, parties_notified_datetime)
values (10, '2100000005', '2024-08-10 11:00:00', 'listingStatus1-1', 'caselistingStatus1-1', 1, null);

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (1, 1, '2021-01-10 11:00:00', '2020-11-10 11:00:00', 'venue1-1', 'room1-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (2, 1, '2020-01-10 11:00:00', '2020-08-10 11:00:00', 'venue1-2', 'room1-2');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (3, 3, '2020-01-10 11:00:00', '2020-07-10 11:00:00', 'venue2-1', 'room2-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (4, 3, '2021-01-10 11:00:00', '2021-08-10 11:00:00', 'venue1-1', 'room1-1');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (5, 4, '2020-01-10 11:00:00', '2020-07-10 11:00:00', 'venue2-1', 'room2-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (6, 5, '2021-01-10 11:00:00', '2021-08-10 11:00:00', 'venue1-1', 'room1-1');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (7, 6, '2020-01-10 11:00:00', '2020-07-10 11:00:00', 'venue2-1', 'room2-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (8, 6, '2021-01-10 11:00:00', '2021-08-10 11:00:00', 'venue1-1', 'room1-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (9, 7, '2011-01-10 11:00:00', '2010-07-10 11:00:00', 'venue2-1', 'room2-1');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (10, 8, '2023-06-15 09:00:00', '2023-06-15 10:00:00', 'venue2-1', 'room2-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (11, 9, '2023-06-15 12:00:00', '2023-06-16 13:00:00', 'venue2-1', 'room2-1');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (12, 10, null, '2023-06-15 10:00:00', 'venue2-1', 'room2-1');
