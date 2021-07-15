import javafx.util.Pair;

import java.util.*;

public class BFSvisit<T> {

    Stack<Node<T>> workingStack; // stack for discovered nodes
    Queue<Pair<Node<T>,Integer>> workingQueue;
    Collection<Collection<T>> path;
    int shortestPath;

    public BFSvisit(){
        workingStack = new Stack<>();
        workingQueue = new LinkedList<>();
        path = new ArrayList<>();
        shortestPath = Integer.MAX_VALUE;
    }

    public Collection<Collection<T>> traverse(Traversable<T> partOfGraph,Node<T> source,Node<T> dest){

        if(partOfGraph.getValue(source.getData()) == 0 || partOfGraph.getValue(dest.getData()) == 0){
            return path;
        }

        // Create a queue for BFS
        //workingQueue = new LinkedList<>();

        // Distance of source cell is 0
        workingQueue.add(new Pair<>(source,0));
        // Do a BFS starting from source cell

        source.setParent(null);

        while (!workingQueue.isEmpty())
        {
            Pair<Node<T>,Integer> curr = workingQueue.peek();
            Node<T> pt = curr.getKey();

            // Otherwise dequeue the front cell
            // in the queue and enqueue
            // its adjacent cells
            workingQueue.remove();

            Collection<Node<T>> reachableNodes = partOfGraph.getReachableNodes(pt,false);

            for (Node<T> reachable : reachableNodes)
            {
                // If we have reached the destination cell,
                // We found the shortest path

                if (reachable.getData().equals(dest.getData())) {
                    List<T> temp = new ArrayList<>();
                    temp.add(reachable.getData());
                    shortestPath = curr.getValue();
                    temp.add(pt.getData());
                    Node<T> parent = pt.getParent();
                    while (parent != null){
                        pt = parent;
                        temp.add(pt.getData());
                        parent = pt.getParent();
                    }
                    Collections.reverse(temp);
                    path.add(temp);
                    break;
                }
                else if (!reachable.equals(pt.getParent()) && (curr.getValue() < shortestPath))
                {
                    // mark cell as visited and enqueue it
                    //visited.add(reachable);
                    workingQueue.add(new Pair<>(reachable,curr.getValue() + 1));
                }
            }
        }

        return path;
    }
}


/*
   100 50 200
   -50 -20 50
 */