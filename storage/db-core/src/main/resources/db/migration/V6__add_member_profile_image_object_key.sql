alter table members
    add column profile_image_object_key varchar(512);

create index idx_members_profile_image_object_key
    on members (profile_image_object_key)
    where profile_image_object_key is not null;
