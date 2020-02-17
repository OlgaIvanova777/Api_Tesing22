package core;

import beans.Boards;
import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static core.TrelloConstants.*;
import static org.hamcrest.Matchers.lessThan;

public class BoardsApi {

    //builder pattern
    private BoardsApi() {
    }

    private static Map<String, String> params = new HashMap<String, String>() {
        {
            put(KEY_PARAM, TRELLO_API_KEY_VALUE);
            put(TOKEN_PARAM, TRELLO_API_TOKEN_VALUE);
        }
    };
    private static String Url = null;

    public static ApiBuilder with() {
        BoardsApi api = new BoardsApi();
        return new ApiBuilder(api);
    }

    public static class ApiBuilder {
        private BoardsApi trelloApi;

        private ApiBuilder(BoardsApi gcApi) {
            trelloApi = gcApi;
        }

        public ApiBuilder url(String baseUrl) {
            Url = baseUrl;
            return this;
        }

        public ApiBuilder name(String name) {
            trelloApi.params.put(NAME_PARAM, name);
            return this;
        }

        public Response callApi(Method method) {
            final RequestSpecification specification;
            switch (method) {
                case POST:
                    specification = basePOSTRequestConfiguration();
                    break;
                default:
                    specification = baseGETRequestConfiguration();

            }
            return RestAssured
                    .given()
                    .spec(specification)
                    .baseUri(Url)
                    .log().all()
                    .when()
                    .request(method)
                    .prettyPeek();
        }
    }

    public static Boards getAnswer (Response response) {
        return new Gson().fromJson(response.asString().trim(), Boards.class);
    }

    //set base request and response specifications to use in tests

    private static RequestSpecification basePOSTRequestConfiguration() {

        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBody(BoardsApi.params)
                .setRelaxedHTTPSValidation()
                .setBaseUri(Url)
                .build();
    }

    private static RequestSpecification baseGETRequestConfiguration() {
        return new RequestSpecBuilder()
                .addQueryParams(BoardsApi.params)
                .setAccept(ContentType.JSON)
                .setRelaxedHTTPSValidation()
                .build();
    }
    public static ResponseSpecification successResponse() {
        return new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .expectHeader(HttpHeaders.CONNECTION, "keep-alive")
                .expectResponseTime(lessThan(20000L))
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification badRequest() {
        return new ResponseSpecBuilder()
                .expectContentType(ContentType.TEXT)
                .expectHeader(HttpHeaders.CONNECTION, "close")
                .expectResponseTime(lessThan(20000L))
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .build();
    }

    public static ResponseSpecification boardNotFound() {
        return new ResponseSpecBuilder()
                .expectContentType(ContentType.TEXT)
                .expectHeader(HttpHeaders.CONNECTION, "keep-alive")
                .expectResponseTime(lessThan(20000L))
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .build();
    }

    public static void removeBoard(Boards board) {
        BoardsApi.with()
                .url(TRELLO_NEW_BOARD_API_URL + board.getId())
                .callApi(Method.DELETE)
                .then()
                .spec(BoardsApi.successResponse());
    }
}
