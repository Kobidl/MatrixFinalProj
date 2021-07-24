import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class BFSvisit4<T> {

    protected final ThreadLocal<HashMap<T,Collection<Pair<List<T>,Integer>>>> visited =
            ThreadLocal.withInitial(HashMap::new);


    public Collection<Pair<List<T>,Integer>> getPaths(Traversable<T> partOfGraph,Node<T> source, Node<T> dest ){

        Collection<Pair<List<T>,Integer>> paths = new ArrayList<>();
        Collection<Future<Collection<Pair<List<T>,Integer>>>> futures = new ArrayList<>();

        if(source.equals(dest)){
            List<T> t = new ArrayList<>();
            t.add(source.getData());
            paths.add( new Pair<>(t,0));
            return paths;
        }


        partOfGraph.addBanIndex(source.getData());
        int value = partOfGraph.getValue(source.getData());

        Collection<Node<T>> reachableNodes = partOfGraph.getNeighborNodes(source,false);
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 4, 50,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        for (Node<T> reachable : reachableNodes) {

            Callable<Collection<Pair<List<T>, Integer>>> taskToHandle = () -> getPaths(partOfGraph,reachable,dest);

            futures.add(threadPool.submit(taskToHandle));
        }

        for (Future<Collection<Pair<List<T>,Integer>>> future : futures){
            try {
                Collection<Pair<List<T>,Integer>> temp = future.get();
                Collection<Pair<List<T>,Integer>> pairs = new ArrayList<>();
                for (Pair<List<T>,Integer> pair: temp) {
                    pair.getKey().add(source.getData());
                    pairs.add(new Pair<>(pair.getKey(),pair.getValue()+value));
                }
                paths.addAll(pairs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        OptionalInt minPath = paths.stream().mapToInt(Pair::getValue).min();
        if(minPath.isPresent()){
            int min = minPath.getAsInt();
            paths = paths.stream().filter(p->p.getValue() == min).collect(Collectors.toList());
//            visited.get().put(source.getData(),
//                    paths.stream().map(p->new Pair<>(difference(p.getKey(),listThreadLocal.get()),p.getValue() - sum )).collect(Collectors.toList()));
        }

        return paths;
    }

    public <T> List<T> difference(List<T> first, List<T> second) {
        List<T> toReturn = new ArrayList<>(first);
        toReturn.removeAll(second);
        return toReturn;
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