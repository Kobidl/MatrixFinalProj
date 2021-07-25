import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class BfsBfVisit<T> {

    protected HashMap<Node<T>,Collection<Pair<List<T>,Integer>>> visited = new HashMap<>();

    ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

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

        if(source.equals(dest)){
            List<T> t = new ArrayList<>();
            t.add(source.getData());
            paths.add( new Pair<>(t,partOfGraph.getValue(dest.getData())));
            return paths;
        }

        //if visited add all known lightest paths
        readWriteLock.readLock().lock();
        try {
            if (visited.containsKey(source)) {
                paths = new ArrayList<>(visited.get(source));
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }finally {
            readWriteLock.readLock().unlock();
        }


        //if reached to end return path with dest point


        //adding point to black list
        blackList.add(source.getData());
        int value = partOfGraph.getValue(source.getData());

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 4, 50,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        Collection<Node<T>> neighborNodes = partOfGraph.getNeighborNodes(source,false);
        for (Node<T> neighbor : neighborNodes) {
            if (blackList.contains(neighbor.getData())) {
                continue;
            }
            if (paths.stream().anyMatch(p -> p.getKey().contains(neighbor)))
                continue;

            // if not visited and not in black list
            // create callable and add to future
            // recursive call to getLightestPaths sending neighbor

            Callable<Collection<Pair<List<T>, Integer>>> taskToHandle = () ->
            {
                List<T> newPrev = new ArrayList<>(blackList);
                return getLightestPaths(partOfGraph, newPrev, neighbor, dest);
            };

            futures.add(threadPool.submit(taskToHandle));
        }

        for (Future<Collection<Pair<List<T>,Integer>>> future : futures){
            try {
                //Get all neighbors paths from future
                Collection<Pair<List<T>,Integer>> pairs = future.get();
                for (Pair<List<T>, Integer> pair : pairs) {
                    if(!pair.getKey().contains(source.getData())) {
                        //add the source to path and increase the value
                        Pair<List<T>, Integer> newPair = new Pair<>(new ArrayList<>(pair.getKey()), pair.getValue() + value);
                        newPair.getKey().add(source.getData());
                        paths.add(newPair);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        //Sending all neighbors paths and source data
        paths = getMinPaths(paths);

        //mark in visited and save lightest paths data
        if(paths.size() > 0) {
            readWriteLock.writeLock().lock();
            try {
                visited.put(source, paths);
            } catch (Exception ignored) {
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }
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
