package cargo.floter.driver.model;

import java.io.Serializable;

/**
 * Created by SONI on 4/15/2017.
 */

public class Driver implements Serializable {
    private final static long serialVersionUID = 814465194164L;

    private String api_key;
    private String car_name;
    private String d_address;
    private String d_city;
    private String d_country;
    private String d_degree;
    private String d_email;
    private String d_fname;
    private String d_lat;
    private String d_lname;
    private String d_lng;
    private String d_name;
    private String d_password;
    private String d_phone;
    private String d_state;
    private String d_zip;
    private String driver_id;
    private String image_id;
    private String truck_reg_no;

    public String getTruck_reg_no() {
        return truck_reg_no;
    }

    public void setTruck_reg_no(String truck_reg_no) {
        this.truck_reg_no = truck_reg_no;
    }

    public String getCar_name() {
        return car_name;
    }

    public void setCar_name(String car_name) {
        this.car_name = car_name;
    }

    private String d_license_id ;
    private String d_rc ;
    private String car_id ;
    private String d_device_type ;
    private String d_device_token ;
    private String d_rating ;
    private String d_rating_count ;
    private String d_is_available ;
    private String d_is_verified ;
    private String d_created ;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getD_name() {
        return d_name;
    }

    public void setD_name(String d_name) {
        this.d_name = d_name;
    }

    public String getD_fname() {
        return d_fname;
    }

    public void setD_fname(String d_fname) {
        this.d_fname = d_fname;
    }

    public String getD_lname() {
        return d_lname;
    }

    public void setD_lname(String d_lname) {
        this.d_lname = d_lname;
    }

    public String getD_email() {
        return d_email;
    }

    public void setD_email(String d_email) {
        this.d_email = d_email;
    }

    public String getD_password() {
        return d_password;
    }

    public void setD_password(String d_password) {
        this.d_password = d_password;
    }

    public String getD_phone() {
        return d_phone;
    }

    public void setD_phone(String d_phone) {
        this.d_phone = d_phone;
    }

    public String getD_address() {
        return d_address;
    }

    public void setD_address(String d_address) {
        this.d_address = d_address;
    }

    public String getD_city() {
        return d_city;
    }

    public void setD_city(String d_city) {
        this.d_city = d_city;
    }

    public String getD_state() {
        return d_state;
    }

    public void setD_state(String d_state) {
        this.d_state = d_state;
    }

    public String getD_country() {
        return d_country;
    }

    public void setD_country(String d_country) {
        this.d_country = d_country;
    }

    public String getD_zip() {
        return d_zip;
    }

    public void setD_zip(String d_zip) {
        this.d_zip = d_zip;
    }

    public String getD_lat() {
        return d_lat;
    }

    public void setD_lat(String d_lat) {
        this.d_lat = d_lat;
    }

    public String getD_lng() {
        return d_lng;
    }

    public void setD_lng(String d_lng) {
        this.d_lng = d_lng;
    }

    public String getD_degree() {
        return d_degree;
    }

    public void setD_degree(String d_degree) {
        this.d_degree = d_degree;
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public String getD_license_id() {
        return d_license_id;
    }

    public void setD_license_id(String d_license_id) {
        this.d_license_id = d_license_id;
    }

    public String getD_rc() {
        return d_rc;
    }

    public void setD_rc(String d_rc) {
        this.d_rc = d_rc;
    }

    public String getCar_id() {
        return car_id;
    }

    public void setCar_id(String car_id) {
        this.car_id = car_id;
    }

    public String getD_device_type() {
        return d_device_type;
    }

    public void setD_device_type(String d_device_type) {
        this.d_device_type = d_device_type;
    }

    public String getD_device_token() {
        return d_device_token;
    }

    public void setD_device_token(String d_device_token) {
        this.d_device_token = d_device_token;
    }

    public String getD_rating() {
        return d_rating;
    }

    public void setD_rating(String d_rating) {
        this.d_rating = d_rating;
    }

    public String getD_rating_count() {
        return d_rating_count;
    }

    public void setD_rating_count(String d_rating_count) {
        this.d_rating_count = d_rating_count;
    }

    public String getD_is_available() {
        return d_is_available;
    }

    public void setD_is_available(String d_is_available) {
        this.d_is_available = d_is_available;
    }

    public String getD_is_verified() {
        return d_is_verified;
    }

    public void setD_is_verified(String d_is_verified) {
        this.d_is_verified = d_is_verified;
    }

    public String getD_created() {
        return d_created;
    }

    public void setD_created(String d_created) {
        this.d_created = d_created;
    }
}
