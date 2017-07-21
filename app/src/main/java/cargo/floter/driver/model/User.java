package cargo.floter.driver.model;

import java.io.Serializable;

/**
 * Created by SONI on 4/14/2017.
 */

public class User implements Serializable {
    private final static long serialVersionUID = 814469655194164L;

    private String user_id;
    private String api_key;
    private String group_id;
    private String u_fname;
    private String u_lname;
    private String u_email;
    private String u_mobile;
    private String u_lat;
    private String u_lng;
    private String u_degree;
    private String active;
    private String u_created;
    private String u_device_token;
    private String u_device_type;

    public String getU_device_token() {
        return u_device_token;
    }

    public void setU_device_token(String u_device_token) {
        this.u_device_token = u_device_token;
    }

    public String getU_device_type() {
        return u_device_type;
    }

    public void setU_device_type(String u_device_type) {
        this.u_device_type = u_device_type;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getU_fname() {
        return u_fname;
    }

    public void setU_fname(String u_fname) {
        this.u_fname = u_fname;
    }

    public String getU_lname() {
        return u_lname;
    }

    public void setU_lname(String u_lname) {
        this.u_lname = u_lname;
    }

    public String getU_email() {
        return u_email;
    }

    public void setU_email(String u_email) {
        this.u_email = u_email;
    }

    public String getU_mobile() {
        return u_mobile;
    }

    public void setU_mobile(String u_mobile) {
        this.u_mobile = u_mobile;
    }

    public String getU_lat() {
        return u_lat;
    }

    public void setU_lat(String u_lat) {
        this.u_lat = u_lat;
    }

    public String getU_lng() {
        return u_lng;
    }

    public void setU_lng(String u_lng) {
        this.u_lng = u_lng;
    }

    public String getU_degree() {
        return u_degree;
    }

    public void setU_degree(String u_degree) {
        this.u_degree = u_degree;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getU_created() {
        return u_created;
    }

    public void setU_created(String u_created) {
        this.u_created = u_created;
    }
}
