package com.jihedapps.taskmanagement.service;

import com.jihedapps.taskmanagement.entity.User;
import com.jihedapps.taskmanagement.exception.ResourceNotFoundException;
import com.jihedapps.taskmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(String username, String displayName, com.jihedapps.taskmanagement.entity.Role role) {
        userRepository.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("Un utilisateur existe deja avec le username " + username);
        });
        User user = new User(username, displayName, role);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User requireById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : id=" + id));
    }

    @Transactional(readOnly = true)
    public List<User> listAll() {
        return userRepository.findAll();
    }
}
