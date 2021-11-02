package uk.ac.ed.inf.location;

import com.mapbox.geojson.FeatureCollection;
import uk.ac.ed.inf.App;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class flyzones {
    public String nameNo;
    public String portNo;
    public String url;

    public flyzones(String nameNo, String portNo) {
        this.nameNo = nameNo;
        this.portNo = portNo;
    }


    public String getNameNo() {
        return nameNo;
    }

    public void setNameNo(String nameNo) {
        this.nameNo = nameNo;
    }

    public String getPortNo() {
        return portNo;
    }

    public void setPortNo(String portNo) {
        this.portNo = portNo;
    }



    public FeatureCollection getGson(String url) {
        // HttpRequest assumes that it is a GET request by default.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        // We call the send method on the client which we created.
        try {
            HttpResponse<String> response = App.getClient().send(request, HttpResponse.BodyHandlers.ofString());
            FeatureCollection items = FeatureCollection.fromJson(String.valueOf(response.body()));
            return items;
            //creates shop class from deserialized information from Gson for processing

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


}
