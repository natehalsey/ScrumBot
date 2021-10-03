/**
 * This class handles table generation and manages the arraylist of tasks
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Random;

public class TableGenerator {
    // the tasks to be inputted into the table
    ArrayList<Task> tasks;

    // to format the table
    int[] formatting;

    // the physical table
    String table;

    // a header task so we know if we're formatting the header, so we can add a divider
    Task headerTask;

    // keep a reference to all hexcodes entered
    ArrayList<String> hexCodes;

    JTask jtask;

    public TableGenerator(){
        table = "";

        jtask = new JTask();
        tasks = new ArrayList<>();
        hexCodes = new ArrayList<>();

        ArrayList<Task> jtaskLists = jtask.getTasks();
        initHeader();

        tasks.addAll(jtaskLists);
        hexCodes.addAll(jtask.getHexCodes());
        updateTable();
    }
    private void initHeader(){
        headerTask = new Task("Task Name", "Status", "Assigned");
        tasks.add(headerTask);
        hexCodes.add("FFFFFF");
        headerTask.setTaskID("Task ID");
    }
    public String getTableMD(){
        return "```"+table+"```";
    }
    public BufferedImage tableToImage(){
        String[] tableLines = table.split("\n");

        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Monospaced", Font.PLAIN, 15);
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();

        //since every line is equal we can set the width to be an arbitrary element in our tableLines array
        int width = fm.stringWidth(tableLines[0]);
        int height = fm.getHeight()+1;

        g2d.dispose();

        img = new BufferedImage(width, height*tableLines.length, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);

        g2d.drawString(tableLines[0],0,fm.getHeight()+1);
        for (int i = 1; i < tableLines.length; i++) {
            g2d.drawString(tableLines[i], 0, (fm.getHeight()*(i+1)));
        }
        g2d.dispose();
        return img;
    }
    public void updateTable(){

        // for now we reset the table to null, could get messy if we try to edit the string
        StringBuilder tableBuilder = new StringBuilder();
        //updates the formatting array with the largest values in every column
        getLargest();
        for (Task task : tasks) {

            // initiate a new padding array for every loop
            String[] padding = initPadding();

            // create new headers from the task input
            String[] headers = {
                    task.getTaskID(),
                    task.getTaskMessage(),
                    task.getStatus(),
                    task.getAssignedTo(),
            };
            // get the differences between the largest lengths and the length of the header strings
            int[] diff = {
                    formatting[0] - headers[0].length(),
                    formatting[1] - headers[1].length(),
                    formatting[2] - headers[2].length(),
                    formatting[3] - headers[3].length()
            };
            for (int i = 0; i < headers.length; i++) {
                if (i == 0) {
                    tableBuilder.append("|");
                }
                // the header is greater than the largest, get the difference and center the header
                // tableEntry = padding + diff/2 + word + diff/2 + padding
                if (!(diff[i] <= 0)) {
                    for (int j = 0; j < ((diff[i]+1) / 2); j++) {
                        padding[i] += " "; // add to the padding
                    }
                }
                StringBuilder tableEntry = new StringBuilder();
                tableEntry.append(padding[i]).append(headers[i]).append(padding[i]);

                // this could be confusing, essentially a forward search. We always ceiling an odd number, so if the formatting
                // fits exactly, with one space on each side, we just add another character, if it doesn't then it means it's odd
                if ((tableEntry.length() == (formatting[i]+2))){
                    tableEntry.append(" ");

                }
                tableBuilder.append(tableEntry).append("|");
            }
            tableBuilder.append("\n");
            if (task.equals(headerTask)){
                setDivider(tableBuilder,headers,padding);
            }
        }
        table = tableBuilder.toString();
    }
    public void updateEntryStatus(String taskID, String status) throws TaskNotFoundException{
        int index = hexCodes.indexOf(taskID);
        if (index != -1) {
            tasks.get(index).setStatus(status);
            jtask.updateEntryStatusDB(taskID,status);
            updateTable();
        } else {
            throw new TaskNotFoundException(taskID);
        }
    }
    public void updateEntryAssigned(String taskID, String assign) throws TaskNotFoundException{
        int index = hexCodes.indexOf(taskID);
        if (index != -1) {
            tasks.get(index).setAssignedTo(assign);
            jtask.updateEntryAssignedDB(taskID,assign);
            updateTable();
        } else {
            throw new TaskNotFoundException(taskID);
        }
    }
    public void addEntry(String taskName){
        tasks.add(new Task(taskName,"todo"," "));
        String hexCode = generateHexCode();
        while(hexCodes.contains(hexCode)) {
            hexCode = generateHexCode();
        }
        tasks.get(tasks.size()-1).setTaskID(hexCode);
        hexCodes.add(hexCode);
        jtask.addEntryDB(hexCode,taskName);
        updateTable();
    }
    public void removeEntry(String taskID) throws TaskNotFoundException{
        int index = hexCodes.indexOf(taskID);
        if (index != -1) {
            tasks.remove(index);
            hexCodes.remove(index);
            jtask.deleteEntryDB(taskID);
            updateTable();
        } else {
            throw new TaskNotFoundException(taskID);
        }
    }
    private void setDivider(StringBuilder tableBuilder,String[] headers, String[] padding){
        tableBuilder.append("|");
        for (int i = 0; i < headers.length; i++){
            int tableEntry = padding[i].length()+headers[i].length()+padding[i].length();
            for (int j = 0; j < tableEntry; j++){
                tableBuilder.append("-");
            }
            if (tableEntry == formatting[i]+2){
                tableBuilder.append("-");

            }
            tableBuilder.append("|");
        }
        tableBuilder.append("\n");
    }
    private String[] initPadding(){
        String[] padding = new String[4];
        for (int i = 0; i < 4; i++){
            padding[i] = " ";
        }
        return padding;
    }
    private void getLargest(){
        formatting = new int[4];

        int[] largestValues = {0,0,0,0};
        for (Task task : tasks){
            if (task.getTaskID().length() > largestValues[0]) {
                largestValues[0] = task.getTaskID().length();
            }
            if (task.getTaskMessage().length() > largestValues[1]) {
                largestValues[1] = task.getTaskMessage().length();
            }
            if (task.getStatus().length() > largestValues[2]) {
                largestValues[2] = task.getStatus().length();
            }
            if (task.getAssignedTo().length() > largestValues[3]) {
                largestValues[3] = task.getAssignedTo().length();
            }
        }
        System.arraycopy(largestValues, 0, formatting, 0, 4);
    }
    // generates a 4 digit hexadecimal code
    private String generateHexCode(){
        int maxValue = 0xFFFFFF;
        Random rand = new Random();
        int myHex = rand.nextInt(maxValue+1);
        return String.format("%06x",myHex);
    }

}
