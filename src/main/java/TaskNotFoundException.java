/**
 * This class handles errors associated with removal, assignation and status updates
 */
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String key) {
        super ("Could not find task: "+key);
    }
}

