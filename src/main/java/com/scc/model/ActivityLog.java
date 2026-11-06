
package com.scc.model;

import java.sql.Timestamp;

public class ActivityLog {

    private int log_id;
    private int user_id;
    private String action;
    private String description;
    private String log_timestamp;

    public ActivityLog(int log_id, int user_id, String action,
                       String description, Timestamp log_timestamp) {
        this.log_id = log_id;
        this.user_id = user_id;
        this.action = action;
        this.description = description;
        this.log_timestamp = log_timestamp != null
                ? log_timestamp.toString() : "N/A";
    }

    public int getLog_id() {
        return log_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public String getLog_timestamp() {
        return log_timestamp;
    }
}
