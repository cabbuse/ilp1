package uk.ac.ed.inf.menu;


/**
 * sub class of shop to represent all the items it provides
 * and their associated costs
 */
public class MenuItem {
    public MenuItem(String item, Integer pence) {
        this.item = item;
        this.pence = pence;
    }

    private String item;

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Integer getPence() {
        return pence;
    }

    public void setPence(Integer pence) {
        this.pence = pence;
    }

    private Integer pence;
}
