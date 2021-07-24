import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class BFSvisit3<T> {

    protected final ThreadLocal<HashMap<T,Collection<Pair<List<T>,Integer>>>> visited =
            ThreadLocal.withInitial(HashMap::new);


    public BFSvisit3(){
    }


    public Collection<Pair<List<T>,Integer>> getPaths(Traversable<T> partOfGraph, List<T> prev,int sumOfPrev,Node<T> source, Node<T> dest ){

        Collection<Pair<List<T>,Integer>> paths = new ArrayList<>();

        Collection<Future<Collection<Pair<List<T>,Integer>>>> futures = new ArrayList<>();
        if(source.equals(dest)){
            prev.add(dest.getData());

            paths.add( new Pair<>(prev,0));
            return paths;
        }

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 4, 50,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        Collection<Node<T>> reachableNodes = partOfGraph.getNeighborNodes(source,false);
        final int sum = sumOfPrev + partOfGraph.getValue(source.getData());
        for (Node<T> reachable : reachableNodes)
        {
            if(reachable.equals(dest)) {
                List<T> newPath = new ArrayList<>(prev);
                newPath.add(source.getData());
                newPath.add(reachable.getData());
                paths.add(new Pair<>(newPath,sum));
            }else if(!prev.contains(reachable.getData())){
                if(visited.get().containsKey(reachable.getData())){
                    return visited.get().get(reachable.getData());
                }else {
                    Callable<Collection<Pair<List<T>, Integer>>> taskToHandle = () ->
                    {
                        List<T> newPrev = new ArrayList<>(prev);
                        newPrev.add(source.getData());
                        return getPaths(partOfGraph, newPrev, sum, reachable, dest);
                    };

                    futures.add(threadPool.submit(taskToHandle));
                }
            }
        }

        for (Future<Collection<Pair<List<T>,Integer>>> future : futures){
            try {
                Collection<Pair<List<T>,Integer>> pairs = future.get();
                paths.addAll(pairs);
                //int minPath = pairs.stream().mapToInt(Pair::getValue).min().getAsInt();
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
            visited.get().put(source.getData(),
                    paths.stream().map(p->new Pair<>(difference(p.getKey(),prev),p.getValue() - sum )).collect(Collectors.toList()));
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