package spotty.server

class AppTestContext {

    public static Spotty SPOTTY

    static {
        SPOTTY = new Spotty(5050)
        SPOTTY.start()
        SPOTTY.awaitUntilStart()
    }

    static void clean() {
        SPOTTY.close()
        SPOTTY.awaitUntilStop()
    }

}
