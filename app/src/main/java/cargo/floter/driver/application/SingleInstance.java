package cargo.floter.driver.application;

import cargo.floter.driver.model.Trip;

import org.json.JSONObject;

/**
 * Created by SONI on 5/19/2017.
 */

public class SingleInstance {
    private static final SingleInstance ourInstance = new SingleInstance();

    public static SingleInstance getInstance() {
        return ourInstance;
    }

    private SingleInstance() {
    }

    private Trip historyTrip;
    private JSONObject jsonTripPayload;

    public JSONObject getJsonTripPayload() {
        return jsonTripPayload;
    }

    public void setJsonTripPayload(JSONObject jsonTripPayload) {
        this.jsonTripPayload = jsonTripPayload;
    }

    public Trip getHistoryTrip() {
        return historyTrip;
    }

    public void setHistoryTrip(Trip historyTrip) {
        this.historyTrip = historyTrip;
    }
}
