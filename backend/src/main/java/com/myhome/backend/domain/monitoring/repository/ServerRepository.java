package com.myhome.backend.domain.monitoring.repository;

import com.myhome.backend.domain.monitoring.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRepository extends JpaRepository<Server, Long> {
}
