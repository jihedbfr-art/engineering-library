package com.jihedapps.taskmanagement.config;

import com.jihedapps.taskmanagement.entity.Role;
import com.jihedapps.taskmanagement.repository.UserRepository;
import com.jihedapps.taskmanagement.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Amorce un ADMIN et un MEMBER de demonstration au premier demarrage,
 * pour pouvoir tester l'API sans etape d'inscription prealable.
 */
@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    public DemoDataInitializer(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userService.createUser("admin", "Administratrice Sonia", Role.ADMIN);
            userService.createUser("jihed", "Jihed Ben Arfa", Role.MEMBER);
        }
    }
}
