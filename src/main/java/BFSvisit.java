import java.util.*;

public class BFSvisit<T> {

    Stack<Node<T>> workingStack; // stack for discovered nodes
    Queue<Map.Entry<Node<T>,Integer>> workingQueue;
    Collection<Collection<T>> allPaths;
    int shortestSteps;

    public BFSvisit(){
        workingStack = new Stack<>();
        workingQueue = new LinkedList<>();
        allPaths = new ArrayList<>();
        shortestSteps = Integer.MAX_VALUE;
    }

    /**
     * Get all shortest allPaths from source to index
     * @param partOfGraph
     * @param dest
     * @return Collection of Collections of T
     */
    public Collection<Collection<T>> traverse(Traversable<T> partOfGraph,Node<T> dest){
        //get source from Traversable origin
        Node<T> source = partOfGraph.getOrigin();
        source.setParent(null);

        if(partOfGraph.getValue(source.getData()) == 0 || partOfGraph.getValue(dest.getData()) == 0){
            return allPaths;
        }

        // Create a queue for BFS
        // Distance of source cell is 0
        workingQueue.add(new AbstractMap.SimpleEntry<>(source,0));

        // Do a BFS starting from source cell
        while (!workingQueue.isEmpty())
        {
            //Remove first item in queue and save as current
            Map.Entry<Node<T>,Integer> current = workingQueue.remove();
            Node<T> pt = current.getKey();

            //Finding reachable nodes from current node
            Collection<Node<T>> reachableNodes = partOfGraph.getReachableNodes(pt,false);

            for (Node<T> reachable : reachableNodes)
            {
                // If we have reached the destination cell,
                // We found the shortest allPaths
                if (reachable.getData().equals(dest.getData())) {
                    //save the shortest allPaths length
                    shortestSteps = current.getValue();

                    //Setting up new allPaths
                    //Adding dest + current
                    List<T> newPath = new ArrayList<>();
                    newPath.add(reachable.getData());
                    newPath.add(pt.getData());
                    //go back to all node parents and add to list
                    Node<T> parent = pt.getParent();
                    while (parent != null){
                        pt = parent;
                        newPath.add(pt.getData());
                        parent = pt.getParent();
                    }
                    Collections.reverse(newPath);

                    //save new allPaths in all paths
                    allPaths.add(newPath);
                    break;
                }
                //Check if not visited (as parent) && current steps lower then shortest steps
                else if (!reachable.equals(pt.getParent()) && (current.getValue() < shortestSteps))
                {
                    //Adding reachable to working queue and increase steps by 1
                    workingQueue.add(new AbstractMap.SimpleEntry<>(reachable,current.getValue() + 1));
                }
            }
        }

        return allPaths;
    }
}