package il.ac.shenkar.mngit;

/**
 * Created by Ori on 1/10/14.
 */
public class TaskDetails {
    private long id;
    private String description;

    public TaskDetails(String description) {
        this.description = description;
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
}
