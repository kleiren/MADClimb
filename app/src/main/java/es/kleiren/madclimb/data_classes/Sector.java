package es.kleiren.madclimb.data_classes;

import java.io.Serializable;

public class Sector extends Datum implements Serializable {

    private String zoneName;
    private String zone_id;
    private String date = "";
    private String has_sub_sectors;
    private String parentSector;
    private String restriction_start;
    private String restriction_end;
    public Integer[] routesFiltered;

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

    public String getRestriction_start() {
        return restriction_start;
    }

    public void setRestriction_start(String restriction_start) {
        this.restriction_start = restriction_start;
    }

    public String getRestriction_end() {
        return restriction_end;
    }

    public void setRestriction_end(String restriction_end) {
        this.restriction_end = restriction_end;
    }

    public Integer[] getRoutesFiltered() {
        return routesFiltered;
    }

    public void setRoutesFiltered(Integer[] routesFiltered) {
        this.routesFiltered = routesFiltered;
    }
}
