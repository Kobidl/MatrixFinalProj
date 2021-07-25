import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MatrixIHandler implements IHandler {
    private Matrix matrix;
    private Index start,end;
    private TraversableMatrix traversableMatrix;

    /*
    to clear data members between clients (if same instance is shared among clients/tasks)
     */
    private void resetParams(){
        this.matrix = null;
        this.start = null;
        this.end = null;
        this.traversableMatrix = null;
    }

    @Override
    public void handle(InputStream fromClient, OutputStream toClient)
            throws Exception {

        // In order to read either objects or primitive types we can use ObjectInputStream
        ObjectInputStream objectInputStream = new ObjectInputStream(fromClient);
        // In order to write either objects or primitive types we can use ObjectOutputStream
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(toClient);
        this.resetParams(); // in order to use same handler between tasks/clients

        boolean doWork = true;
        while (doWork) {
            /*
             Use switch-case in order to get commands from client
             - client sends a 2D array
             - client send start index
             - client send end index
             - client sends an index and wished to get neighbors
             - client sends an index and wished to get reachable indices
             */

            // client send a verbal command
            switch (objectInputStream.readObject().toString()) {
                case "matrix": {
                    // client will send a 2d array. handler will create a new Matrix object
                    try {
                        int[][] primitiveMatrix = (int[][]) objectInputStream.readObject();
                        System.out.println("Server: Got 2d array from client");
                        this.matrix = new Matrix(primitiveMatrix);
                        this.matrix.printMatrix();
                        traversableMatrix = new TraversableMatrix(this.matrix);
                    } catch (Exception e) {
                        System.out.println("matrix - Invalid Matrix from client");
                        throw new Exception("Invalid Matrix");
                    }
                    break;
                }
                case "start index": {
                    try {
                        this.start = (Index) objectInputStream.readObject();
                    } catch (ClassCastException e) {
                        throw new Exception("Invalid source");
                    }
                    break;
                }

                case "end index": {
                    try {
                        this.end = (Index) objectInputStream.readObject();
                    } catch (ClassCastException e) {
                        throw new Exception("Invalid dest");
                    }
                    break;
                }
                //Task 1
                case "getAllLinkedPoints":
                    validateMatrix();

                    try {
                        List<HashSet<Index>> finalList = getAllLinkedPoints();
                        objectOutputStream.writeObject(finalList);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.out.println("getAllLinkedPoints - something went wrong: " + e.getMessage());
                        throw e;
                    }

                    break;
                //Task 2
                case "getShortestPath":
                    Collection<Collection<Index>> path = getShortestPath();

                    //return to client
                    objectOutputStream.writeObject(path);

                    break;
                //Task 3
                case "getNumOfValidSubmarines":
                    validateMatrix();
                    try {
                        //Get all linked points
                        List<HashSet<Index>> finalList = getAllLinkedPoints();
                        int num = getValidateSubmarines(finalList);
                        objectOutputStream.writeObject(num);
                    } catch (InterruptedException e) {
                        throw e;
                    }
                    break;
                //Task 4
                case "getLightestPath":
                    Collection<Collection<Index>> lightestPaths = getLightestPaths();
                    //return to client
                    objectOutputStream.writeObject(lightestPaths);
                    break;


                case "stop": {
                    doWork = false;
                    break;
                }
            }
        }
    }

    private Collection<Collection<Index>> getLightestPaths() throws Exception{
        validateMatrix();
        validateStartIndex();
        validateEndIndex();

        BfsBfVisit<Index> bfsVisit2 = new BfsBfVisit<>();

        Collection<Pair<List<Index>, Integer>> pairs = bfsVisit2.getLightestPaths(traversableMatrix, new ArrayList<>(), new DirectNode<>(start), new DirectNode<>(end));
        return pairs.stream().map(p -> reverse(p.getKey())).collect(Collectors.toList());
    }

    private Collection<Collection<Index>> getShortestPath()throws Exception {
        validateMatrix();
        validateStartIndex();
        validateEndIndex();

        if (this.matrix.getPrimitiveMatrix().length > 50 || this.matrix.getPrimitiveMatrix()[0].length > 50) {
            throw new Exception("Invalid matrix - max should be 50 x 50");
        }

        BFSvisit<Index> bfsVisit = new BFSvisit<>();
        traversableMatrix.setStartIndex(this.start);
        return bfsVisit.traverse(traversableMatrix, new Node(this.end));
    }

    private void validateStartIndex() throws Exception{
        if(start == null){
            throw new Exception("No start index found");
        }
        if (!traversableMatrix.isValidIndex(this.start)) {
            throw new Exception("Source is out of matrix");
        }
    }

    private void validateEndIndex() throws Exception{
        if(end == null){
            throw new Exception("No end index found");
        }
        if (!traversableMatrix.isValidIndex(this.start)) {
            throw new Exception("Source is out of matrix");
        }
    }

    private void validateMatrix() throws Exception{
        if(matrix == null){
            throw new Exception("No matrix found");
        }
    }

    private List<Index> reverse(List<Index> list){
        Collections.reverse(list);
        return list;
    }

    /**
     * Get count of validate submarines
     * @param finalList List of linked points
     * @return number of validate submarines
     */
    private int getValidateSubmarines(List<HashSet<Index>> finalList) {
        int counter = 0;
        List<Future<Boolean>> futures = new ArrayList<>();
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, 5, 10,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        //Go over all linked points
        //Each one run as callable and calculate if validate
        //Return in Future if linked points list are validate submarine
        while(!finalList.isEmpty()){
            HashSet<Index> item = finalList.remove(0);

            Callable<Boolean> taskHandle = () -> {
                if(item != null && item.size() > 1) {
                    //init min , max
                    int minRow = Integer.MAX_VALUE;
                    int minCol = Integer.MAX_VALUE;
                    int maxRow = 0;
                    int maxCol = 0;

                    //Find min row , min col, max row , max col
                    //Find rectangle expected items count based on borders using local min and max
                    //check if equal to number of indexes in the list
                    for (Index index : item) {
                        if(index.row < minRow){
                            minRow = index.row;
                        }
                        if(index.column < minCol){
                            minCol = index.column;
                        }
                        if (index.row > maxRow) {
                            maxRow = index.row;
                        }
                        if (index.column > maxCol) {
                            maxCol = index.column;
                        }
                    }
                    return (maxRow - minRow + 1) * (maxCol - minCol + 1) == item.size();
                }
                return false;
            };
            futures.add(threadPool.submit(taskHandle));
        }

        for (Future<Boolean> future : futures) {
            try {
                if(future.get()){
                    counter++;
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return counter;
    }

    /**
     * Get all linked points
     * @return List of HasSet of Index
     * @throws InterruptedException
     */
    private List<HashSet<Index>> getAllLinkedPoints() throws InterruptedException {
        //get all active points
        List<Index> actives = this.matrix.getAllActivePoints();
        if (actives.size() == 0) { //if 0 return empty
            return new ArrayList<>();
        }

        //init sync list which will contains indexes that already found in linked
        List<Index> found = Collections.synchronizedList(new ArrayList<>());
        //init Future list tasks to wait for result from traverse
        List<Future<HashSet<Index>>> futures = new ArrayList<>();

        //run traverse dfs on all active points
        int maxThreads = actives.size() < 10 ? actives.size() : 10;
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, maxThreads, 10,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        ThreadLocalDfsVisit threadLocalDfsVisit = new ThreadLocalDfsVisit<Index>();

        while (!actives.isEmpty()) {
            Index active = actives.remove(0);

            Callable<HashSet<Index>> taskHandle = () -> {
                if (active != null) {
                    // init part of graph based on active index
                    TraversableMatrix traversableMatrix = new TraversableMatrix(this.matrix);
                    traversableMatrix.setStartIndex(active);

                    //Double check synchronized if index already found
                    if (found.contains(active)) {
                        return new HashSet<>();
                    }

                    //Clear ThreadLocal data
                    threadLocalDfsVisit.reset();

                    //Finding all the linked points from active and add to HashSet
                    HashSet<Index> points = threadLocalDfsVisit.traverse(traversableMatrix, true);

                    //Double check synchronized if index already found
                    if (!found.contains(active)) {
                        synchronized (found) {
                            if (!found.contains(active)) {
                                found.addAll(points);
                                return points;
                            }
                        }
                    }
                }
                return new HashSet<>();
            };

            //submit task and add to futures
            futures.add(threadPool.submit(taskHandle));
        }

        List<HashSet<Index>> finalList = new ArrayList<>();
        for (Future<HashSet<Index>> future : futures) {
            try {
                //get HashSet from future
                HashSet<Index> points = future.get();
                //if not empty add to final list
                if (!points.isEmpty()) {
                    finalList.add(points);
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        //Sort by size and return
        return finalList.stream().sorted(Comparator.comparingInt(HashSet::size)).collect(Collectors.toList());
    }
}