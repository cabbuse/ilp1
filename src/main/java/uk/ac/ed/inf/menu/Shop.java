package uk.ac.ed.inf.menu;

import java.util.ArrayList;

/**
 * class created to deserialize Gson data
 */
public class Shop {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<MenuItem> getMenu() {
        return menu;
    }

    public void setMenu(ArrayList<MenuItem> menu) {
        this.menu = menu;
    }

    private String name;

    public Shop(String name, String location, ArrayList<MenuItem> menu) {
        this.name = name;
        this.location = location;
        this.menu = menu;
    }

    private String location;
    private ArrayList<MenuItem> menu;


}
