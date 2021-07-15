import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * This class enables the conversion of both Runnable and Callable tasks
 * to a PriorityRunnable object.
 * A PriorityRunnable tasks wraps a Runnable and assigns it with a priority.
 * The various apply methods convert Runnable or Callable tasks and
 * submit them (using the offer method) to a PriorityBlockingQueue
 * An instance of the EnhancedGenerics class creates a separate thread and while it is not stopped
 * tries to take a task from the queue and run it.
 * Complete the methods marked with TODO. Use main method to test your code
 * @param <T> A Runnable tasks or an instance of a type that implements the Runnable interface
 */

public class EnhancedGenerics<T extends Runnable> {
    protected boolean stop = false;
    protected boolean stopNow = false;
    protected final BlockingQueue<T> taskQueue;
    protected final Thread consumerThread;
    protected final Function<Runnable,T> defaultFunction;


    private final ReentrantReadWriteLock readWriteLock=new ReentrantReadWriteLock();

    public EnhancedGenerics(Function<Runnable,T> runnableTFunction){
        this(new LinkedBlockingQueue<>(10), runnableTFunction);
    }

    public EnhancedGenerics(BlockingQueue<T> paramBlockingQueue,
                            Function<Runnable,T> runnableTFunction) {

        throwIfNull(paramBlockingQueue, runnableTFunction);

        this.taskQueue = paramBlockingQueue;
        this.defaultFunction = runnableTFunction;


        this.consumerThread = new Thread(
                () -> {
                    while ((!stop || !this.taskQueue.isEmpty()) &&
                            (!stopNow)) {
                        int a = 1;
                        try {
                            taskQueue.take().run();
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }
                });

        this.consumerThread.start();
    }

    /**
     * @param objects pass unknown number of arguments passed in run-time
     * @throws NullPointerException
     */
    public static void throwIfNull(Object... objects) throws NullPointerException {
        for (Object argument: objects){
            if(argument == null){
                throw new NullPointerException("one of the arguments is null");
            }
        }
    }

    /**
     * Add runnable to queue based on default runnableTFunction
     * Use apply(final Runnable runnable,Function<Runnable,T> runnableTFunction)
     * @param runnable
     * @throws InterruptedException
     */
    public void apply(final Runnable runnable) throws InterruptedException{
        this.apply(runnable,defaultFunction);
    }


    /**
     * Add Callable to queue based on default runnableTFunction
     * Use apply(final Callable<V> callable,Function<Runnable,T> runnableTFunction)
     * @param callable
     * @param <V>
     * @return Future <V>
     * @throws InterruptedException
     */
    public<V> Future<V> apply(final Callable<V> callable) throws InterruptedException{
        return this.apply(callable,defaultFunction);
    }


    /**
     * Add runnable to the queue based on runnableTFunction
     * @param runnable
     * @param runnableTFunction
     * @throws InterruptedException
     */
    public void apply(final Runnable runnable,Function<Runnable,T> runnableTFunction) throws InterruptedException {
        //set default function if didn't get any
        if(runnableTFunction==null){
            runnableTFunction = this.defaultFunction;
        }

        readWriteLock.readLock().lock();
        try {
            if (!stop && !stopNow)
                taskQueue.offer(runnableTFunction.apply(runnable));
        }catch (Exception e){
            throw e;
        }finally {
            readWriteLock.readLock().unlock();
        }
    }


    /**
     * Gets Callable,converts to FutureTask.
     * Add as a runnable to the queue and returns Future
     * @param callable
     * @param runnableTFunction
     * @param <V>
     * @return Future<V>
     * @throws InterruptedException
     */
    public<V> Future<V> apply(final Callable<V> callable,Function<Runnable,T> runnableTFunction) throws InterruptedException {
        FutureTask<V> futureTask = new FutureTask<>(callable);
        this.apply(futureTask, runnableTFunction);
        return futureTask;
    }


    /**
     * Empty the queue and return the tasks remaining
     * @return List <Runnable>
     * @throws InterruptedException
     */
    public List<Runnable> drain() throws InterruptedException {
        //Init new queue
        List<Runnable> queueList = new ArrayList<>();

        //Lock so only one will empty the queue
        readWriteLock.writeLock().lock();
        try {
            while (!taskQueue.isEmpty()) { //pull from the queue and add to list
                queueList.add(taskQueue.take());
            }
        }catch (Exception e){
            throw e;
        }finally {
            readWriteLock.writeLock().unlock();
        }

        return  queueList;
    }


    /**
     * Stop the queue based on wait param.
     * if wait is true, mark as stop and call waitUntilDone
     * if wait is false, mark as stopNow and interrupt the thread
     * @param wait
     * @throws InterruptedException
     */
    public void stop(boolean wait) throws InterruptedException{
        if(wait) { //mark as stop, and wait until all the tasks in queue finishes.
            if(!stop) {
                //Lock and check if stop didn't changed while waiting
                readWriteLock.writeLock().lock();
                try {
                    if (!stop) {
                        stop = true;
                        this.waitUntilDone();
                        this.consumerThread.interrupt();
                    }
                } catch (Exception e) {
                    throw e;
                } finally {
                    readWriteLock.writeLock().unlock();
                }
            }
        }
        else { //mark as stopNow and interrupt the thread (don't wait for all the tasks to finish).
            if (!stopNow) {
                //Lock and check if stop didn't changed while waiting
                readWriteLock.writeLock().lock();
                try {
                    if (!stopNow) {
                        stopNow = true;
                        this.consumerThread.interrupt();
                    }
                } catch (Exception e) {
                    throw e;
                } finally {
                    readWriteLock.writeLock().unlock();
                }

            }
        }

        //this.drain(); TO DO: to ask if needed.
    }


    /**
     * Check if thread is still alive
     * Wait until thread finish
     * @throws InterruptedException
     */
    public void waitUntilDone() throws InterruptedException {
        if(this.consumerThread.isAlive() && !taskQueue.isEmpty()) {
            //if not empty and still alive wait to finish
            this.consumerThread.join();
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        EnhancedGenerics<PriorityRunnable> service =
                new EnhancedGenerics<>(new PriorityBlockingQueue<>(),
                        aRunnableTask -> new PriorityRunnable(aRunnableTask, 1));

        /*
         submit Runnable tasks to to the queue (as PriorityRunnable objects) using
         the apply methods above
         */
        service.apply(() -> System.out.println(
                "There are more than 2 design patterns in this class"),
                runnable -> new PriorityRunnable(runnable,1));

        service.apply(() -> System.out.println("a runnable"));

        service.apply(new Runnable() {
            @Override
            public void run() {
                System.out.println("Fun");
            }
        }, runnable -> new PriorityRunnable(runnable,5));

        Callable<String> stringCallable= () -> {
            try {
                Thread.sleep(5000); // wait until interrupt
            } catch (InterruptedException e) {
                System.out.println("interrupted");
            }
            return "callable string";
        };
        Future<String> futureString = service.apply(stringCallable);
        System.out.println("hello world!");
        Future<String> anotherFutureString = service.apply(stringCallable);


        try {
            System.out.println(futureString.get());
            System.out.println(anotherFutureString.get(10000, TimeUnit.MILLISECONDS));
        }catch (TimeoutException ex){

        }

        service.stop(true);
        System.out.println("done");


    }
}
