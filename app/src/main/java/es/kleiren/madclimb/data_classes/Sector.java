package es.kleiren.madclimb.data_classes;

import java.io.Serializable;

public class Sector extends Datum implements Serializable {

    private String zoneName;
    private int numOfRoutes;
    private String zone_id;
    private String date;
    private String has_sub_sectors;
    private String parentSector;

    public String getDate() {
        return date;
    }

    public Boolean hasSubSectors() {
        if (has_sub_sectors != null) {
            return has_sub_sectors.equals("true");
        } else return false;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getZone_id() {
        return zone_id;
    }

    public void setZone_id(String zone_id) {
        this.zone_id = zone_id;
    }

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

    public void setHas_sub_sectors(String has_sub_sectors) {
        this.has_sub_sectors = has_sub_sectors;
    }

    public String getHas_sub_sectors() {
        return has_sub_sectors;
    }

    public String getParentSector() {
        return parentSector;
    }

    public void setParentSector(String parentSector) {
        this.parentSector = parentSector;
    }
}
