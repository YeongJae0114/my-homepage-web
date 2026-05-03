insert into llm_servers (id, name, host, mac_address, chat_endpoint_url, status, last_wake_requested_at, last_checked_at)
values
    (1, 'Local GPU LLM Server', '192.168.0.10', null, 'http://192.168.0.10:8000/chat', 'OFFLINE', null, null);
