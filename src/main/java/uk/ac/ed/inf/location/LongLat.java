package uk.ac.ed.inf.location;

import com.mapbox.geojson.*;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;


/**
 * class to represent drones specific location and properties
 */
public class LongLat {
    public double longitude;
    public double latitude;

    private static final double longBound1 = -3.184319;
    private static final double longBound2 = -3.192473;
    private static final double latBound1 = 55.942617;
    private static final double latBound2 = 55.946233;

    public LongLat(ThreeWordsDescription.Coordinate coordinates) {
        double lat = coordinates.getLat();
        double lon = coordinates.getLng();

    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }


    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * no parameter method to ensure drone stays within the longitude/latitude
     * confinement area
     *
     * @return True if drone is within confinement are or False if drone is outwith boundaries
     */
    public boolean isConfined() {
        return getLongitude() <= longBound1 && getLongitude() >= longBound2 && getLatitude() >= latBound1 && getLatitude() <= latBound2;

    }

    /**
     * method to calculate the absolute distance between 2 points; one being
     * the drones location and the second being a LongLat class passed as a parameter
     *
     * @param point2 Longlat class with a longitude/latitude as part of its attributes
     * @return a double of the distance calculated by the 2 points or 0 if their is no distance between.
     */
    public double distanceTo(LongLat point2) {

        double dist = 0;
        double distsqr = Math.pow((this.longitude - point2.longitude), 2) + Math.pow((this.latitude - point2.latitude), 2);
        dist = Math.pow(distsqr, 0.5);
        return dist;

    }

    /**
     * method to calculate if drone is within the delivery distance threshold
     *
     * @param point2 Longlat class with a longitude/latitude as part of its attributes
     * @return True if the drone is within the absolute distance of the threshold required or false if it
     * is outwith
     */
    public boolean closeTo(LongLat point2) {
        double dThresh = 0.00015;
        //condition to check drone is within threshold
        return !(distanceTo(point2) > dThresh);

    }

    /**
     * method to calculate the next position of the drone given the angle of its next move
     * based on mathematical to calculate this on a 2D plane
     *
     * @param angle direction drone is to move in multiples of 10 from 0 to 350
     * @return LongLat class of the drone containing the attributes of
     * its next given longitude and latitude
     */
    public LongLat nextPosition(int angle) {
        LongLat newPos = new LongLat(0, 0);
        if (angle % 10 == 0 && angle <= 350) { // ensure angle is in range and multiple of 10

            double brng = angle;
            double d = 0.00015;

            double lat1 = this.latitude;
            double lon1 = this.longitude;

            //calculate new longitude and latitude formulas
            double lon2 = lon1 + (d * Math.cos(brng * (Math.PI / 180)));
            double lat2 = lat1 + (d * Math.sin(brng * (Math.PI / 180)));


            //populates new class with new position
            newPos = new LongLat(lon2, lat2);

        } else if (angle == -999) { // case for is drone is to hover still
            newPos = new LongLat(this.longitude, this.latitude);
        }else{
            throw  new IllegalArgumentException("angle is not within parameters");
        }
        return newPos;
    }



    public ArrayList<LongLat> landmarksToLNLT(List<Feature> landmarksFC){
        ArrayList<LongLat> landmarksLonLat = new ArrayList<>();
        for(int i =0; i< landmarksFC.size();i++) {
            Feature landmarks = landmarksFC.get(i);
            Point findingcoords = (Point) landmarks.geometry();
            landmarksLonLat.add(new LongLat(findingcoords.longitude(),findingcoords.latitude()));

        }

        return landmarksLonLat;
    }






    public LongLat closestDelivery(ArrayList<LongLat> deliveries, LongLat currentPos){
        double smallestdist = 100;
        int deliveryRef = 0;
        //get shortest distance
        for (int d = 0; d < deliveries.size(); d++) {
            if (deliveries.get(d).distanceTo(currentPos) < smallestdist) {
                smallestdist = deliveries.get(d).distanceTo(currentPos);
                deliveryRef = d;

            }
        }
        return  deliveries.get(deliveryRef);
    }

    public String matchDeliveries(LongLat delivery, ArrayList<ThreeWordsDescription> allDeliveriesLoc, ArrayList<ArrayList<String>> userOrder) {
        String ThreeWords = null;
        String orderNo = null;
        for (int i = 0; i < allDeliveriesLoc.size(); i++) {
            if (delivery.longitude == allDeliveriesLoc.get(i).getCoordinates().getLng() && delivery.latitude == allDeliveriesLoc.get(i).getCoordinates().getLat()) {
                ThreeWords = allDeliveriesLoc.get(i).getWords();
                for (int y = 0; y < userOrder.size(); y++) {
                    if (ThreeWords.equals(userOrder.get(y).get(2))) {
                        orderNo = userOrder.get(y).get(0);
                    }
                }
            }
        }
        return orderNo;
    }

        public String matchThreeWord(LongLat delivery, ArrayList<ThreeWordsDescription> localDeliver) {
            String ThreeWords = null;
            for (int i = 0; i < localDeliver.size(); i++) {
                if (delivery.longitude == localDeliver.get(i).getCoordinates().getLng() && delivery.latitude == localDeliver.get(i).getCoordinates().getLat()) {
                    ThreeWords = localDeliver.get(i).getWords();

                }
            }
            return ThreeWords;
        }


    public int getshortestDeliverRef(ArrayList<LongLat>deliveries, LongLat currentPos){
        double smallestdist = 100;
        int deliveryRef = 0;
        //get shortest distance
        for (int d = 0; d < deliveries.size(); d++) {
            if (deliveries.get(d).distanceTo(currentPos) < smallestdist) {
                smallestdist = deliveries.get(d).distanceTo(currentPos);
                deliveryRef = d;

            }
        }
        return  deliveryRef;
    }



    public int chooseAngle(LongLat pos,LongLat dest,FeatureCollection restricted){ //need to fix
        double distance = 100;
        int angle = 0;


        ArrayList<LongLat> posCheck = new ArrayList<>();
        for (int y= 0; y<=35; y++){
            posCheck.add(pos);
        }
        //works without restriction zone
        for (int i= 0; i<=350; i= i+10){
            if (posCheck.get(i/10).nextPosition(i).distanceTo(dest)<distance && posCheck.get(i/10).isConfined()){
                boolean contained = false;
                for(int j = 0; j<restricted.features().size(); j++) {
                    if (posCheck.get(i / 10).nextPosition(i).isInside(pos, posCheck.get(i / 10).nextPosition(i), restricted.features().get(j)) == false) {
                        contained = false;
                    } else {
                        contained = true;
                        j = restricted.features().size();
                    }
                }
                if (contained == false) {
                    posCheck.set(i / 10, posCheck.get(i / 10).nextPosition(i));
                    distance = posCheck.get(i / 10).distanceTo(dest);
                    angle = i;
                    if (distance < 0.00015) {
                        angle = -999;
                        return angle;
                    }
                }

            }
        }


        return angle;
    }


    public boolean isInside(LongLat prev, LongLat curr,Feature restricted){
        Polygon findingcoords = (Polygon) restricted.geometry();

        List<Point> coordinates = findingcoords.coordinates().get(0);


        List<Point2D> point2DS = coordinates.stream().map(p -> new Point2D(p.latitude(), p.longitude())).collect(Collectors.toList());
        double[] yCoords = point2DS.stream().mapToDouble(p -> p.x()).toArray();


        double[] xCoords = point2DS.stream().mapToDouble(p -> p.y()).toArray();
        for (int v =0; v<xCoords.length; v++){
            if (curr.closeTo(new LongLat(xCoords[v], yCoords[v]))){
                return true;
            }
        }

        Path2D myPolygon = new Path2D.Double();
        myPolygon.moveTo(xCoords[0], yCoords[0]); //first point
        for(int i = 1; i < xCoords.length; i ++) {
            myPolygon.lineTo(xCoords[i], yCoords[i]);//draw lines
        }
        myPolygon.closePath();//draw last line
        if (myPolygon.intersects(prev.getLongitude(),prev.getLatitude(),curr.getLongitude(),curr.getLatitude())){
            return true;
        }
        return myPolygon.contains(curr.getLongitude(), curr.getLatitude());




    }








}
