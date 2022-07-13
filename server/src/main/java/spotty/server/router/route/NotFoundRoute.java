package spotty.server.router.route;

import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;

import static spotty.common.http.HttpStatus.NOT_FOUND;

public final class NotFoundRoute implements Route {
    @Override
    public Object handle(SpottyRequest request, SpottyResponse response) throws Exception {
        response.status(NOT_FOUND);

        return null;
    }
}
