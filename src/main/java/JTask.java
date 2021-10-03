/**
 * This class will communicate with redis to store and retrieve the data on the database
 */


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JTask {
    private Jedis jedis;
    private JedisPool pool;
    private ArrayList<String> hexCodes;
    private ArrayList<Task> tasks;
    public JTask(){
        URI redisURI = null;
        try {
            redisURI = new URI(System.getenv("REDISTOGO_URL"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        pool = new JedisPool(new JedisPoolConfig(),
                redisURI.getHost(),
                redisURI.getPort(),
                Protocol.DEFAULT_TIMEOUT,
                redisURI.getUserInfo().split(":",2)[1]);

        initLists();
    }

    /**
     * called by TableGenerator at its inception
     * retrieves the list from the database and populates the arraylist of tasks
     * called at the beginning of every session
    */
     private void initLists(){
         jedis = pool.getResource();
         tasks = new ArrayList<>();
         hexCodes = new ArrayList<>();
         Set<String> keys = jedis.keys("*");
         for (String key : keys){

             Map<String,String> map = jedis.hgetAll(key);
             String taskID = map.get("taskID");
             String taskName = map.get("taskName");
             String status = map.get("status");
             String assigned = map.get("assigned");

             Task task = new Task(taskName,status,assigned);
             task.setTaskID(taskID);
             String hexCode = taskID;

             hexCodes.add(hexCode);
             tasks.add(task);
         }
         jedis.close();
     }
     public ArrayList<Task> getTasks(){
         return tasks;
     }
     public ArrayList<String> getHexCodes(){
         return hexCodes;
     }
     // called everytime an entry is added to the arraylist of tasks in TableGenerator
     public void addEntryDB(String taskID, String taskName){
         jedis = pool.getResource();

         Map<String,String> map = new HashMap<String, String>();
         map.put("taskID",taskID);
         map.put("taskName",taskName);
         map.put("status","todo");
         map.put("assigned"," ");

         jedis.hmset(taskID,map);
         jedis.close();
     }
     // called everytime an entry is deleted from the arraylist of tasks in TableGenerator
     public void deleteEntryDB(String taskID){
         jedis = pool.getResource();
         jedis.del(taskID);
         jedis.close();
     }
     // called everytime an entry's status is updated in the arraylist of tasks in TableGenerator
     public void updateEntryStatusDB(String taskID,String status){
         jedis = pool.getResource();
         Map<String,String> map = jedis.hgetAll(taskID);
         map.put("status",status);
         jedis.hmset(taskID,map);
         jedis.close();
     }
     // called everytime and entry's assigned state is updated in the arraylist of tasks in TableGenerator
     public void updateEntryAssignedDB(String taskID, String assigned){
         jedis = pool.getResource();
         Map<String,String> map = jedis.hgetAll(taskID);
         map.put("assigned",assigned);
         jedis.hmset(taskID,map);
         jedis.close();
     }
}
