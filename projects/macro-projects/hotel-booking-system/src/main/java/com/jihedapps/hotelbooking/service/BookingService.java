package com.jihedapps.hotelbooking.service;

import com.jihedapps.hotelbooking.entity.Booking;
import com.jihedapps.hotelbooking.entity.BookingStatus;
import com.jihedapps.hotelbooking.entity.Guest;
import com.jihedapps.hotelbooking.entity.Room;
import com.jihedapps.hotelbooking.exception.InvalidBookingException;
import com.jihedapps.hotelbooking.exception.ResourceNotFoundException;
import com.jihedapps.hotelbooking.exception.RoomUnavailableException;
import com.jihedapps.hotelbooking.repository.BookingRepository;
import com.jihedapps.hotelbooking.repository.GuestRepository;
import com.jihedapps.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
                           GuestRepository guestRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
    }

    /**
     * Deux periodes [startA, endA) et [startB, endB) se chevauchent si
     * chacune commence avant que l'autre ne se termine. Les dates de
     * checkout et checkin le meme jour ne sont PAS considerees comme un
     * chevauchement (une chambre liberee le matin peut etre reprise le
     * jour meme), d'ou l'usage d'intervalles semi-ouverts.
     */
    public static boolean datesOverlap(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    /**
     * Prix total = nombre de nuits x prix/nuit de la chambre. Le prix/nuit
     * est deja specifique au type de chambre (voir Room.pricePerNight),
     * donc le type est pris en compte indirectement via la chambre choisie.
     */
    public static BigDecimal calculateTotalPrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            throw new InvalidBookingException("La date de depart doit etre posterieure a la date d'arrivee.");
        }
        return room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
    }

    @Transactional
    public Booking createBooking(Long roomId, Long guestId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidBookingException("Les dates d'arrivee et de depart sont obligatoires.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidBookingException("La date de depart doit etre posterieure a la date d'arrivee.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidBookingException("La date d'arrivee ne peut pas etre dans le passe.");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chambre introuvable: " + roomId));
        if (room.isOutOfService()) {
            throw new RoomUnavailableException("La chambre " + room.getRoomNumber() + " est hors service.");
        }

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable: " + guestId));

        if (!isRoomAvailable(roomId, checkIn, checkOut)) {
            throw new RoomUnavailableException(
                    "La chambre " + room.getRoomNumber() + " est deja reservee sur cette periode.");
        }

        BigDecimal totalPrice = calculateTotalPrice(room, checkIn, checkOut);
        Booking booking = new Booking(room, guest, checkIn, checkOut, totalPrice);
        return bookingRepository.save(booking);
    }

    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> activeBookings = bookingRepository.findByRoomIdAndStatusNot(roomId, BookingStatus.CANCELLED);
        return activeBookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .noneMatch(b -> datesOverlap(b.getCheckInDate(), b.getCheckOutDate(), checkIn, checkOut));
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation introuvable: " + bookingId));
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingException("Cette reservation est deja annulee.");
        }
        // Annuler libere automatiquement la chambre : une reservation CANCELLED
        // est exclue par isRoomAvailable/datesOverlap pour toute nouvelle demande.
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation introuvable: " + id));
    }

    public List<Booking> findByGuest(Long guestId) {
        return bookingRepository.findByGuestId(guestId);
    }
}
