package es.kleiren.madclimb.data_classes;

import java.io.Serializable;

/**
 * Created by Carlos on 11/05/2017.
 */

public class Zone extends Datum implements Serializable {

    private Boolean hasSectors = false;

    public Boolean getHasSectors() {
        return hasSectors;
    }

    public void setHasSectors(Boolean hasSectors) {
        this.hasSectors = hasSectors;
    }

}
