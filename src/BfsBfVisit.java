import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.*;

public class BfsBfVisit<T> {

    protected final ThreadLocal<HashMap<T,Collection<Pair<List<T>,Integer>>>> visited =
            ThreadLocal.withInitial(HashMap::new);


    public BfsBfVisit(){
    }


    /**
     * Get lightest paths from source to dest in graph
     * @param partOfGraph
     * @param blackList
     * @param source
     * @param dest
     * @return
     */
    public Collection<Pair<List<T>,Integer>> getLightestPaths(Traversable<T> partOfGraph, List<T> blackList, Node<T> source, Node<T> dest ){

        Collection<Pair<List<T>,Integer>> paths = new ArrayList<>();

        Collection<Future<Collection<Pair<List<T>,Integer>>>> futures = new ArrayList<>();

        //if reached to end return path with dest point
        if(source.equals(dest)){
            List<T> t = new ArrayList<>();
            t.add(source.getData());
            paths.add( new Pair<>(t,0));
            return paths;
        }

        //adding point to black list
        blackList.add(source.getData());
        int value = partOfGraph.getValue(source.getData());

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 4, 50,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        Collection<Node<T>> neighborNodes = partOfGraph.getNeighborNodes(source,false);

        for (Node<T> neighbor : neighborNodes) {
            //if visited add all known lightest paths
            if(visited.get().containsKey(neighbor.getData())){
                paths.addAll(visited.get().get(neighbor.getData()));
            }
            // if not visited and not in black list
            // create callable and add to future
            // recursive call to getLightestPaths sending neighbor
            else if(!blackList.contains(neighbor.getData())) {
                Callable<Collection<Pair<List<T>, Integer>>> taskToHandle = () ->
                {
                    List<T> newPrev = new ArrayList<>(blackList);
                    return getLightestPaths(partOfGraph, newPrev, neighbor, dest);
                };

                futures.add(threadPool.submit(taskToHandle));
            }
        }

        for (Future<Collection<Pair<List<T>,Integer>>> future : futures){
            try {
                //Get all neighbors paths from future
                Collection<Pair<List<T>,Integer>> pairs = future.get();
                paths.addAll(pairs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        //Sending all neighbors paths and source data
        paths = getMinPaths(paths,source.getData(),value);

        //mark in visited and save lightest paths data
        visited.get().put(source.getData(),paths);

        return paths;
    }

    /**
     * Add source node to all lightest neighbors paths and increase the value
     * Returns the new paths
     * @param paths
     * @param source
     * @param value
     * @return lightest paths includes source
     */
    private Collection<Pair<List<T>,Integer>> getMinPaths( Collection<Pair<List<T>,Integer>> paths,T source,int value) {
        OptionalInt minPath = paths.stream().mapToInt(Pair::getValue).min();
        Collection<Pair<List<T>, Integer>> finalPaths = new ArrayList<>();

        if (minPath.isPresent()) {
            int min = minPath.getAsInt();
            for (Pair<List<T>, Integer> pair : paths) {
                if(pair.getValue() == min) {
                    //add the source to path and increase the value
                    pair.getKey().add(source);
                    finalPaths.add(new Pair<>(pair.getKey(), pair.getValue() + value));
                }
            }
        }
        return finalPaths;
    }
}
