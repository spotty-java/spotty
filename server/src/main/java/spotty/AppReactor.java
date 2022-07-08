package spotty;

import spotty.server.Spotty;

public class AppReactor {

    public static void main(String[] args) {
        final var spotty = new Spotty();
        spotty.start();
    }

}
