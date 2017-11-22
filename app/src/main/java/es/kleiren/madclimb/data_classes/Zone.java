package es.kleiren.madclimb.data_classes;

import java.io.Serializable;

/**
 * Created by Carlos on 11/05/2017.
 */

public class Zone extends Datum implements Serializable {

    private String img;
    public String getImg() {
        return img;
    }
    public void setImg(String img) {
        this.img = img;
    }


}
