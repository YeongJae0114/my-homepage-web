delete from monitored_services;
delete from servers;

insert into servers (
    id,
    name,
    host,
    location,
    server_role,
    metrics_url,
    enabled,
    display_order,
    status,
    cpu_usage_percent,
    memory_usage_percent,
    disk_usage_percent,
    last_checked_at
)
values
    (101, 'raspi-observe', '192.168.1.104', 'LAN', 'monitoring', 'http://192.168.1.104:9100/metrics', true, 1, 'UNKNOWN', null, null, null, null),
    (102, 'raspi-redis', '192.168.1.103', 'LAN', 'redis', 'http://192.168.1.103:9100/metrics', true, 2, 'UNKNOWN', null, null, null, null),
    (103, 'lab-mini', '192.168.1.102', 'LAN', 'toy-projects', 'http://192.168.1.102:9100/metrics', true, 3, 'UNKNOWN', null, null, null, null),
    (104, 'lab-main', '192.168.1.105', 'LAN', 'llm', 'http://192.168.1.105:9100/metrics', true, 4, 'UNKNOWN', null, null, null, null),
    (105, 'raspi-db', '192.168.1.106', 'LAN', 'database', 'http://192.168.1.106:9100/metrics', true, 5, 'UNKNOWN', null, null, null, null);
