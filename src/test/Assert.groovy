package test

class Assert {
    static public void assertEquals(actual, expected) {
        try {
            assert expected == actual
        } catch (AssertionError e) {
            e.printStackTrace()
            System.exit(1)
        }
    }
}