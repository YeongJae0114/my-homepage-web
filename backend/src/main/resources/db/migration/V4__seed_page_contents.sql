insert into page_contents (id, page_type, title, subtitle, description, display_order)
values
    (1, 'HOME', 'Java/Spring Backend Engineer', '운영 서비스와 기술 기록을 연결하는 개인 기술 플랫폼', '백엔드 개발자의 포트폴리오를 넘어 서비스 상태, 프로젝트, 블로그, 실험 기능을 한 곳에서 보여줍니다.', 1),
    (2, 'ABOUT', 'About', '문제를 끝까지 추적하고 운영 가능한 형태로 만드는 백엔드 개발자', 'Java, Spring, JPA, 인프라 운영 경험을 바탕으로 안정적인 서비스를 설계하고 개선합니다.', 2),
    (3, 'PROJECT', 'Projects', '서비스와 실험을 기록하는 프로젝트 아카이브', '개인 서비스, 백엔드 실험, 인프라 자동화, LLM 기능을 프로젝트 단위로 정리합니다.', 3);

insert into page_heroes (id, page_id, headline, sub_headline, description, primary_action_label, primary_action_url)
values
    (1, 1, 'Java/Spring 기반 개인 기술 플랫폼', '포트폴리오, 운영 상태, 기술 기록, 실험 기능을 연결합니다.', '운영 중인 서비스와 개발 경험을 한 화면에서 탐색할 수 있도록 구성합니다.', 'View Projects', '/project'),
    (2, 2, '운영을 이해하는 백엔드 개발자', '도메인 모델링, API 설계, 데이터 정합성, 배포 환경을 함께 봅니다.', '서비스가 오래 살아남기 위한 구조와 관찰 가능성을 중요하게 생각합니다.', 'Contact', 'mailto:hello@example.com'),
    (3, 3, '프로젝트로 증명하는 기술 기록', 'Spring Boot, JPA, Docker, 모니터링, LLM 실험을 담습니다.', '개인 플랫폼을 구성하는 기능들을 작은 프로젝트 단위로 확장합니다.', 'GitHub', 'https://github.com');

insert into page_sections (id, page_id, section_key, title, description, display_order)
values
    (1, 1, 'overview', 'Overview', '개인 기술 브랜드 페이지이자 확장 가능한 기술 플랫폼입니다.', 1),
    (2, 1, 'services', 'Services', '운영 중인 서비스와 실험 기능의 상태를 보여줍니다.', 2),
    (3, 2, 'principles', 'Engineering Principles', '단순 구현보다 유지보수 가능한 구조와 운영 안정성을 우선합니다.', 1),
    (4, 2, 'experiences', 'Experiences', '백엔드 개발과 서비스 운영 경험을 정리합니다.', 2),
    (5, 3, 'featured', 'Featured Projects', '플랫폼을 구성하는 주요 프로젝트 목록입니다.', 1);

insert into page_section_items (section_id, item_order, item)
values
    (1, 0, 'Gather Town 스타일의 2D 인터랙티브 Home 화면'),
    (1, 1, '기술 블로그, 프로젝트, 서비스 상태를 통합'),
    (2, 0, '서비스 상태 모니터링'),
    (2, 1, 'Local LLM 및 RAG 실험 확장'),
    (3, 0, '명확한 계층 분리와 테스트 가능한 설계'),
    (3, 1, '운영 환경을 고려한 설정과 배포 전략'),
    (4, 0, 'Spring Boot 기반 API 설계'),
    (4, 1, 'JPA 기반 도메인 모델링'),
    (5, 0, '개인 홈페이지 플랫폼'),
    (5, 1, '서버 모니터링 대시보드'),
    (5, 2, '로컬 LLM 제어 및 채팅 기능');

insert into page_skills (id, page_id, name, category, level, display_order)
values
    (1, 2, 'Java', 'Backend', 'advanced', 1),
    (2, 2, 'Spring Boot', 'Backend', 'advanced', 2),
    (3, 2, 'JPA', 'Persistence', 'advanced', 3),
    (4, 2, 'PostgreSQL', 'Database', 'intermediate', 4),
    (5, 2, 'Docker', 'Infrastructure', 'intermediate', 5),
    (6, 2, 'Monitoring', 'Operations', 'intermediate', 6);

insert into page_experiences (id, page_id, company, role, period, description, display_order)
values
    (1, 2, 'Personal Platform', 'Backend Engineer', '2026', '개인 기술 플랫폼의 API, 데이터 모델, 배포 환경을 설계합니다.', 1);

insert into page_experience_highlights (experience_id, highlight_order, highlight)
values
    (1, 0, 'Spring Boot 기반 계층형 백엔드 구조 설계'),
    (1, 1, 'Docker Compose 기반 로컬 개발환경 구성'),
    (1, 2, 'Flyway 기반 스키마 및 초기 데이터 버전 관리');

insert into project_items (id, page_id, title, summary, description, repository_url, service_url, display_order)
values
    (1, 3, 'My Homepage Platform', '개인 기술 브랜드 페이지와 운영 플랫폼', '프로필, 프로젝트, 모니터링, 블로그, LLM 실험 기능을 통합하는 개인 플랫폼입니다.', 'https://github.com', null, 1),
    (2, 3, 'Monitoring Dashboard', '서버 및 서비스 상태 조회 API', '운영 중인 서버와 서비스의 상태, 응답 시간, 리소스 사용률을 조회합니다.', null, null, 2),
    (3, 3, 'Local LLM Control', '로컬 GPU 서버 Wake 및 Chat API', 'WOL 기반 서버 제어와 로컬 LLM 채팅 연동을 목표로 하는 실험 기능입니다.', null, null, 3);

insert into project_item_tech_stacks (project_id, tech_stack_order, tech_stack)
values
    (1, 0, 'Spring Boot'),
    (1, 1, 'React'),
    (1, 2, 'PostgreSQL'),
    (2, 0, 'Spring Boot'),
    (2, 1, 'JPA'),
    (2, 2, 'Docker'),
    (3, 0, 'Spring Boot'),
    (3, 1, 'Wake-on-LAN'),
    (3, 2, 'Local LLM');
