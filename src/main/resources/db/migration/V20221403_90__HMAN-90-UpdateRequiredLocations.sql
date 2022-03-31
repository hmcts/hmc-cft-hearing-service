ALTER TABLE public.case_hearing_request DROP COLUMN listing_comments;

ALTER TABLE public.individual_detail DROP COLUMN related_party_relationship_type;

ALTER TABLE public.individual_detail DROP COLUMN related_party_id;

ALTER TABLE public.required_locations DROP COLUMN location_id;

ALTER TABLE public.required_locations DROP COLUMN location_level_type;

ALTER TABLE public.case_hearing_request ADD COLUMN listing_comments VARCHAR(2000);

ALTER TABLE public.individual_detail ADD COLUMN related_party_relationship_type VARCHAR(10) default 'DEFAULT';

ALTER TABLE public.individual_detail ALTER COLUMN related_party_relationship_type DROP DEFAULT;

ALTER TABLE public.individual_detail ADD COLUMN related_party_id VARCHAR(15) default 'DEFAULT';

ALTER TABLE public.individual_detail ALTER COLUMN related_party_id DROP DEFAULT;

ALTER TABLE public.required_locations ADD COLUMN location_id VARCHAR not null default 'DEFAULT';

ALTER TABLE public.required_locations ALTER COLUMN location_id DROP DEFAULT;

CREATE TYPE public.locationtype AS ENUM (
    'COURT',
    'CLUSTER',
    'REGION'
);

ALTER TABLE public.required_locations ADD COLUMN location_level_type public.locationtype;
