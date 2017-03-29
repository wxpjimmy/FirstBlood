package com.jimmy.quasar;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableRunnable;

import java.util.concurrent.ExecutionException;

/**
 * Created by wxp04 on 2017/3/29.
 */
public class QuasarTest {
    static void m1() throws SuspendExecution, InterruptedException {
        String m = "m1";
        System.out.println("m1 begin");
        m = m2();
        m = m3();
        System.out.println("m1 end");
        System.out.println(m);
    }
    static String m2() throws SuspendExecution, InterruptedException {
        return "m2";
    }
    static String m3() throws SuspendExecution, InterruptedException {
        return "m3";
    }
    static public void main(String[] args) throws ExecutionException, InterruptedException {
        new Fiber<Void>("Caller", new SuspendableRunnable() {
            @Override
            public void run() throws SuspendExecution, InterruptedException {
                m1();
            }
        }).start();
    }
}
