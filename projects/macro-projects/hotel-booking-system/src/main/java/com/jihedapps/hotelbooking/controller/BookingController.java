package com.jihedapps.hotelbooking.controller;

import com.jihedapps.hotelbooking.dto.BookingRequest;
import com.jihedapps.hotelbooking.entity.Booking;
import com.jihedapps.hotelbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(@Valid @RequestBody BookingRequest request) {
        return bookingService.createBooking(request.getRoomId(), request.getGuestId(),
                request.getCheckInDate(), request.getCheckOutDate());
    }

    @GetMapping
    public List<Booking> listBookings() {
        return bookingService.findAll();
    }

    @GetMapping("/{id}")
    public Booking getBooking(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @GetMapping("/guest/{guestId}")
    public List<Booking> bookingsByGuest(@PathVariable Long guestId) {
        return bookingService.findByGuest(guestId);
    }

    @PostMapping("/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }
}
