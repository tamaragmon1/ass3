package bgu.spl181.net.srv;

import bgu.spl181.net.api.bidi.MovieRentalService;
import bgu.spl181.net.api.bidi.ObjectEncoderDecoder;

public class BlockBusterMain {

    public static void main(String[] args) {
        SharedData feed = new SharedData("Database/Movies.json","Database/"); //one shared object

// you can use any server...
        Server.threadPerClient(
                7777, //port
                () -> new MovieRentalService(feed), //protocol factory
                ObjectEncoderDecoder::new //message encoder decoder factory
        ).serve();

/*        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                7777, //port
                () ->  new MovieRentalService<>(feed), //protocol factory
                ObjectEncoderDecoder::new //message encoder decoder factory
        ).serve();
*/
    }
}