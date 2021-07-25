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

//        // sending #1 matrix
//        int[][] source = {
//                {1, 0, 0, 1},
//                {1, 0,0, 1},
//                {1, 0, 0, 1},
//                {1, 1, 0, 1}
//        };
//        //send "matrix" command then write 2d array to socket
//        toServer.writeObject("matrix");
//        toServer.writeObject(source);
//
//        //Task 1
//        toServer.writeObject("getAllLinkedPoints");
//        List<HashSet<Index>> linkedPoints =
//                new ArrayList<>((List<HashSet<Index>>) fromServer.readObject());
//
//        if(linkedPoints.size() == 0){
//            System.out.println("There are no available linked points");
//        }else {
//            linkedPoints.forEach(System.out::println);
//        }
//
//        //Task 2
//        toServer.writeObject("start index");
//        toServer.writeObject(new Index(1, 0));
//        toServer.writeObject("end index");
//        toServer.writeObject(new Index(1, 2));
//        toServer.writeObject("getShortestPath");
//        Collection<List<Index>> shortestPaths = (Collection<List<Index>>) fromServer.readObject();
//        if(shortestPaths.size() == 0){
//            System.out.println("There are no available allPaths between 2 indexes");
//        }else {
//            System.out.println("from client - Shortest allPaths are:  ");
//            shortestPaths.forEach(System.out::println);
//        }
//
//        //Task 3
//        toServer.writeObject("getNumOfValidSubmarines");
//        int numOfValid = (int) fromServer.readObject();
//        System.out.println("Num of valid submarines: " + numOfValid);
//

        //Task 4
//        int[][] source4 = {
//                {100, 300, 100, 300},
//                {300, 900, 500, 300},
//                {300, 900, 500, 300},
//                {300, 900, 500, 300},
//                {300, 900, 500, 300},
//                {300, 900, 500, 300},
//                {300, 900, 500, 300},
//                {300, 900, 500, 300},
//                {300, 900, 500, 300},
//                {300, 900, 500, 300},
//        };
                int[][] source4 = {
                        {100, 300, 100, 100, 500, 100, 100, 500, 100, 100, 500, 100},
                        {300, 900, 500, 100, 500, 100, 100, 500, 100, 100, 500, 100},
                        {300, 900, 500, 100, 500, 100, 100, 500, 100, 100, 500, 100},
                        {300, 900, 500, 100, 500, 100, 100, 500, 100, 100, 500, 100},
                        {300, 900, 500, 100, 500, 100, 100, 500, 100, 100, 500, 100},
                        {300, 900, 500, 100, 500, 100, 100, 500, 100, 100, 500, 100},
                        {300, 900, 500, 100, 500, 100, 100, 500, 100, 100, 500, 100},
                        {300, 900, 500, 100, 500, 100, 100, 500, 100, 100, 500, 100},
                        {300, 900, 500, 100, 500, 100, 100, 500, 100, 100, 500, 100},
                        {300, 900, 500, 100, 500, 100, 100, 500, 100, 100, 500, 100}
                };
        toServer.writeObject("matrix");
        toServer.writeObject(source4);

        toServer.writeObject("start index");
        toServer.writeObject(new Index(1, 0));
        toServer.writeObject("end index");
        toServer.writeObject(new Index(1, 2));

        toServer.writeObject("getLightestPath");
        Collection<List<Index>> lightestPaths = (Collection<List<Index>>) fromServer.readObject();
        if(lightestPaths.size() == 0) {
            System.out.println("There are no available allPaths between 2 indexes");
        }else{
            System.out.println("from client - Lightest allPaths are: ");
            lightestPaths.forEach(System.out::println);
        }

        toServer.writeObject("stop");

        System.out.println("client: Close all streams");
        fromServer.close();
        toServer.close();
        socket.close();
        System.out.println("client: Closed operational socket");

    }
}