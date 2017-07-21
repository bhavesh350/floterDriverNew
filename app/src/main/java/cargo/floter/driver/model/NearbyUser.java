package cargo.floter.driver.model;

import java.util.List;

/**
 * Created by SONI on 4/26/2017.
 */

public class NearbyUser {

    private String status;
    private int code;
    private List<Response> response;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<Response> getResponse() {
        return response;
    }

    public void setResponse(List<Response> response) {
        this.response = response;
    }

    public class Response{

        private String user_id;
        private String api_key;
        private String group_id;
        private String u_fname;
        private String u_lname;
        private String u_email;
        private String u_mobile;
        private String u_address;
        private String u_city;
        private String u_state;
        private String u_country;
        private String u_zip;
        private String u_lat;
        private String u_lng;
        private String u_device_type;
        private String u_device_token;
        private String u_is_available;
        private String u_profile_image_path;
        private String distance;

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

        public String getU_address() {
            return u_address;
        }

        public void setU_address(String u_address) {
            this.u_address = u_address;
        }

        public String getU_city() {
            return u_city;
        }

        public void setU_city(String u_city) {
            this.u_city = u_city;
        }

        public String getU_state() {
            return u_state;
        }

        public void setU_state(String u_state) {
            this.u_state = u_state;
        }

        public String getU_country() {
            return u_country;
        }

        public void setU_country(String u_country) {
            this.u_country = u_country;
        }

        public String getU_zip() {
            return u_zip;
        }

        public void setU_zip(String u_zip) {
            this.u_zip = u_zip;
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

        public String getU_device_type() {
            return u_device_type;
        }

        public void setU_device_type(String u_device_type) {
            this.u_device_type = u_device_type;
        }

        public String getU_device_token() {
            return u_device_token;
        }

        public void setU_device_token(String u_device_token) {
            this.u_device_token = u_device_token;
        }

        public String getU_is_available() {
            return u_is_available;
        }

        public void setU_is_available(String u_is_available) {
            this.u_is_available = u_is_available;
        }

        public String getU_profile_image_path() {
            return u_profile_image_path;
        }

        public void setU_profile_image_path(String u_profile_image_path) {
            this.u_profile_image_path = u_profile_image_path;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }
    }
}
