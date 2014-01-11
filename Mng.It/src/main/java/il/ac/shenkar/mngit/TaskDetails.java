package il.ac.shenkar.mngit;

/**
 * Created by Ori on 1/10/14.
 */
public class TaskDetails {
    private long id;
    private String description;
    private String location;
    private Boolean done;

    public TaskDetails(String description, String location) {
        this.description = description;
        this.location = location;
        this.done = false;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }
}
