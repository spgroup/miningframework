public class Victim implements Test8003720.CallMe {

    public void callme() {
        Test8003720.println("executing in loader=" + Victim.class.getClassLoader());
        long now = System.currentTimeMillis();
        while ((System.currentTimeMillis() - now) < Test8003720.DURATION) {
            long count = VictimClassLoader.counter++;
            if (count % 1000000 == 0)
                System.gc();
            if (count % 16180000 == 0)
                blurb();
            new Object[1].clone();
        }
    }

    static void blurb() {
        Test8003720.println("count=" + VictimClassLoader.counter);
    }

    static {
        blather();
    }

    static void blather() {
        new java.util.ArrayList<Object>(1000000);
        Class<Victim> c = Victim.class;
        Test8003720.println("initializing " + c + "#" + System.identityHashCode(c) + " in " + c.getClassLoader());
    }
}