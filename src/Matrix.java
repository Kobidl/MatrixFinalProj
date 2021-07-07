import com.sun.istack.internal.NotNull;

import java.util.*;

public class Matrix {
    /**
     * Neighboring Indices are up,down, left,right
     *   1 0 0
     *   0 0 1
     *   0 0 0
     *   1 1 1
     *
     * [[(0,0),
     * [(1,1) ,(1,2)],
     * [(3,0),(3,1),(3,2)]]
     *
     *
     * 1 0 0
     * 0 1 1
     * 0 1 0
     * 0 1 1
     *
     *
     */

    int[][] primitiveMatrix;

    public Matrix(int[][] oArray){
        List<int[]> list = new ArrayList<>();
        for (int[] row : oArray) {
            int[] clone = row.clone();
            list.add(clone);
        }
        primitiveMatrix = list.toArray(new int[0][]);
    }

    public Matrix() {
        Random r = new Random();
        primitiveMatrix = new int[5][5];
        for (int i = 0; i < primitiveMatrix.length; i++) {
            for (int j = 0; j < primitiveMatrix[0].length; j++) {
                primitiveMatrix[i][j] = r.nextInt(2);
            }
        }
        for (int[] row : primitiveMatrix) {
            String s = Arrays.toString(row);
            System.out.println(s);
        }
        System.out.println("\n");
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int[] row : primitiveMatrix) {
            stringBuilder.append(Arrays.toString(row));
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }



    @NotNull
    public Collection<Index> getNeighbors(@NotNull final Index index,boolean includeDiagonal ) {
        Collection<Index> list = new ArrayList<>();
        int extracted = -1;
        try {
            extracted = primitiveMatrix[index.row + 1][index.column];
            list.add(new Index(index.row + 1, index.column));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        try {
            extracted = primitiveMatrix[index.row][index.column + 1];
            list.add(new Index(index.row, index.column + 1));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        try {
            extracted = primitiveMatrix[index.row - 1][index.column];
            list.add(new Index(index.row - 1, index.column));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        try {
            extracted = primitiveMatrix[index.row][index.column - 1];
            list.add(new Index(index.row, index.column - 1));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        if (includeDiagonal) {
            try {
                extracted = primitiveMatrix[index.row - 1][index.column - 1];
                list.add(new Index(index.row - 1, index.column - 1));
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
            try {
                extracted = primitiveMatrix[index.row - 1][index.column + 1];
                list.add(new Index(index.row - 1, index.column + 1));
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
            try {
                extracted = primitiveMatrix[index.row + 1][index.column - 1];
                list.add(new Index(index.row + 1, index.column - 1));
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
            try {
                extracted = primitiveMatrix[index.row + 1][index.column + 1];
                list.add(new Index(index.row + 1, index.column + 1));
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }
        return list;
    }

    public int getValue(@NotNull final Index index){
        return primitiveMatrix[index.row][index.column];
    }

    public void printMatrix(){
        for (int[] row : primitiveMatrix) {
            String s = Arrays.toString(row);
            System.out.println(s);
        }
    }

    public List<Index> getAllActivePoints(){
        List <Index> activeList = new ArrayList<>();
        for (int i=0; i<primitiveMatrix.length;i++){
            for (int j=0; j<primitiveMatrix[i].length;j++){
                if(primitiveMatrix[i][j] == 1){
                    activeList.add(new Index(i,j));
                }
            }
        }

        return  activeList;
    }

    public final int[][] getPrimitiveMatrix() {
        return primitiveMatrix;
    }


}