package com.jihedapps.hotelbooking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class BookingRequest {

    @NotNull
    private Long roomId;

    @NotNull
    private Long guestId;

    @NotNull
    private LocalDate checkInDate;

    @NotNull
    private LocalDate checkOutDate;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
}
