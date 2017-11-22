package es.kleiren.leviathan.data_classes;

import java.io.Serializable;

/**
 * Created by carlos on 22/11/17.
 */

public class Datum implements Serializable {

    String name;
    String id;
    String description;
    private String loc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



}
