import java.util.*;

public class DFSvisit<T> {
    /**
     * TLS - Thread-Local Storage
     */
    Stack<Node<T>> workingStack; // stack for discovered nodes
    Set<Node<T>> finished;       // set for finished nodes

    public DFSvisit(){
        workingStack = new Stack<>();
        finished = new HashSet<>();
        // {3,1,2}
    }
    /*
Push to stack the starting node of our graph V
While stack is not empty: // there are nodes to handle V
    removed = pop operation V
    insert to finish set V
    invoke getReachableNodes on the removed node V
    For each reachable node:
        if the current reachable node is not in finished set && working stack
        push to stack
 */
    public Collection<T> traverse(Traversable<T> partOfGraph,boolean includeDiagonal){
        workingStack.push(partOfGraph.getOrigin());
        while(!workingStack.isEmpty()){
            Node<T> poppedNode = workingStack.pop();
            finished.add(poppedNode);
            Collection<Node<T>> reachableNodes = partOfGraph.getReachableNodes(poppedNode,includeDiagonal);
            for (Node<T> singleReachableNode: reachableNodes){
                if (!finished.contains(singleReachableNode) &&
                        !workingStack.contains(singleReachableNode)){
                    workingStack.push(singleReachableNode);
                }
            }
        }
        List<T> blackList = new ArrayList<>();
        for (Node<T> node: finished){
            blackList.add(node.getData());
        }
        return blackList;
    }
   /*
   [1,0,1,1]
   [1,1,0,1]

   Reachables

   WorkingStack

   FinishedSet
   (0,0)
   (1,0)
   (1,1)
    */



}