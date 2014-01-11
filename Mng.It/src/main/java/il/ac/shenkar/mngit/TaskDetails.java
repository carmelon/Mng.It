package il.ac.shenkar.mngit;

/**
 * POJO of a Task for the Model layer.
 */
public class TaskDetails {
    private long id; //received by the DB when inserted
    private String description;
    private String location;
    private Boolean done; //task completed

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
