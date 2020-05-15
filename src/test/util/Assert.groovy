package util

class Assert {
    static public void assertEquals(actual, expected) {
        try {
            assert expected == actual
        } catch (AssertionError e) {
            e.printStackTrace()
            System.exit(1)
        }
    }


    static public void assertEquals(actual, expected, message) {
        try {
            assert expected == actual
        } catch (AssertionError e) {
            println message
            e.printStackTrace()
            System.exit(1)
        }
    }
}