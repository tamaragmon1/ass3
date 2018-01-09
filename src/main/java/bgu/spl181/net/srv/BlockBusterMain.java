package bgu.spl181.net.srv;

import bgu.spl181.net.api.bidi.MovieRentalService;
import bgu.spl181.net.api.bidi.ObjectEncoderDecoder;

import java.util.ArrayList;

public class BlockBusterMain {

    public static void main(String[] args) {
        MoviesSharedData feed = new MoviesSharedData("Database/Movies.json","Database/Users.json"); //one shared object

        Server.threadPerClient(
                7777, //port // TODO was 7777
                () -> new MovieRentalService(feed), //protocol factory
                ObjectEncoderDecoder::new //message encoder decoder factory
        ).serve();
 /*
       Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                4000, //port
                () ->  new MovieRentalService<>(feed), //protocol factory
                ObjectEncoderDecoder::new //message encoder decoder factory
        ).serve();
*/
    }
}