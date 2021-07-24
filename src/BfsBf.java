import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class BfsBf<T> {

    protected final ThreadLocal<HashMap<T,Collection<Pair<List<T>,Integer>>>> visited =
            ThreadLocal.withInitial(HashMap::new);


    public BfsBf(){
    }


    public Collection<Pair<List<T>,Integer>> getPaths(Traversable<T> partOfGraph, List<T> prev,Node<T> source, Node<T> dest ){

        Collection<Pair<List<T>,Integer>> paths = new ArrayList<>();

        Collection<Future<Collection<Pair<List<T>,Integer>>>> futures = new ArrayList<>();

        if(source.equals(dest)){
            List<T> t = new ArrayList<>();
            t.add(source.getData());
            paths.add( new Pair<>(t,0));
            return paths;
        }

        prev.add(source.getData());
        int value = partOfGraph.getValue(source.getData());

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 4, 50,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        Collection<Node<T>> reachableNodes = partOfGraph.getNeighborNodes(source,false);

        for (Node<T> reachable : reachableNodes) {
            if(visited.get().containsKey(reachable.getData())){
                paths.addAll(visited.get().get(reachable.getData()));
            }
            else if(!prev.contains(reachable.getData())) {
                Callable<Collection<Pair<List<T>, Integer>>> taskToHandle = () ->
                {
                    List<T> newPrev = new ArrayList<>(prev);
                    return getPaths(partOfGraph, newPrev, reachable, dest);
                };

                futures.add(threadPool.submit(taskToHandle));
            }
        }

        for (Future<Collection<Pair<List<T>,Integer>>> future : futures){
            try {
                Collection<Pair<List<T>,Integer>> pairs = future.get();
                paths.addAll(pairs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        paths = getMinPaths(paths,source.getData(),value);
        visited.get().put(source.getData(),paths);

        return paths;
    }

    private Collection<Pair<List<T>,Integer>> getMinPaths( Collection<Pair<List<T>,Integer>> paths,T source,int value) {
        OptionalInt minPath = paths.stream().mapToInt(Pair::getValue).min();
        Collection<Pair<List<T>, Integer>> finalPaths = new ArrayList<>();

        if (minPath.isPresent()) {
            int min = minPath.getAsInt();
            for (Pair<List<T>, Integer> pair : paths) {
                if(pair.getValue() == min) {
                    pair.getKey().add(source);
                    finalPaths.add(new Pair<>(pair.getKey(), pair.getValue() + value));
                }
            }
        }
        return finalPaths;
    }
}


/*
    1  50 30
    15 20 10
   -80 35 55
 */

/*
shortest = -9
(1,15,-80,35,20)
 */

/*
 Queue = (1,50) , (1,15)
 Queue = (1,15)
 Queue = (1,15) (1,50,30) (1,50 ,20)
 Queue = (1,50,30) (1,50 ,20)
 Queue = (1,50,30) (1,50 ,20) (1,15,20) (1,15,-8)
 Queue = (1,50,30) (1,50 ,20) (1,15,20) (1,15,-8)
 Queue =  (1,50 ,20) (1,15,20) (1,15,-8) (1,50,30,10)
 Queue =  (1,15,-80) (1,50,30,10)
 Queue =  (1,15,-80) (1,50,30,10)
 Queue =   (1,50,30,10) (1,15,-80,35)
 Queue =   (1,15,-80,35) (1,50,30,10,20) (1,50,30,10,55)
 Queue =    (1,50,30,10,20) (1,50,30,10,55) (1,15,-80,35,20) (1,15,-80,35,55)
 Queue =   (1,15,-80,35,20) (1,15,-80,35,55) (1,50,30,10,35)
 Queue =   (1,15,-80,35,55) (1,50,30,10,35)
 Queue =    (1,50,30,10,35) (1,15,-80,35,55,10)
 Queue =    (1,15,-80,35,55,10)  (1,50,30,10,35,20)  (1,50,30,10,35,-80)
 Queue =      (1,50,30,10,35,20)  (1,50,30,10,35,-80) (1,15,-80,35,55,10,30) (1,15,-80,35,55,10,20)
*/