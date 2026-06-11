alter table members
    add column nickname varchar(30),
    add column profile_image_url varchar(512),
    add column representative_latitude numeric(10, 7),
    add column representative_longitude numeric(10, 7),
    add column representative_region_name varchar(100),
    add constraint chk_members_representative_latitude
        check (representative_latitude is null or representative_latitude between -90 and 90),
    add constraint chk_members_representative_longitude
        check (representative_longitude is null or representative_longitude between -180 and 180),
    add constraint chk_members_representative_location_pair
        check ((representative_latitude is null) = (representative_longitude is null));

update members
set nickname = name
where nickname is null;
