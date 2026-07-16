package com.jihedapps.hotelbooking.controller;

import com.jihedapps.hotelbooking.entity.Room;
import com.jihedapps.hotelbooking.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Room createRoom(@Valid @RequestBody Room room) {
        return roomService.createRoom(room);
    }

    @GetMapping
    public List<Room> listRooms() {
        return roomService.findAll();
    }

    @GetMapping("/{id}")
    public Room getRoom(@PathVariable Long id) {
        return roomService.findById(id);
    }

    @GetMapping("/available")
    public List<Room> availableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return roomService.findAvailableRooms(checkIn, checkOut);
    }
}
