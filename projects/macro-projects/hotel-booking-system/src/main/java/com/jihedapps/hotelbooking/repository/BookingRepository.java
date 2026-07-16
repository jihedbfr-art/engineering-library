package com.jihedapps.hotelbooking.repository;

import com.jihedapps.hotelbooking.entity.Booking;
import com.jihedapps.hotelbooking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByRoomIdAndStatusNot(Long roomId, BookingStatus status);

    List<Booking> findByGuestId(Long guestId);

    List<Booking> findByStatus(BookingStatus status);
}
