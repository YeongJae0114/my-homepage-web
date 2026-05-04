alter table servers add column description varchar(500);

update servers
set description = name
where description is null;

alter table servers alter column description set not null;

create table server_tags (
    server_id bigint not null,
    tag_order integer not null,
    tag varchar(80) not null,
    constraint pk_server_tags primary key (server_id, tag_order),
    constraint fk_server_tags_server_id foreign key (server_id) references servers (id)
);

alter table monitored_services add column service_key varchar(80);
alter table monitored_services add column health_check_url varchar(500);
alter table monitored_services add column service_type varchar(80);
alter table monitored_services add column public_visible boolean;

update monitored_services
set service_key = lower(replace(name, ' ', '-')),
    service_type = 'service',
    public_visible = false
where service_key is null;

alter table monitored_services alter column service_key set not null;
alter table monitored_services alter column service_type set not null;
alter table monitored_services alter column public_visible set not null;

alter table monitored_services add constraint uk_monitored_services_service_key unique (service_key);

create table monitored_service_tags (
    service_id bigint not null,
    tag_order integer not null,
    tag varchar(80) not null,
    constraint pk_monitored_service_tags primary key (service_id, tag_order),
    constraint fk_monitored_service_tags_service_id foreign key (service_id) references monitored_services (id)
);

alter table monitoring_snapshots add column memory_available_bytes double precision;
alter table monitoring_snapshots add column memory_total_bytes double precision;
alter table monitoring_snapshots add column disk_available_bytes double precision;
alter table monitoring_snapshots add column disk_total_bytes double precision;
