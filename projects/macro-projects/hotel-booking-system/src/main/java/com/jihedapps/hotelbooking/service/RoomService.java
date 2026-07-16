package com.jihedapps.hotelbooking.service;

import com.jihedapps.hotelbooking.entity.Room;
import com.jihedapps.hotelbooking.exception.InvalidBookingException;
import com.jihedapps.hotelbooking.exception.ResourceNotFoundException;
import com.jihedapps.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingService bookingService;

    public RoomService(RoomRepository roomRepository, BookingService bookingService) {
        this.roomRepository = roomRepository;
        this.bookingService = bookingService;
    }

    public Room createRoom(Room room) {
        if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
            throw new InvalidBookingException("Le numero de chambre " + room.getRoomNumber() + " existe deja.");
        }
        return roomRepository.save(room);
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chambre introuvable: " + id));
    }

    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAll().stream()
                .filter(r -> !r.isOutOfService())
                .filter(r -> bookingService.isRoomAvailable(r.getId(), checkIn, checkOut))
                .toList();
    }
}
