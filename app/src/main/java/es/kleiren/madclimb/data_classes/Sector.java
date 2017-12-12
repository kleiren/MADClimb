package es.kleiren.madclimb.data_classes;

import java.io.Serializable;

/**
 * Created by Carlos on 11/05/2017.
 */

public class Sector extends Datum implements Serializable {

    private String zoneName;
    private int numOfRoutes;
    private String zone_id;
    private String croquis;

    public String getZone_id() {
        return zone_id;
    }
    public void setZone_id(String zone_id) {
        this.zone_id = zone_id;
    }
    public String getCroquis() {
        return croquis;
    }
    public void setCroquis(String croquis) {
        this.croquis = croquis;
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

}
