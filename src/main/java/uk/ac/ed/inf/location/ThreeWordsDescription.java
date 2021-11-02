package uk.ac.ed.inf.location;

import java.util.ArrayList;

public class ThreeWordsDescription {

    private String words;
    private String language;
    private String map;
    private String country;
    private Square square;
    private String nearestPlace;
    private Coordinate coordinates;

    public class Coordinate {

        private Double lat;

        private Double lng;


        public Coordinate(Double lat, Double lon) {
            this.lat = lat;
            this.lng = lon;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lon) {
            this.lng = lon;
        }

    }

    public class Square {
        private Coordinate southwest;
        private Coordinate northeast;

        public Square(Coordinate southwest, Coordinate northeast) {
            this.southwest = southwest;
            this.northeast = northeast;
        }

        public Coordinate getSouthwest() {
            return southwest;
        }

        public void setSouthwest(Coordinate southwest) {
            this.southwest = southwest;
        }

        public Coordinate getNortheast() {
            return northeast;
        }

        public void setNortheast(Coordinate northeast) {
            this.northeast = northeast;
        }
    }

    public ThreeWordsDescription(String country, Square square, String nearestPlace, Coordinate coordinates, String words, String language, String map) {
        this.country = country;
        this.square = square;
        this.nearestPlace = nearestPlace;
        this.coordinates = coordinates;
        this.words = words;
        this.language = language;
        this.map = map;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Square getSquare() {
        return square;
    }

    public void setSquare(Square square) {
        this.square = square;
    }

    public String getNearestPlace() {
        return nearestPlace;
    }

    public void setNearestPlace(String nearestPlace) {
        this.nearestPlace = nearestPlace;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinate coordinates) {
        this.coordinates = coordinates;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }


}
