package cargo.floter.driver.model;

import java.io.Serializable;

/**
 * Created by SONI on 5/11/2017.
 */

public class Trip implements Serializable {
    private final static long serialVersionUID = 81449664L;
    private Driver Driver;
    private User User;
    private String driver_id;
    private String goods_type;
    private String later_booking_time;
    private String promo_id;
    private String trip_actual_drop_lat;
    private String trip_actual_drop_lng;
    private String trip_actual_pick_lat;
    private String trip_actual_pick_lng;
    private String trip_created;
    private String trip_date;
    private String trip_distance;
    private String trip_driver_commision;
    private String trip_drop_time;
    private String trip_fare;
    private String trip_feedback;
    private String trip_from_loc;
    private String trip_id;
    private String trip_modified;
    private String trip_pay_amount;
    private String trip_pay_date;
    private String trip_pay_mode;
    private String trip_pay_status;
    private String trip_pickup_time;
    private String trip_promo_amt;
    private String trip_promo_code;
    private String trip_rating;
    private String trip_reason;
    private String trip_scheduled_drop_lat;
    private String trip_scheduled_drop_lng;
    private String trip_scheduled_pick_lat;
    private String trip_scheduled_pick_lng;
    private String trip_search_result_addr;
    private String trip_searched_addr;
    private String trip_status;
    private String trip_to_loc;
    private String trip_validity;
    private String trip_wait_time;
    private String user_id;

    public String getLater_booking_time() {
        return this.later_booking_time;
    }

    public void setLater_booking_time(String later_booking_time) {
        this.later_booking_time = later_booking_time;
    }

    public String getTrip_id() {
        return this.trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public String getTrip_date() {
        return this.trip_date;
    }

    public void setTrip_date(String trip_date) {
        this.trip_date = trip_date;
    }

    public String getDriver_id() {
        return this.driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getUser_id() {
        return this.user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTrip_from_loc() {
        return this.trip_from_loc;
    }

    public void setTrip_from_loc(String trip_from_loc) {
        this.trip_from_loc = trip_from_loc;
    }

    public String getTrip_to_loc() {
        return this.trip_to_loc;
    }

    public void setTrip_to_loc(String trip_to_loc) {
        this.trip_to_loc = trip_to_loc;
    }

    public String getTrip_distance() {
        return this.trip_distance;
    }

    public void setTrip_distance(String trip_distance) {
        this.trip_distance = trip_distance;
    }

    public String getTrip_fare() {
        return this.trip_fare;
    }

    public void setTrip_fare(String trip_fare) {
        this.trip_fare = trip_fare;
    }

    public String getTrip_wait_time() {
        return this.trip_wait_time;
    }

    public void setTrip_wait_time(String trip_wait_time) {
        this.trip_wait_time = trip_wait_time;
    }

    public String getTrip_pickup_time() {
        return this.trip_pickup_time;
    }

    public void setTrip_pickup_time(String trip_pickup_time) {
        this.trip_pickup_time = trip_pickup_time;
    }

    public String getTrip_drop_time() {
        return this.trip_drop_time;
    }

    public void setTrip_drop_time(String trip_drop_time) {
        this.trip_drop_time = trip_drop_time;
    }

    public String getTrip_reason() {
        return this.trip_reason;
    }

    public void setTrip_reason(String trip_reason) {
        this.trip_reason = trip_reason;
    }

    public String getTrip_validity() {
        return this.trip_validity;
    }

    public void setTrip_validity(String trip_validity) {
        this.trip_validity = trip_validity;
    }

    public String getTrip_feedback() {
        return this.trip_feedback;
    }

    public void setTrip_feedback(String trip_feedback) {
        this.trip_feedback = trip_feedback;
    }

    public String getTrip_status() {
        return this.trip_status;
    }

    public void setTrip_status(String trip_status) {
        this.trip_status = trip_status;
    }

    public String getTrip_rating() {
        return this.trip_rating;
    }

    public void setTrip_rating(String trip_rating) {
        this.trip_rating = trip_rating;
    }

    public String getTrip_scheduled_pick_lat() {
        return this.trip_scheduled_pick_lat;
    }

    public void setTrip_scheduled_pick_lat(String trip_scheduled_pick_lat) {
        this.trip_scheduled_pick_lat = trip_scheduled_pick_lat;
    }

    public String getTrip_scheduled_pick_lng() {
        return this.trip_scheduled_pick_lng;
    }

    public void setTrip_scheduled_pick_lng(String trip_scheduled_pick_lng) {
        this.trip_scheduled_pick_lng = trip_scheduled_pick_lng;
    }

    public String getTrip_actual_pick_lat() {
        return this.trip_actual_pick_lat;
    }

    public void setTrip_actual_pick_lat(String trip_actual_pick_lat) {
        this.trip_actual_pick_lat = trip_actual_pick_lat;
    }

    public String getTrip_actual_pick_lng() {
        return this.trip_actual_pick_lng;
    }

    public void setTrip_actual_pick_lng(String trip_actual_pick_lng) {
        this.trip_actual_pick_lng = trip_actual_pick_lng;
    }

    public String getTrip_scheduled_drop_lat() {
        return this.trip_scheduled_drop_lat;
    }

    public void setTrip_scheduled_drop_lat(String trip_scheduled_drop_lat) {
        this.trip_scheduled_drop_lat = trip_scheduled_drop_lat;
    }

    public String getTrip_scheduled_drop_lng() {
        return this.trip_scheduled_drop_lng;
    }

    public void setTrip_scheduled_drop_lng(String trip_scheduled_drop_lng) {
        this.trip_scheduled_drop_lng = trip_scheduled_drop_lng;
    }

    public String getTrip_actual_drop_lat() {
        return this.trip_actual_drop_lat;
    }

    public void setTrip_actual_drop_lat(String trip_actual_drop_lat) {
        this.trip_actual_drop_lat = trip_actual_drop_lat;
    }

    public String getTrip_actual_drop_lng() {
        return this.trip_actual_drop_lng;
    }

    public void setTrip_actual_drop_lng(String trip_actual_drop_lng) {
        this.trip_actual_drop_lng = trip_actual_drop_lng;
    }

    public String getTrip_searched_addr() {
        return this.trip_searched_addr;
    }

    public void setTrip_searched_addr(String trip_searched_addr) {
        this.trip_searched_addr = trip_searched_addr;
    }

    public String getTrip_search_result_addr() {
        return this.trip_search_result_addr;
    }

    public void setTrip_search_result_addr(String trip_search_result_addr) {
        this.trip_search_result_addr = trip_search_result_addr;
    }

    public String getTrip_pay_mode() {
        return this.trip_pay_mode;
    }

    public void setTrip_pay_mode(String trip_pay_mode) {
        this.trip_pay_mode = trip_pay_mode;
    }

    public String getTrip_pay_amount() {
        return this.trip_pay_amount;
    }

    public void setTrip_pay_amount(String trip_pay_amount) {
        this.trip_pay_amount = trip_pay_amount;
    }

    public String getTrip_pay_date() {
        return this.trip_pay_date;
    }

    public void setTrip_pay_date(String trip_pay_date) {
        this.trip_pay_date = trip_pay_date;
    }

    public String getTrip_pay_status() {
        return this.trip_pay_status;
    }

    public void setTrip_pay_status(String trip_pay_status) {
        this.trip_pay_status = trip_pay_status;
    }

    public String getTrip_driver_commision() {
        return this.trip_driver_commision;
    }

    public void setTrip_driver_commision(String trip_driver_commision) {
        this.trip_driver_commision = trip_driver_commision;
    }

    public String getGoods_type() {
        return this.goods_type;
    }

    public void setGoods_type(String goods_type) {
        this.goods_type = goods_type;
    }

    public String getTrip_created() {
        return this.trip_created;
    }

    public void setTrip_created(String trip_created) {
        this.trip_created = trip_created;
    }

    public String getTrip_modified() {
        return this.trip_modified;
    }

    public void setTrip_modified(String trip_modified) {
        this.trip_modified = trip_modified;
    }

    public String getPromo_id() {
        return this.promo_id;
    }

    public void setPromo_id(String promo_id) {
        this.promo_id = promo_id;
    }

    public String getTrip_promo_code() {
        return this.trip_promo_code;
    }

    public void setTrip_promo_code(String trip_promo_code) {
        this.trip_promo_code = trip_promo_code;
    }

    public String getTrip_promo_amt() {
        return this.trip_promo_amt;
    }

    public void setTrip_promo_amt(String trip_promo_amt) {
        this.trip_promo_amt = trip_promo_amt;
    }

    public User getUser() {
        return this.User;
    }

    public void setUser(User user) {
        this.User = user;
    }

    public Driver getDriver() {
        return this.Driver;
    }

    public void setDriver(Driver driver) {
        this.Driver = driver;
    }
}
