package es.kleiren.leviathan;

/**
 * Created by carlos on 5/17/17.
 */

public class Route {


    public Route(String zoneName,String sectorName,String name, int resource, String grade) {
        this.name = name;
        this.resource = resource;
        this.grade = grade;
        this.zoneName = zoneName;
        this.sectorName = sectorName;

    }

    public  Route(){}
    private String name;
    private String zoneName;
    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    private String sectorName;
    private int resource;
    private String grade;

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
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
