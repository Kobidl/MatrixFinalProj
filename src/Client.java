import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("127.0.0.1", 8010);
        System.out.println("client: Created Socket");

        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        ObjectOutputStream toServer = new ObjectOutputStream(outputStream);
        ObjectInputStream fromServer = new ObjectInputStream(inputStream);

        // sending #1 matrix
        int[][] source = {
                {1, 1, 0, 0},
                {1, 1, 1, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        //send "matrix" command then write 2d array to socket
        toServer.writeObject("matrix");
        toServer.writeObject(source);

//        toServer.writeObject("getAllLinkedPoints");
//        List<HashSet<Index>> linkedPoints =
//                new ArrayList<HashSet<Index>>((List<HashSet<Index>>) fromServer.readObject());
//
//        //todo: change prints
//        for (HashSet<Index> set:linkedPoints){
//            for (Index index : set){
//                System.out.print(index.toString());
//            }
//            System.out.print(",");
//        }


        toServer.writeObject("getShortestPath");
        toServer.writeObject(new Index(0, 0));
        toServer.writeObject(new Index(1, 2));
//        List<Index> reachables =
//                new ArrayList<Index>((List<Index>) fromServer.readObject());
        Collection<List<Index>> paths = (Collection<List<Index>>) fromServer.readObject();
        for(List<Index> path : paths){
            System.out.println("from client - Shortest path are:  " + path);
        }


        // get neighboring indices as list
//        List<Index> AdjacentIndices =
//                new ArrayList<Index>((List<Index>) fromServer.readObject());
//        System.out.println("from client - Neighboring Indices are: "+ AdjacentIndices);

        //send "reachables" command then write an index to socket
//        toServer.writeObject("reachables");
//        toServer.writeObject(new Index(1,1));

        // get reachable indices as list
//        List<Index> reachables =
//                new ArrayList<Index>((List<Index>) fromServer.readObject());
//        System.out.println("from client - Reachable Indices are:  "+ reachables);

        toServer.writeObject("stop");

        System.out.println("client: Close all streams");
        fromServer.close();
        toServer.close();
        socket.close();
        System.out.println("client: Closed operational socket");

    }
}