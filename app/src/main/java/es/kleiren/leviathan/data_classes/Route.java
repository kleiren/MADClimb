package es.kleiren.leviathan.data_classes;

import java.io.Serializable;

/**
 * Created by carlos on 5/17/17.
 */

public class Route implements Serializable{

    private String zoneName;
    private String sectorName;
    private int resource;
    private String grade;
    String name;
    String description;

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
    public String getGrade() {
        return grade;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }
    public int getResource() {
        return resource;
    }
    public void setResource(int resource) {
        this.resource = resource;
    }

}
