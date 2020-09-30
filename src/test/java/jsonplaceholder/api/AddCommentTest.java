package jsonplaceholder.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import model.Comment;
import model.Post;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.NoSuchElementException;

import static io.restassured.RestAssured.*;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class AddCommentTest {
    @Test
    @Tags(value =
            {
                    @Tag("api-comment"),
                    @Tag("api"),
                    @Tag("api_0001")
            })
    public void addCommentToHighestUserId() {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";

        ObjectMapper objectMapper = new ObjectMapper();
        RestAssuredConfig config = config().objectMapperConfig(objectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> objectMapper));
        List<Post> postList = get("posts")
                            .then().assertThat().statusCode(200)
                            .extract().body().jsonPath().getList(".", Post.class);

        assertThat(postList != null);

        int highestUserId = postList
                .stream()
                .mapToInt(Post::getUserId)
                .max()
                .orElseThrow(NoSuchElementException::new);

        List<Post> userPostList = given().queryParam("userId",Integer.toString(highestUserId)).get("posts")
                .then().assertThat().statusCode(200)
                .extract().body().jsonPath().getList(".", Post.class);

        assertThat(userPostList != null);

        int highestId = userPostList
                .stream()
                .mapToInt(Post::getId)
                .max()
                .orElseThrow(NoSuchElementException::new);

        Comment comment = new Comment();
        comment.setBody("Lorem Ipsum");
        comment.setEmail("dummy@email.com");
        comment.setPostId(highestId);
        comment.setName("ex et quam laboru");

        Comment responseComment = given().log().all().queryParam("postId", Integer.toString(highestId))
                .header("content-type", "application/json")
                .body(comment).post("comments")
                .then().log().all()
                .assertThat().statusCode(201)
                .extract().body().as(Comment.class);

        assertThat(responseComment != null);

        //Set id to request comment for object comparision
        comment.setId(responseComment.getId());

        assertThat(responseComment)
                .usingRecursiveComparison()
                .isEqualTo(comment);
    }
}
