package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import uk.ac.ed.inf.location.*;
import uk.ac.ed.inf.menu.Shop;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;




/**
 * * Hello world!
 */
public class App {

    public static void main(String[] args) {
        App system = new App("2022-06-06","localhost","9898","jdbc:derby://localhost:9876/database/derbyDB");
        Menus menuForDate = new Menus(App.getName(),App.getPort());
        String MenusUrl = "http://"+menuForDate.getName()+":"+menuForDate.getPort()+"/menus/menus.json";
        flyzones flightZone =  new flyzones(App.getName(),App.getPort());
        FeatureCollection restricted = flightZone.getGson("http://" + flightZone.getNameNo() + ":" + flightZone.getPortNo() + "/buildings/no-fly-zones.geojson");
        FeatureCollection landmarks = flightZone.getGson("http://" + flightZone.getNameNo() + ":" + flightZone.getPortNo() + "/buildings/landmarks.geojson");
        Database database = new Database(getJdbc()); //creates new database
        database.createTableDeliveries();
        database.createTableFlightpath();
        ArrayList<ArrayList<String>> userOrder = database.findOrders(getDate()); //gets all user orders on a certain date
        String[][] totalCostPence = system.totalPenceCost(userOrder,database,menuForDate); //calculates array of all the order costs with order no for reference
        double StartLong = -3.186874; //initialise flight with appleton tower location
        double Startlat = 55.944494;
        int noOfmoves = 15000;
        LongLat Home = new LongLat(StartLong,Startlat);
        LongLat currLocate = new LongLat(StartLong,Startlat);
        ArrayList<Point> flightpath = new ArrayList<>();
        flightpath.add(Point.fromLngLat(Home.getLongitude(),Home.getLatitude())); //start flightpath
        ArrayList<LongLat> landmarkLL= Home.landmarksToLNLT(landmarks.features());
        landmarkLL.add(Home); //add all landmarks and appleton to landmarks array for


        
//        load all current deliveries from set date and get their location
        ArrayList<String> allDel = system.alldeliveriesMatched(userOrder, database, system, menuForDate, MenusUrl);
        ArrayList<ThreeWordsDescription> allDeliveriesLocation = system.allDeliveryLocations(allDel);
        ArrayList<LongLat> deliveries = new ArrayList<>();
        LongLat closestLL = new LongLat(landmarkLL.get(0).getLongitude(),landmarkLL.get(0).getLatitude());
        for(int t = 0; t<allDeliveriesLocation.size(); t++) {
            deliveries.add(new LongLat(allDeliveriesLocation.get(t).getCoordinates().getLng(), allDeliveriesLocation.get(t).getCoordinates().getLat()));
            double closest = closestLL.distanceTo(deliveries.get(t));
            for (int x= 0; x< landmarkLL.size(); x++){
                if (deliveries.get(t).distanceTo(landmarkLL.get(x))<closest){
                    closest = deliveries.get(t).distanceTo(landmarkLL.get(x));
                    closestLL = landmarkLL.get(x);

                }
            }//add in closest landmark after ever pick-up/drop for collision avoidance
            deliveries.add(new LongLat(closestLL.getLongitude(),closestLL.getLatitude()));
        }

        //adding landmarks + appleton to all possible delivery locations
        ArrayList<String> landM = new ArrayList<>();
        for (int r = 0; r< landmarkLL.size()-1; r++){
            landM.add(landmarks.features().get(r).properties().get("location").toString().replace("\"", ""));

        }
        landM.add("nests.takes.print");
        //add landmarks + appleton to possible stop locations
        ArrayList<ThreeWordsDescription> LandM3 = system.allDeliveryLocations(landM);
        for (int s = 0; s<LandM3.size(); s++){
            allDeliveriesLocation.add(LandM3.get(s));
        }



        //initialise variable for getting correct index of delivery ,is always equal to 1 as arraylists act like stack when an index is removed
        int deliveryIndex = 0;
        int deliveries_made = 0; //tracks no of deliveries made for adding to deliveries database
        while (noOfmoves>((landmarkLL.get(0).distanceTo(Home)+landmarkLL.get(1).distanceTo(Home))/0.00015)+1 && deliveries.size()>0 ) { //ensure drone can always reach appleton
            LongLat from = currLocate;
            int angleTofly = currLocate.chooseAngle(currLocate,deliveries.get(deliveryIndex),restricted);
            currLocate = currLocate.nextPosition(angleTofly);
            String matchedOrderNo = userOrder.get(deliveries_made).get(0);
            flightpath.add(Point.fromLngLat(currLocate.getLongitude(),currLocate.getLatitude())); //update flightpath
            database.insertToTableflightPath(matchedOrderNo,from.getLongitude(),from.getLatitude(),angleTofly,currLocate.getLongitude(),currLocate.getLatitude()); //update database

            if (angleTofly ==-999) {
                String threeWord = currLocate.matchThreeWord(deliveries.get(deliveryIndex),allDeliveriesLocation);
                if (system.AtdeliveryLoc(userOrder,threeWord) == true) { //only add to delivery table if at delivery location
                    database.insertToTableDelivery(matchedOrderNo, threeWord, matchPence(matchedOrderNo, totalCostPence));
                    deliveries_made += 1;
                }
                deliveries.remove(deliveryIndex);
                flightpath.add(Point.fromLngLat(currLocate.getLongitude(),currLocate.getLatitude())); //add hover move to flightpath and update database
                database.insertToTableflightPath(matchedOrderNo,currLocate.getLongitude(),currLocate.getLatitude(),-999,currLocate.getLongitude(),currLocate.getLatitude());

                noOfmoves-= 1;
            }
            noOfmoves -=1;
        }
        //all deliveries are completed or drone is low on battery so must return back
        deliveries = new ArrayList<>();
        deliveries.add(Home); //set drone to return home
        while (noOfmoves>0&&deliveries.size()>0){
            LongLat from = currLocate;
            int angleTofly = currLocate.chooseAngle(currLocate,deliveries.get(deliveryIndex),restricted);
            currLocate = currLocate.nextPosition(angleTofly);
            String matchedOrderNo = userOrder.get(deliveries_made).get(0);
            flightpath.add(Point.fromLngLat(currLocate.getLongitude(),currLocate.getLatitude()));//add move to flightpath
            database.insertToTableflightPath(matchedOrderNo,from.getLongitude(),from.getLatitude(),angleTofly,currLocate.getLongitude(),currLocate.getLatitude());//update database
            if (angleTofly ==-999) {
                deliveries.remove(deliveryIndex);
                flightpath.add(Point.fromLngLat(currLocate.getLongitude(),currLocate.getLatitude()));//update flightpath and database
                database.insertToTableflightPath(matchedOrderNo,currLocate.getLongitude(),currLocate.getLatitude(),-999,currLocate.getLongitude(),currLocate.getLatitude());

                noOfmoves-= 1;
            }
            noOfmoves -=1;

        }

        //write flightpath to json
        system.writeToJSon(flightpath,getDate());
    }


    public boolean AtdeliveryLoc (ArrayList<ArrayList<String>> userOrder, String threeword){
        for (int i =0; i<userOrder.size(); i++){
            if (userOrder.get(i).get(1).equals(threeword)){
                return true;
            }
        }
        return false;
    }









    public ArrayList<ThreeWordsDescription>allDeliveryLocations(ArrayList<String> allDel){
        ArrayList<ThreeWordsDescription> allDeliveriesLocation = new ArrayList<>();
        for(int i = 0; i<allDel.size();i++) {
            String[] deliveryLocation = allDel.get(i).split("\\.");
            ThreeWords delivery = new ThreeWords(App.getName(), App.getPort(), "http://" + App.getName() + ":" + App.getPort() + "/words/" + deliveryLocation[0] + "/" + deliveryLocation[1] + "/" + deliveryLocation[2] + "/details.json");
            ThreeWordsDescription deliveryTo = delivery.GetGson(delivery.getUrl());
            allDeliveriesLocation.add(deliveryTo);

        }
        return allDeliveriesLocation;

    }

    public ArrayList<String> alldeliveriesMatched(ArrayList<ArrayList<String>> userOrder, Database database, App system, Menus allMenus, String menuUrl) {
        ArrayList<String> userOrder3W = new ArrayList<>();
        ArrayList<ArrayList<String>> cde = new ArrayList<>();

            for (int x = 0; x < userOrder.size(); x++) {
                cde.add(database.findOrdersdetails(userOrder.get(x).get(0)));
                for (int y = 1; y < cde.get(x).size(); y++) {
                    if (cde.get(x).get(y).matches(".*\\d.*")) {
                        cde.get(x).remove(y);
                    }
                }
                userOrder3W.add(userOrder.get(x).get(1));
            }






        ArrayList<String> all_deliveries_ordered = matchOrderToMenu(cde, allMenus, menuUrl, userOrder);
        return all_deliveries_ordered;





    }

    public ArrayList<String> matchOrderToMenu(ArrayList<ArrayList<String>> userMenuOrder, Menus allMenus, String menuUrl, ArrayList<ArrayList<String>> usersOrderDrop){
        Shop[] menus = Menus.GetGson(menuUrl);
        ArrayList<String> orderLocation = new ArrayList<>();
        for (int i = 0 ; i< menus.length;i++){
            boolean addOnce = true;
            for(int l = 0; l<menus.length; l++){
                for (int j =0; j<menus[l].getMenu().size(); j++) {
                    for (int k = 0; k < userMenuOrder.get(i).size(); k++) {
                        if (userMenuOrder.get(i).get(k).equals(menus[l].getMenu().get(j).getItem())) {
                            orderLocation.add(menus[i].getLocation());
                            if (addOnce) {
                                orderLocation.add(String.valueOf(i));
                                addOnce = false;
                            }
                    }
                }
            }

            }
        }
        List<String> dropLocation = orderLocation.stream().distinct().collect(Collectors.toList());
        int added = 0;
        ArrayList<String> pickUpToDrop = new ArrayList<>();
        for (int x =0; x<dropLocation.size(); x++){
            if(String.valueOf(dropLocation.get(x)).equals(String.valueOf(added))){
                pickUpToDrop.add(usersOrderDrop.get(added).get(1));
                added +=1;

            }else{
                pickUpToDrop.add(dropLocation.get(x));
            }
        }
        return pickUpToDrop;

    }

    public String[][] totalPenceCost(ArrayList<ArrayList<String>> userOrder,Database database,Menus menuForSetDate){
        String[][] totalCostPence = new String[userOrder.size()][2];
        for (int i = 0;i<userOrder.size();i++) {
            //finds what user ordered based on order no
            ArrayList<String> orderdetails = database.findOrdersdetails(userOrder.get(i).get(0));

        for(int p =0; p<orderdetails.size()-1; p ++) {
            orderdetails.remove(p);
        }
            String orderNumforOrder = userOrder.get(i).get(0);
            Object[] details = orderdetails.toArray();
            String[] detail = Arrays.copyOf(details, details.length, String[].class);
            totalCostPence[i][0] = String.valueOf(menuForSetDate.getDeliveryCost(detail));
            totalCostPence[i][1] = orderNumforOrder;


        }
        return  totalCostPence ;
    }




    public void writeToJSon(ArrayList<Point> flightpath,String date ){
        LineString flight = LineString.fromLngLats(flightpath);
        Feature flightParseA = Feature.fromGeometry(flight);
        FeatureCollection flightParseB = FeatureCollection.fromFeature(flightParseA);
        String flightStr = flightParseB.toJson();
        File file = new File("drone-"+date+".geojson");
        try {
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(flightStr);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int matchPence(String orderNo, String[][] penceRef){
        int cost = 0;
        for (int i = 0; i <penceRef.length; i++){
            if(penceRef[i][1].equals(orderNo)){
                cost = Integer.parseInt(penceRef[i][0]);

            }
        }
        return cost;
    }

    public static String getDate() {
        return date;
    }

    public void setDate(String date) {
        App.date = date;
    }

    public static String getName() {
        return name;
    }

    public void setName(String name) {
        App.name = name;
    }

    public static String getPort() {
        return port;
    }

    public void setPort(String port) {
        App.port = port;
    }

    public static String getJdbc() {
        return jdbc;
    }

    public void setJdbc(String jdbc) {
        App.jdbc = jdbc;
    }

    public App(String date, String name, String port, String jdbc) {
        App.date = date;
        App.name = name;
        App.port = port;
        App.jdbc = jdbc;
    }

    public static HttpClient getClient() {
        return client;
    }


    private static final HttpClient client = HttpClient.newHttpClient();

    public static String date;



    public static String name;
    public static String port;
    public static String jdbc;









}


