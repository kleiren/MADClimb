package es.kleiren.leviathan;

/**
 * Created by Carlos on 11/05/2017.
 */

public class Zone {

    public Zone (){}

    public Zone(String name, int resource) {
        this.name = name;
        this.resource = resource;
    }

    private String name;
    private int resource;
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
