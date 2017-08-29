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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    private String name;
    private String image;
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
