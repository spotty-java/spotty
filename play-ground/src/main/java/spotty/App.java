package spotty;

import spotty.server.Spotty;

public class App {
    public static void main(String[] args) throws Exception {
        final Spotty spotty = new Spotty();
        spotty.start();

        spotty.post("/", (request, response) -> {
            response.contentType(request.contentType());
            return request.body();
        });
    }
}
