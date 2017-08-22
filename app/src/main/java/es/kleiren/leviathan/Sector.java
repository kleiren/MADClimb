package es.kleiren.leviathan;

/**
 * Created by Carlos on 11/05/2017.
 */

public class Sector {

    public Sector(){}

    public Sector(String name, String zoneName, int numOfRoutes, int resource) {
        this.name = name;
        this.zoneName = zoneName;
        this.numOfRoutes = numOfRoutes;
        this.resource = resource;
    }


    private String name;
    private String zoneName;
    private int numOfRoutes;
    private int resource;

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public int getNumOfRoutes() {
        return numOfRoutes;
    }

    public void setNumOfRoutes(int numOfRoutes) {
        this.numOfRoutes = numOfRoutes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }
}
