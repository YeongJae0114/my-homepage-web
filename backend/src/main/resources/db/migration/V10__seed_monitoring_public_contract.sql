update servers
set description = 'Prometheus, Grafana, Loki를 실행하는 홈랩 모니터링 서버',
    server_role = 'monitoring'
where name = 'raspi-observe';

update servers
set description = '개인 프로젝트와 홈랩 서비스에서 사용하는 Redis 서버',
    server_role = 'cache'
where name = 'raspi-redis';

update servers
set description = '토이 프로젝트와 실험용 서비스를 실행하는 미니 PC 서버',
    server_role = 'toy-host'
where name = 'lab-mini';

update servers
set description = 'Spring API와 주요 백엔드 서비스를 실행하는 메인 홈랩 서버',
    server_role = 'api'
where name = 'lab-main';

update servers
set description = '개인 프로젝트 데이터 저장을 위한 데이터베이스 서버',
    server_role = 'database'
where name = 'raspi-db';

insert into server_tags (server_id, tag_order, tag)
values
    (101, 0, 'raspberry-pi'),
    (101, 1, 'monitoring'),
    (101, 2, 'internal'),
    (102, 0, 'raspberry-pi'),
    (102, 1, 'redis'),
    (102, 2, 'internal'),
    (103, 0, 'mini-pc'),
    (103, 1, 'toy'),
    (103, 2, 'internal'),
    (104, 0, 'spring'),
    (104, 1, 'docker'),
    (104, 2, 'backend'),
    (105, 0, 'raspberry-pi'),
    (105, 1, 'database'),
    (105, 2, 'internal');

insert into monitored_services (
    server_id,
    service_key,
    name,
    description,
    base_url,
    health_check_url,
    service_type,
    public_visible,
    status,
    uptime_percentage,
    response_time_ms,
    last_checked_at
)
values
    (104, 'homepage-api', 'Homepage API', '개인 홈페이지 콘텐츠와 상태 데이터를 제공하는 Spring API', 'http://192.168.1.105:8081', 'http://192.168.1.105:8081/actuator/health', 'api', false, 'UNKNOWN', null, null, null),
    (101, 'prometheus', 'Prometheus', '홈랩 서버 메트릭을 수집하고 저장하는 모니터링 수집기', 'http://192.168.1.104:9090', 'http://192.168.1.104:9090/-/ready', 'monitoring', false, 'UNKNOWN', null, null, null);

insert into monitored_service_tags (service_id, tag_order, tag)
select id, 0, 'spring'
from monitored_services
where service_key = 'homepage-api';

insert into monitored_service_tags (service_id, tag_order, tag)
select id, 1, 'api'
from monitored_services
where service_key = 'homepage-api';

insert into monitored_service_tags (service_id, tag_order, tag)
select id, 2, 'backend'
from monitored_services
where service_key = 'homepage-api';

insert into monitored_service_tags (service_id, tag_order, tag)
select id, 0, 'prometheus'
from monitored_services
where service_key = 'prometheus';

insert into monitored_service_tags (service_id, tag_order, tag)
select id, 1, 'metrics'
from monitored_services
where service_key = 'prometheus';

insert into monitored_service_tags (service_id, tag_order, tag)
select id, 2, 'internal'
from monitored_services
where service_key = 'prometheus';
