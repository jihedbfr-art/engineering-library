package com.jihedapps.hotelbooking.service;

import com.jihedapps.hotelbooking.entity.Booking;
import com.jihedapps.hotelbooking.entity.BookingStatus;
import com.jihedapps.hotelbooking.entity.Guest;
import com.jihedapps.hotelbooking.entity.Room;
import com.jihedapps.hotelbooking.entity.RoomType;
import com.jihedapps.hotelbooking.exception.InvalidBookingException;
import com.jihedapps.hotelbooking.exception.ResourceNotFoundException;
import com.jihedapps.hotelbooking.exception.RoomUnavailableException;
import com.jihedapps.hotelbooking.repository.BookingRepository;
import com.jihedapps.hotelbooking.repository.GuestRepository;
import com.jihedapps.hotelbooking.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private BookingService bookingService;

    private Room room;
    private Guest guest;

    @BeforeEach
    void setUp() {
        room = new Room("101", RoomType.DOUBLE, new BigDecimal("100.00"));
        room.setId(1L);
        guest = new Guest("Alice Martin", "alice@example.com", "0600000000");
        guest.setId(1L);
    }

    // ---- Detection de chevauchement de dates ----

    @Test
    void datesOverlap_shouldDetectPartialOverlapAtStart() {
        assertTrue(BookingService.datesOverlap(
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 12)));
    }

    @Test
    void datesOverlap_shouldDetectPartialOverlapAtEnd() {
        assertTrue(BookingService.datesOverlap(
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 1, 13), LocalDate.of(2026, 1, 20)));
    }

    @Test
    void datesOverlap_shouldDetectFullyContainedPeriod() {
        assertTrue(BookingService.datesOverlap(
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20),
                LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 15)));
    }

    @Test
    void datesOverlap_shouldDetectPeriodThatEnglobesExisting() {
        assertTrue(BookingService.datesOverlap(
                LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20)));
    }

    @Test
    void datesOverlap_shouldReturnFalseWhenNoOverlap() {
        assertFalse(BookingService.datesOverlap(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15)));
    }

    @Test
    void datesOverlap_shouldReturnFalseWhenCheckoutMatchesCheckin() {
        // Checkout le 10 et checkin le 10 le meme jour : pas un chevauchement
        // (intervalle semi-ouvert), la chambre peut etre reprise le jour meme.
        assertFalse(BookingService.datesOverlap(
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15)));
    }

    @Test
    void isRoomAvailable_shouldReturnFalseWhenActiveBookingOverlaps() {
        Booking existing = new Booking(room, guest, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 10), new BigDecimal("900"));
        existing.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByRoomIdAndStatusNot(1L, BookingStatus.CANCELLED))
                .thenReturn(List.of(existing));

        boolean available = bookingService.isRoomAvailable(1L, LocalDate.of(2026, 2, 5), LocalDate.of(2026, 2, 8));

        assertFalse(available);
    }

    @Test
    void isRoomAvailable_shouldReturnTrueWhenNoOverlap() {
        Booking existing = new Booking(room, guest, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 10), new BigDecimal("900"));
        existing.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByRoomIdAndStatusNot(1L, BookingStatus.CANCELLED))
                .thenReturn(List.of(existing));

        boolean available = bookingService.isRoomAvailable(1L, LocalDate.of(2026, 2, 11), LocalDate.of(2026, 2, 15));

        assertTrue(available);
    }

    // ---- Calcul du prix total ----

    @Test
    void calculateTotalPrice_shouldMultiplyNightsByPricePerNight() {
        BigDecimal total = BookingService.calculateTotalPrice(room, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5));
        assertEquals(0, new BigDecimal("400.00").compareTo(total));
    }

    @Test
    void calculateTotalPrice_shouldHandleSingleNight() {
        BigDecimal total = BookingService.calculateTotalPrice(room, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2));
        assertEquals(0, new BigDecimal("100.00").compareTo(total));
    }

    @Test
    void calculateTotalPrice_shouldReflectRoomType() {
        Room suite = new Room("201", RoomType.SUITE, new BigDecimal("300.00"));
        BigDecimal total = BookingService.calculateTotalPrice(suite, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 4));
        assertEquals(0, new BigDecimal("900.00").compareTo(total));
    }

    @Test
    void calculateTotalPrice_shouldRejectSameDayCheckout() {
        assertThrows(InvalidBookingException.class,
                () -> BookingService.calculateTotalPrice(room, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1)));
    }

    // ---- Creation de reservation (integration des regles) ----

    @Test
    void createBooking_shouldThrowWhenRoomAlreadyBookedOnOverlappingPeriod() {
        LocalDate baseStart = LocalDate.now().plusDays(20);
        Booking existing = new Booking(room, guest, baseStart, baseStart.plusDays(9), new BigDecimal("900"));
        existing.setStatus(BookingStatus.CONFIRMED);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(bookingRepository.findByRoomIdAndStatusNot(1L, BookingStatus.CANCELLED))
                .thenReturn(List.of(existing));

        assertThrows(RoomUnavailableException.class, () ->
                bookingService.createBooking(1L, 1L, baseStart.plusDays(4), baseStart.plusDays(7)));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_shouldSucceedAndComputePriceWhenNoConflict() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(bookingRepository.findByRoomIdAndStatusNot(1L, BookingStatus.CANCELLED))
                .thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(3);
        Booking result = bookingService.createBooking(1L, 1L, checkIn, checkOut);

        assertEquals(0, new BigDecimal("300.00").compareTo(result.getTotalPrice()));
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void createBooking_shouldRejectCheckoutBeforeCheckin() {
        assertThrows(InvalidBookingException.class, () ->
                bookingService.createBooking(1L, 1L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(2)));
    }

    @Test
    void createBooking_shouldThrowWhenRoomNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bookingService.createBooking(99L, 1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)));
    }

    // ---- Annulation ----

    @Test
    void cancelBooking_shouldSetStatusToCancelledAndFreeTheRoom() {
        Booking booking = new Booking(room, guest, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 5), new BigDecimal("400"));
        booking.setId(1L);
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking cancelled = bookingService.cancelBooking(1L);

        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void cancelBooking_shouldRejectDoubleCancellation() {
        Booking booking = new Booking(room, guest, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 5), new BigDecimal("400"));
        booking.setId(1L);
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(InvalidBookingException.class, () -> bookingService.cancelBooking(1L));
    }
}
