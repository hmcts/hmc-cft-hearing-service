ALTER TABLE public.case_hearing_request DROP COLUMN listing_comments;

ALTER TABLE public.individual_detail DROP COLUMN related_party_relationship_type;

ALTER TABLE public.individual_detail DROP COLUMN related_party_id;

ALTER TABLE public.required_locations DROP COLUMN location_id;

ALTER TABLE public.required_locations DROP COLUMN location_level_type;

ALTER TABLE public.case_hearing_request ADD COLUMN listing_comments VARCHAR(2000);

ALTER TABLE public.individual_detail ADD COLUMN related_party_relationship_type VARCHAR(10) not null;

ALTER TABLE public.individual_detail ADD COLUMN related_party_id VARCHAR(15) not null;

ALTER TABLE public.required_locations ADD COLUMN location_id VARCHAR not null;

CREATE TYPE public.locationtype AS ENUM (
    'COURT',
    'CLUSTER',
    'REGION'
);

ALTER TABLE public.required_locations ADD COLUMN location_level_type public.locationtype;



