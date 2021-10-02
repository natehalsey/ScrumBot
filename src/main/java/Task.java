public class Task {
    private String status;
    private String taskID;
    private String taskMessage;
    private String assignedTo;
    public Task(String taskMessage, String status, String assignedTo){
        this.status = status;
        this.taskMessage = taskMessage;
        this.assignedTo = assignedTo;
    }
    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public void setTaskID(String taskID){
        this.taskID = taskID;
    }
    public String getTaskID(){
        return taskID;
    }
    public String getTaskMessage(){
        return taskMessage;
    }
    public void setTaskMessage(String taskMessage){
        this.taskMessage = taskMessage;
    }
    public String getAssignedTo(){
        return assignedTo;
    }
    public void setAssignedTo(String assignedTo){
        this.assignedTo = assignedTo;
    }

}
