import javafx.util.Pair;

import java.util.*;

public class BFSvisit2<T> {

    Stack<Node<T>> workingStack; // stack for discovered nodes
    Queue<Pair<List<T>,Integer>> workingQueue;
    Collection<Collection<T>> path;
    int shortestPath;

    public BFSvisit2(){
        workingStack = new Stack<>();
        workingQueue = new LinkedList<>();
        path = new ArrayList<>();
        shortestPath = Integer.MAX_VALUE;
    }


    public Collection<Collection<T>> traverse(Traversable<T> partOfGraph,Node<T> source,Node<T> dest){

        // Create a queue for BFS
        //workingQueue = new LinkedList<>();

        // Distance of source cell is 0
        List<T> list = new ArrayList();
        list.add(source.getData());
        workingQueue.add(new Pair<>(list,partOfGraph.getValue(source.getData())));
        // Do a BFS starting from source cell

        while (!workingQueue.isEmpty())
        {
            Pair<List<T>,Integer> curr = workingQueue.peek();
            List<T> ptList = curr.getKey();
            Node<T> pt = new Node(ptList.get(0));

            // Otherwise dequeue the front cell
            // in the queue and enqueue
            // its adjacent cells
            workingQueue.remove();

            Collection<Node<T>> reachableNodes = partOfGraph.getNeighborNodes(pt,false);

            for (Node<T> reachable : reachableNodes)
            {
                // If we have reached the destination cell,
                // We found the shortest path

                if (reachable.getData().equals(dest.getData())) {
                    if(shortestPath > curr.getValue()){
                        shortestPath = curr.getValue();
                        path = new ArrayList<>();

                        List<T> finalPath = new ArrayList<>(ptList);
                        Collections.reverse(finalPath);
                        finalPath.add(reachable.getData());
                        path.add(finalPath);
                    }else if(shortestPath == curr.getValue()){
                        List<T> finalPath = new ArrayList<>(ptList);
                        Collections.reverse(finalPath);
                        finalPath.add(reachable.getData());
                        path.add(finalPath);
                    }
                }
                else if (!ptList.contains(reachable.getData()))
                {
                    // mark cell as visited and enqueue it
                    //visited.add(reachable);
                    List<T> list2 = new ArrayList<>(ptList);
                    list2.add(0,reachable.getData());
                    workingQueue.add(new Pair<>(list2,curr.getValue() + partOfGraph.getValue(reachable.getData())));
                }
            }
        }

        return path;
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