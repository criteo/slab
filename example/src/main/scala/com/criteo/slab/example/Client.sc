import com.criteo.slab.example.HttpClient
import com.twitter.finagle.Http
import com.twitter.finagle.http.RequestBuilder
import com.twitter.util.Await

val service = Http.client.newService(s"localhost:8080")

val req = RequestBuilder()
    .url("http://localhost/echo/123")
    .buildGet()

//Await.ready(service(req)).map(_.contentString)
//
//HttpClient("http://localhost/123")

java.net.URLDecoder.decode("simple%20board", "UTF-8")