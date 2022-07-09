package spotty.common.request;

import org.apache.http.entity.ContentType;
import spotty.common.http.Headers;
import spotty.common.response.SpottyResponse;

import static java.lang.Integer.parseInt;
import static spotty.common.http.Headers.ACCEPT;
import static spotty.common.http.Headers.ACCEPT_ENCODING;
import static spotty.common.http.Headers.CONNECTION;
import static spotty.common.http.Headers.CONTENT_LENGTH;
import static spotty.common.http.Headers.CONTENT_TYPE;
import static spotty.common.http.Headers.HOST;
import static spotty.common.http.Headers.USER_AGENT;
import static spotty.common.http.HttpMethod.POST;

public interface WebRequestTestData {
    Headers headers = new Headers()
        .add(CONTENT_TYPE, "text/plain")
        .add(USER_AGENT, "Spotty Agent")
        .add(ACCEPT, "*/*")
        .add(HOST, "localhost:4000")
        .add(ACCEPT_ENCODING, "gzip, deflate, br")
        .add(CONNECTION, "keep-alive")
        .add(CONTENT_LENGTH, "2824");

    String requestHeaders = "POST / HTTP/1.1\n" + headers;

    String requestBody = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris dapibus tortor aliquam metus viverra, id iaculis libero aliquet. Vivamus tempor sapien eu metus sollicitudin laoreet. Aliquam erat volutpat. Quisque nulla augue, posuere et condimentum id, consectetur eu felis. Aenean congue nibh orci, vel lacinia diam commodo eu. Nulla sem eros, venenatis at malesuada iaculis, dapibus quis ante. In iaculis sem quis eros interdum facilisis.
            Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Sed faucibus tortor non erat pellentesque scelerisque. Phasellus nunc dolor, aliquam in urna accumsan, faucibus tristique ligula. Nunc maximus augue in lacus mollis lobortis sit amet sit amet ipsum. Proin accumsan enim sit amet lectus varius egestas. Quisque dignissim, augue non sollicitudin scelerisque, nunc turpis sollicitudin lorem, non mollis erat ex rhoncus nisi. Integer rhoncus facilisis massa vitae aliquam. Fusce neque nulla, tristique quis lacinia quis, rutrum eget elit. Mauris blandit sapien et porttitor lobortis.
            Proin et augue et eros varius pulvinar. Nullam ut velit ut ipsum bibendum sagittis vitae ut tellus. Maecenas in mattis neque. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Phasellus sapien risus, molestie eget augue non, fermentum porta nisi. Curabitur lacus ligula, tempor non lobortis id, facilisis id nisi. Nam consequat scelerisque nulla quis tempor. Sed auctor massa eget lectus fringilla, non sollicitudin quam aliquam. Suspendisse et rhoncus ante. Donec ornare massa nec risus varius, in dapibus velit tempus. Nam sollicitudin orci viverra leo blandit varius. Quisque nec dolor gravida, dictum metus non, fermentum turpis.
            Maecenas nec elementum risus, eu dignissim quam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Integer nec semper quam. Curabitur leo augue, tincidunt id turpis in, pharetra blandit ante. Pellentesque tristique ut ante ut volutpat. Etiam sodales id eros at rhoncus. Ut efficitur turpis nec mauris facilisis, sed laoreet dui elementum. Donec sit amet varius eros. Sed sit amet ligula tincidunt, pellentesque diam ac, accumsan neque. Cras et nunc urna. Suspendisse potenti. Vivamus et leo non massa convallis dictum. Aliquam et suscipit sapien, vel faucibus ex. Donec eu purus cursus, rhoncus massa at, consequat arcu.
            Curabitur egestas dui ac commodo pellentesque. Integer nec condimentum eros. Proin bibendum lorem non maximus fermentum. Mauris vel dapibus dolor, ac pellentesque augue. Suspendisse porta, lacus vitae finibus egestas, metus velit aliquam tortor, quis hendrerit nibh massa eu nibh. Donec vehicula placerat cursus. Sed tincidunt ligula id suscipit mattis. Fusce accumsan quis orci vitae molestie. Phasellus a commodo metus.
        """.stripIndent().trim();

    String fullRequest = requestHeaders + "\n\n" + requestBody;

    default SpottyRequest.Builder aSpottyRequest() {
        final var content = requestBody.getBytes();
        final var headers = this.headers.copy();

        return SpottyRequest.builder()
            .protocol("HTTP/1.1")
            .scheme("http")
            .method(POST)
            .path("/")
            .contentLength(parseInt(headers.remove(CONTENT_LENGTH)))
            .contentType(ContentType.parse(headers.remove(CONTENT_TYPE)))
            .headers(headers)
            .body(content);
    }

    default SpottyResponse aSpottyResponse(SpottyRequest request) {
        final var response = new SpottyResponse();
        response.setProtocol(request.protocol);
        response.setContentType(request.contentType.get());
        response.addHeaders(request.headers);
        response.setBody(request.body);

        return response;
    }
}