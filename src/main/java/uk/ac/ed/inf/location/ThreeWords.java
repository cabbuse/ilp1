package uk.ac.ed.inf.location;

import com.google.gson.Gson;
import uk.ac.ed.inf.App;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class ThreeWords {
    public String nameNo;
    public String portNo;
    public String url;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ThreeWords(String nameNo, String portNo, String url) {
        this.nameNo = nameNo;
        this.portNo = portNo;
        this.url = url;
    }

    public ThreeWordsDescription GetGson(String url) {
        // HttpRequest assumes that it is a GET request by default.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        // We call the send method on the client which we created.
        try {
            HttpResponse<String> response = App.getClient().send(request, HttpResponse.BodyHandlers.ofString());
            ThreeWordsDescription items = new Gson().fromJson(response.body(),ThreeWordsDescription.class);
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

