package spotty.common.filter;

import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;

@FunctionalInterface
public interface Filter {
    void handle(SpottyRequest request, SpottyResponse response) throws Exception;
}
