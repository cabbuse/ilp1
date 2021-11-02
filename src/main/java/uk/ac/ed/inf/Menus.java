package uk.ac.ed.inf;

import com.google.gson.Gson;
import uk.ac.ed.inf.menu.Shop;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;



/**
 * menus class used to process all restaurant menus data
 * created from accessing the localhost menu Gson
 */
public class Menus {

    private static  final int  dCharge = 50;
    public String Name;
    public String Port;

    // Just have one HttpClient, shared between all HttpRequests



    // getter method
    public String getName() {
        return Name;
    }

    //getter method
    public String getPort() {
        return Port;
    }

    // constructor for menu class
    public Menus(String name, String port) {
        this.Name = name;
        this.Port = port;
    }

    /**
     * method to return the delivery cost from a set of desired items of arbitrary length
     * cycles through all items on every restaurants list and adds item cost plus delivery charge if
     * they match
     *
     * @param deliveries ellipses string of item cost that need to be calculated
     * @return Integer cost off all the delivery item costs plus delivery charge
     */
    public int getDeliveryCost(String... deliveries) {
        String url = "http://" + getName() + ":" + getPort() + "/menus/menus.json";
        Shop[] Menus = GetGson(url);
        int cost = 0;
        int i = 0;
        while (i < deliveries.length) {
            boolean found = false;
            for (int x = 0; x < Menus.length; x++) { //iterate through all shops items to find if they match deliveries
                for (int y = 0; y < Menus[x].getMenu().size(); y++) {
                    if (deliveries[i].equals(Menus[x].getMenu().get(y).getItem())) {
                        cost = cost + Menus[x].getMenu().get(y).getPence();
                        found = true;

                    }
                }

            }
            if (found == true) {
                i += 1;
            } else {
                return 0; //catch condition if deliveries supplied do not match any item in any shop
            }
        }
        int total_cost = cost + dCharge;
        return total_cost;
    }

    /**
     * private method to create classes to deserialise the Gson file into a usable class formatting
     *
     * @param url local host url that accesses all menu items given by restaurant
     * @return Shop class to represent the list of restaurants and their corresponding
     * name, location and menu items
     */
    public static Shop[] GetGson(String url) {
        // HttpRequest assumes that it is a GET request by default.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        // We call the send method on the client which we created.
        try {
            HttpResponse<String> response = App.getClient().send(request, HttpResponse.BodyHandlers.ofString());
            Shop[] items = new Gson().fromJson(response.body(), Shop[].class);
            return items;
            //creates shop class from deserialized information from Gson for processing

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return null;
    }


}