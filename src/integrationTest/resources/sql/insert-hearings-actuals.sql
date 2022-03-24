INSERT INTO hearing ( hearing_id, status) VALUES ('2000000000', 'HEARING_REQUESTED');
INSERT INTO hearing ( hearing_id, status) VALUES ('2000000100', 'LISTED');
INSERT INTO hearing ( hearing_id, status) VALUES ('2000000200', 'AWAITING_LISTING');

-------------
INSERT INTO hearing (hearing_id, status) VALUES ('2000000302', 'UPDATE_REQUESTED');

INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
VALUES (1, '2000000302', '2021-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');

INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (1, '1', '2022-01-31 11:20:00', '2022-01-31 14:20:00', 'session1-1', 'venue1-1', 'room1-1');
INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (2, '1', '2022-02-12 11:21:00', '2022-02-12 14:20:00', 'session1-1', 'venue1-1', 'room1-1');

-------------
INSERT INTO hearing ( hearing_id, status) VALUES ('2000001000', 'LISTED');

INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
VALUES (2, '2000001000', '2021-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');

INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (3, '2', '2022-01-28 11:20:00', '2022-01-31 14:20:00', 'session1-1', 'venue1-1', 'room1-1');

-------------
INSERT INTO hearing ( hearing_id, status) VALUES ('2000001100', 'UPDATE_REQUESTED');

INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
VALUES (3, '2000001100', '2021-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');

INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (4, '3', '2022-01-28 11:20:00', '2022-01-31 14:20:00', 'session1-1', 'venue1-1', 'room1-1');

-------------
INSERT INTO hearing ( hearing_id, status) VALUES ('2000001200', 'UPDATE_SUBMITTED');

INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
VALUES (4, '2000001200', '2021-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');

INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (5, '4', '2022-01-28 11:20:00', '2022-01-31 14:20:00', 'session1-1', 'venue1-1', 'room1-1');
