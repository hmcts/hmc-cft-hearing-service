INSERT INTO linked_group_details
(linked_group_id, request_id, request_date_time, link_type, status, linked_group_latest_version)
VALUES
(100, '1', now(), 'ORDERED', 'ACTIVE', 1);

INSERT INTO hearing
(hearing_id, status, linked_group_id, linked_order)
VALUES
(200, 'HEARING_REQUESTED', 100, 1);

INSERT INTO hearing
(hearing_id, status, linked_group_id, linked_order)
VALUES
(201, 'HEARING_REQUESTED', 100, 2);

INSERT INTO hearing_response
(hearing_response_id, hearing_id, received_date_time, listing_case_status, request_version)
VALUES
(300, 200, now(), 'STATUS', 1);

INSERT INTO hearing_day_details
(hearing_day_id, hearing_response_id, start_date_time)
VALUES
(400, 300, now() + INTERVAL '1 day');
