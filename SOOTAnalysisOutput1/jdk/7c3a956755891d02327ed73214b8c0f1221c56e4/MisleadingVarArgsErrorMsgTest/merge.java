class MisleadingVarArgsErrorMsgTest {

    class A {

        void f(int... x) {
        }
    }

    class B extends A {

        @Override
        void f(int[] x) {
        }
    }

    {
        new B().f(1);
    }
}
