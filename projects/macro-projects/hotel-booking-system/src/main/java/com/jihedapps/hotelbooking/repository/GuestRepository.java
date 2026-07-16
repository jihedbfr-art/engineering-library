package com.jihedapps.hotelbooking.repository;

import com.jihedapps.hotelbooking.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByEmail(String email);
}
