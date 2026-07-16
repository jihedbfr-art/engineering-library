package com.jihedapps.hotelbooking.controller;

import com.jihedapps.hotelbooking.entity.Guest;
import com.jihedapps.hotelbooking.service.GuestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Guest createGuest(@Valid @RequestBody Guest guest) {
        return guestService.createGuest(guest);
    }

    @GetMapping
    public List<Guest> listGuests() {
        return guestService.findAll();
    }

    @GetMapping("/{id}")
    public Guest getGuest(@PathVariable Long id) {
        return guestService.findById(id);
    }
}
