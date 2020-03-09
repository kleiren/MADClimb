package es.kleiren.madclimb.data_classes;

import java.io.Serializable;

public class Datum implements Serializable {

    private String name;
    private String id;
    private String description;
    private String loc;
    private String img;
    private Integer position = 999;
    private String parking;
    private String parkings;
    public int numberOfSectors;
    public int numberOfRoutes;
    private String eightADotNu = "";

    public String getEightADotNu() {
        return eightADotNu;
    }

    public void setEightADotNu(String eightADotNu) {
        this.eightADotNu = eightADotNu;
    }


    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

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

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
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

    public String getParking() {
        return parking;
    }

    public String[] getParkings() {
        if (parkings != null) {
            if (parkings.contains(";")) {
                return parkings.split(";");
            } else {
                return new String[]{parkings};
            }
        }
        return null;
    }

    public void setParking(String parking) {
        this.parking = parking;
    }

    public void setParkings(String parkings) {
        this.parkings = parkings;
    }
}
