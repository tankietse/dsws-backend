
package com.webgis.dsws.domain.model;

/**
 * Lớp đại diện cho phản hồi API chung.
 */
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;

    // Constructor
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters và Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}