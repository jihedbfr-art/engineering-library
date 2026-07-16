package com.jihedapps.notesapp.repository;

import com.jihedapps.notesapp.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop50ByUsernameOrderByOccurredAtDesc(String username);
}
