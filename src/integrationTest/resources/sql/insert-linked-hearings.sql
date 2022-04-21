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

INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
VALUES (1, '2000000301', '2020-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '1', '1');
INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
VALUES (2, '2000000301', '2021-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');

-- start date in the future, but hearing_response version is not latest
INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (1, '1', '2920-01-10 11:20:00', '2020-08-10 11:20:00', 'session1-2', 'venue1-2', 'room1-2');

-- start date in the past for the latest hearing_response version
INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (2, '2', '2021-01-10 11:20:00', '2021-08-10 11:20:00', 'session1-1', 'venue1-1', 'room1-1');
INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (3, '2', '2920-01-10 11:20:00', '2020-08-10 11:20:00', 'session1-2', 'venue1-2', 'room1-2');

INSERT INTO linked_group_details (linked_group_id, status, link_type, request_id, request_date_time, reason_for_link, linked_group_latest_version)
VALUES ('7600000301', 'ACTIVE', 'ORDERED', '44448', '2021-08-10 11:20:00', 'good reason', '1');
INSERT INTO hearing (hearing_id, is_linked_flag, linked_group_id, linked_order, status)
VALUES ('2000000302', 't', '7600000301', '2', 'UPDATE_REQUESTED');

INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
VALUES (3, '2000000302', '2021-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');

-- start date in the past for the latest hearing_response version
INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (4, '3', '2021-01-10 11:20:00', '2021-08-10 11:20:00', 'session1-1', 'venue1-1', 'room1-1');

INSERT INTO linked_group_details (linked_group_id, status, link_type, request_id, request_date_time, reason_for_link, linked_group_latest_version) VALUES ('7700000000', 'ACTIVE', 'ORDERED', '12345', '2021-08-10 11:20:00', 'good reason', '1');
INSERT INTO hearing (hearing_id, is_linked_flag, linked_group_id, linked_order, status) VALUES ('2100000005', 't', '7700000000', '1', 'HEARING_REQUESTED');
INSERT INTO hearing (hearing_id, is_linked_flag, linked_group_id, linked_order, status) VALUES ('2100000006', 't', '7700000000', '2', 'HEARING_REQUESTED');

