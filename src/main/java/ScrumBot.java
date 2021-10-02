import org.javacord.api.*;
import org.javacord.api.entity.message.Message;

public class ScrumBot {
    // reference to message in scrum-board to refer back to
    static TableGenerator table;
    public static void main(String[] args) {
        //read contents of table.txt and turn it into a string
        table = new TableGenerator();
        String token = (System.getenv("BOT_TOKEN"));

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        api.addMessageCreateListener(event -> {
            Message msg = event.getMessage();
            if (!msg.getContent().startsWith("`")) {
                if (msg.getContent().contains("!task") && !msg.getContent().equals("!tasklist")) {
                    String[] message = msg.getContent().split("!task");
                    table.addEntry(message[1]);
                    event.getChannel().sendMessage("Added new task: " + message[1]);
                }
                if (msg.getContent().equals("!tasklist")) {
                    event.getChannel().sendMessage(table.getTable());
                }
                if (msg.getContent().contains("!remove")) {
                    String[] message = msg.getContent().split(" ");
                    try {
                        table.removeEntry(message[1]);
                        event.getChannel().sendMessage("Removed task: " + message[1]);
                    } catch (TaskNotFoundException e) {
                        event.getChannel().sendMessage(e.getMessage());
                    }
                }
                if (msg.getContent().contains("!status")) {
                    String[] message = msg.getContent().split(" ");
                    try {
                        if (message.length == 3) {
                            table.updateEntryStatus(message[1], message[2]);
                            event.getChannel().sendMessage("Status of: " + message[1] + "changed to :" + message[2]);
                        }
                    } catch (TaskNotFoundException e) {
                        event.getChannel().sendMessage(e.getMessage());
                    }
                }
                if (msg.getContent().contains("!assign")) {
                    String[] message = msg.getContent().split(" ");
                    try {
                        if (message.length == 3) {
                            table.updateEntryAssigned(message[1], message[2]);
                            event.getChannel().sendMessage("Assigned: " + message[1] + " to: " + message[2]);
                        }
                    } catch (TaskNotFoundException e) {
                        event.getChannel().sendMessage(e.getMessage());
                    }
                }
                if (msg.getContent().contains("!scrumbot")) {
                    event.getChannel().sendMessage("```" +
                            "`!task [taskname]` - adds a new task\n" +
                            "`!tasklist` - displays the tasklist\n" +
                            "`!status [taskID] [status]` - update the status of a task\n" +
                            "`!assign [taskID] [name]` - assigns a person to a task (plain name only for now!)]\n" +
                            "`!remove [taskID]` - removes the task with taskID```");
                }
            }
        });
        table.dTableGenerator();
        api.disconnect();
    }

}
