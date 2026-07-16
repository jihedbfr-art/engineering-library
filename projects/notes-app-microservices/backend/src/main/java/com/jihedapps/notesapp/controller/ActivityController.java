package com.jihedapps.notesapp.controller;

import com.jihedapps.notesapp.dto.ActivityResponse;
import com.jihedapps.notesapp.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityLogRepository activityLogRepository;

    @GetMapping
    public List<ActivityResponse> recent(Authentication auth) {
        return activityLogRepository.findTop50ByUsernameOrderByOccurredAtDesc(auth.getName())
                .stream().map(ActivityResponse::from).toList();
    }
}
