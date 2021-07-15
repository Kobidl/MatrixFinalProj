import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class MatrixIHandler implements IHandler {
    private Matrix matrix;
    private Index start,end;

    /*
    to clear data members between clients (if same instance is shared among clients/tasks)
     */
    private void resetParams(){
        this.matrix = null;
        this.start = null;
        this.end = null;
    }

    @Override
    public void handle(InputStream fromClient, OutputStream toClient)
            throws IOException, ClassNotFoundException {

        // In order to read either objects or primitive types we can use ObjectInputStream
        ObjectInputStream objectInputStream = new ObjectInputStream(fromClient);
        // In order to write either objects or primitive types we can use ObjectOutputStream
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(toClient);
        this.resetParams(); // in order to use same handler between tasks/clients

        boolean doWork = true;
        while(doWork){
            /*
             Use switch-case in order to get commands from client
             - client sends a 2D array
             - client send start index
             - client send end index
             - client sends an index and wished to get neighbors
             - client sends an index and wished to get reachable indices
             */

            // client send a verbal command
            switch(objectInputStream.readObject().toString()){
                case "matrix":{
                    // client will send a 2d array. handler will create a new Matrix object
                    int[][] primitiveMatrix = (int[][])objectInputStream.readObject();
                    System.out.println("Server: Got 2d array from client");
                    this.matrix = new Matrix(primitiveMatrix);
                    this.matrix.printMatrix();
                    break;
                }

                case "getAllLinkedPoints":
                    try {
                        getAllLinkedPoints(objectInputStream,objectOutputStream);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;

                case "getShortestPath":
                    TraversableMatrix traversableMatrix;
                    traversableMatrix = new TraversableMatrix(this.matrix);
                    Index source  = (Index)objectInputStream.readObject();
                    Index dest = (Index)objectInputStream.readObject();
                    BFSvisit<Index> bfsVisit = new BFSvisit<>();

                    Collection<Collection<Index>> path = bfsVisit.traverse(traversableMatrix,new Node(source),new Node(dest));

                    //return to client
                    objectOutputStream.writeObject(path);
                    break;

                case "getLightestPath":
                    TraversableMatrix traversableMatrix2;
                    traversableMatrix2 = new TraversableMatrix(this.matrix);
                    Index source2  = (Index)objectInputStream.readObject();
                    Index dest2 = (Index)objectInputStream.readObject();
                    BFSvisit2<Index> bfsVisit2 = new BFSvisit2<>();

                    Collection<Collection<Index>> path2 = bfsVisit2.traverse(traversableMatrix2,new Node(source2),new Node(dest2));

                    //return to client
                    objectOutputStream.writeObject(path2);
                    break;
//                case "neighbors":{
//                    Index findNeighborsIndex = (Index)objectInputStream.readObject();
//                    List<Index> neighbors = new ArrayList<>();
//                    if(this.matrix!=null){
//                        neighbors.addAll(this.matrix.getNeighbors(findNeighborsIndex,false));
//                        // print result in server
//                        System.out.println("neighbors of " + findNeighborsIndex + ": " + neighbors);
//                        // send to socket's OutputStream
//                        objectOutputStream.writeObject(neighbors);
//                    }
//                    break;
//                }

//                case "reachables":{
//                    Index findNeighborsIndex = (Index)objectInputStream.readObject();
//                    List<Index> reachables = new ArrayList<>();
//                    if(this.matrix!=null){
//                        reachables.addAll(this.matrix.getReachables(findNeighborsIndex));
//                        // print result in server
//                        System.out.println("reachables of " + findNeighborsIndex + ": " + reachables);
//                        // send to socket's OutputStream
//                        objectOutputStream.writeObject(reachables);
//                    }
//                    break;
//                }

                case "start index":{
                    this.start = (Index)objectInputStream.readObject();
                    break;
                }

                case "end index":{
                    this.end = (Index)objectInputStream.readObject();
                    break;
                }

                case "stop":{
                    doWork = false;
                    break;
                }
            }
        }
    }

    private void getAllLinkedPoints(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException, InterruptedException {
        //get all active points
       List<Index> actives = this.matrix.getAllActivePoints();
       List<Index> found = Collections.synchronizedList(new ArrayList<>());

       List<Future<HashSet<Index>>> futures = new ArrayList<>();

        //run traverse dfs on all active points
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, 5, 10,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        ThreadLocalDfsVisit threadLocalDfsVisit = new ThreadLocalDfsVisit<Index>();


        while(!actives.isEmpty()){
            Index active = actives.remove(0);

            Callable<HashSet<Index>> taskHandle = () -> {
                if(active != null) {
                    TraversableMatrix traversableMatrix = new TraversableMatrix(this.matrix);
                    traversableMatrix.setStartIndex(active);

                    //Double check synchronized
                    if(found.contains(active)) {
                        return new HashSet<>();
                    }
                    synchronized (found) {
                        if (found.contains(active)) {
                            return new HashSet<>();
                        }
                    }

                    //finding all the linked points and add to HashSet
                    List<Index> points = threadLocalDfsVisit.traverse(traversableMatrix, true);

                    //Double check synchronized
                    if(found.contains(active)) {
                        return new HashSet<>();
                    }
                    synchronized (found){
                        if(!found.contains(active)){
                            found.addAll(points);
                            return new HashSet<>(points);
                        }
                    }
                }
                return new HashSet<>();
            };

            futures.add(threadPool.submit(taskHandle));
        }

        List<HashSet<Index>> finalList = new ArrayList<>();
        for (Future<HashSet<Index>> future : futures) {
            try {
                HashSet<Index> points = future.get();
                if (points.size() > 0) {
                    finalList.add(points);
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        //Sort by size
        finalList = finalList.stream().sorted(Comparator.comparingInt(HashSet::size)).collect(Collectors.toList());

        //return to client
        objectOutputStream.writeObject(finalList);
    }
}