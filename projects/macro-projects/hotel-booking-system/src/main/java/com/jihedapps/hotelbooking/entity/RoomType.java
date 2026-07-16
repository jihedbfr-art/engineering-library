package com.jihedapps.hotelbooking.entity;

/**
 * Type de chambre. Le prix de base par nuit depend du type, mais chaque
 * chambre peut avoir son propre prix (voir Room.pricePerNight) pour
 * refleter des ecarts reels (etage, vue, renovation...).
 */
public enum RoomType {
    SINGLE,
    DOUBLE,
    SUITE,
    FAMILY
}
