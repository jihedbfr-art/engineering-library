package com.jihedapps.hotelbooking.service;

import com.jihedapps.hotelbooking.entity.Guest;
import com.jihedapps.hotelbooking.exception.InvalidBookingException;
import com.jihedapps.hotelbooking.exception.ResourceNotFoundException;
import com.jihedapps.hotelbooking.repository.GuestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    public Guest createGuest(Guest guest) {
        guestRepository.findByEmail(guest.getEmail()).ifPresent(g -> {
            throw new InvalidBookingException("Un client existe deja avec l'email " + guest.getEmail());
        });
        return guestRepository.save(guest);
    }

    public List<Guest> findAll() {
        return guestRepository.findAll();
    }

    public Guest findById(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable: " + id));
    }
}
