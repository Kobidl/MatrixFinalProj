import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class BfsBfVisit<T> {

    private HashMap<DirectNode<T>, Future<Collection<Pair<List<T>, Integer>>>> visited = new HashMap<>();

    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public BfsBfVisit() {

    }

    /**
     * Get lightest paths from source to dest in graph
     *
     * @param partOfGraph
     * @param blackList
     * @param source
     * @param dest
     * @return
     */
    public Collection<Pair<List<T>, Integer>> getLightestPaths(Traversable<T> partOfGraph, List<T> blackList, DirectNode<T> source, DirectNode<T> dest) {

        Collection<Pair<List<T>, Integer>> paths = new ArrayList<>();
        Collection<Future<Collection<Pair<List<T>, Integer>>>> futures = new ArrayList<>();
        int newPaths = 0;

        //if reached to end return path with dest point
        if (source.getData().equals(dest.getData())) {
            List<T> t = new ArrayList<>();
            t.add(source.getData());
            paths.add(new Pair<>(t, partOfGraph.getValue(dest.getData())));
            return paths;
        }


        blackList.add(source.getData());
        int value = partOfGraph.getValue(source.getData());


        Collection<DirectNode<T>> neighborNodes = partOfGraph.getNeighborNodes(source, false);

        for (DirectNode<T> neighbor : neighborNodes) {
            if (blackList.contains(neighbor.getData()))
                continue;
            if(neighbor.getData().equals(dest.getData())){
                List<T> t = new ArrayList<>();
                t.add(dest.getData());
                t.add(source.getData());
                paths.add(new Pair<>(t, partOfGraph.getValue(dest.getData())+value));
                continue;
            }

            readWriteLock.writeLock().lock();
            if (visited.containsKey(neighbor)) {
                futures.add(visited.get(neighbor));
            } else {
                Callable<Collection<Pair<List<T>, Integer>>> taskToHandle = () ->
                {
                    List<T> newPrev = new ArrayList<>(blackList);
                    return getLightestPaths(partOfGraph, newPrev, neighbor, dest);
                };
                FutureTask future = new FutureTask(taskToHandle);
                Thread thread = new Thread(future);
                thread.run();
                visited.put(neighbor, future);
                futures.add(future);
            }
            readWriteLock.writeLock().unlock();
        }

        for (Future<Collection<Pair<List<T>, Integer>>> future : futures) {
            try {
                //Get all neighbors paths from future
                Collection<Pair<List<T>, Integer>> pairs = future.get();
                for (Pair<List<T>, Integer> pair : pairs) {
                    if (!pair.getKey().contains(source.getData())) {
                        //add the source to path and increase the value
                        Pair<List<T>, Integer> newPair = new Pair<>(new ArrayList<>(pair.getKey()), pair.getValue() + value);
                        newPair.getKey().add(source.getData());
                        paths.add(newPair);
                        newPaths++;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (newPaths > 0) {
            //get min paths
            paths = getMinPaths(paths);
        }

        //return paths
        return paths;
    }

    /**
     * Add source node to all lightest neighbors paths and increase the value
     * Returns the new paths
     * @param paths
     * @return lightest paths includes source
     */
    private Collection<Pair<List<T>,Integer>> getMinPaths( Collection<Pair<List<T>,Integer>> paths) {
        OptionalInt minPath = paths.stream().mapToInt(Pair::getValue).min();

        if (minPath.isPresent()) {
            int min = minPath.getAsInt();
            return paths.stream().distinct().filter(p->p.getValue() == min).collect(Collectors.toList());
        }

        return paths;
    }
}
