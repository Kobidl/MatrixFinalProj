import java.util.*;

public class ThreadLocalBfsVisit<T> {
    protected final ThreadLocal<List<T>> shortestPathList = ThreadLocal.withInitial(ArrayList::new);
    protected final ThreadLocal<HashMap<Node<T>, Boolean>> visited = ThreadLocal.withInitial(HashMap::new);
    protected final ThreadLocal<Queue<Node<T>>> queue  =
            ThreadLocal.withInitial(LinkedList::new);
    protected final ThreadLocal<Stack<Node<T>>> pathStack = ThreadLocal.withInitial(Stack::new);

    protected void threadLocalQueueAdd(Node<T> node){
        queue.get().add(node);
    }

    protected void threadLocalStackPush(Node<T> node){
        pathStack.get().push(node);
    }

    private void threadLocalVisitedPut(Node<T> node,boolean flag){
        visited.get().put(node,flag);
    }


    public List<T> traverse(Traversable<T> partOfGraph, Node<T> source, Node<T> dest){
        //Check if same node
        if (!source.getData().equals(dest.getData())) {
            threadLocalQueueAdd(source);
            threadLocalStackPush(source);
            threadLocalVisitedPut(source,true);


            while(!queue.get().isEmpty())
            {
                Node<T> u = queue.get().poll();

                //ArrayList<Integer> adjList = graph.getOutEdges(u);
                Collection<Node<T>> reachableNodes = partOfGraph.getReachableNodes(u,false);

                for(Node<T> v : reachableNodes)
                {
                    if(!visited.get().containsKey(v))
                    {
                        threadLocalQueueAdd(v);
                        threadLocalVisitedPut(v,true);
                        threadLocalStackPush(v);
                        if(u.getData().equals(dest.getData())) {//check if we reached to dest
                            break;
                        }
                    }
                }
            }

            //To find the path
            Node<T> node, currentSrc=dest;
            shortestPathList.get().add(dest.getData());

            while(!pathStack.get().isEmpty())
            {
                node = pathStack.get().pop();
                Collection<Node<T>> reachableNodes = partOfGraph.getReachableNodes(currentSrc,false);

                if(reachableNodes.contains(node))
                {
                    shortestPathList.get().add(node.getData());
                    currentSrc = node;
                    if(node == source)
                        break;
                }
            }

        }
        return shortestPathList.get();
    }
    
}