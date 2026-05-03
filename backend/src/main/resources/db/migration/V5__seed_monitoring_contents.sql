insert into servers (id, name, host, location, status, cpu_usage_percent, memory_usage_percent, disk_usage_percent, last_checked_at)
values
    (1, 'Local Development Server', 'localhost', 'local', 'UNKNOWN', null, null, null, null),
    (2, 'Personal Service Server', 'dev-server', 'dev', 'UNKNOWN', null, null, null, null);

insert into monitored_services (id, server_id, name, description, base_url, status, uptime_percentage, response_time_ms, last_checked_at)
values
    (1, 1, 'Backend API', 'Spring Boot backend API for local development.', 'http://localhost:8080', 'UNKNOWN', null, null, null),
    (2, 1, 'Frontend App', 'React frontend app for local development.', 'http://localhost:5173', 'UNKNOWN', null, null, null),
    (3, 2, 'Homepage API', 'Deployed API service for the personal platform.', 'https://api.example.com', 'UNKNOWN', null, null, null);
