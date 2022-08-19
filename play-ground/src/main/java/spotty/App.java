package spotty;

public class App {
    public static void main(String[] args) throws Exception {
        final Spotty spotty = new Spotty();
        spotty.start();

        spotty.get("/", (request, response) -> {
            response.contentType(request.contentType());
            return request.body();
        });
        spotty.post("/echo", (request, response) -> {
            response.contentType(request.contentType());
            return request.body();
        });

        spotty.get("/request", (request, response) -> request);
        spotty.post("/request", (request, response) -> request);
    }
}
