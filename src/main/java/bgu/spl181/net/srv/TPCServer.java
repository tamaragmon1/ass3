package bgu.spl181.net.srv;

import bgu.spl181.net.api.MessageEncoderDecoder;
import bgu.spl181.net.api.MessagingProtocol;
import bgu.spl181.net.api.bidi.BidiMessagingProtocol;
import bgu.spl181.net.api.bidi.ConnectionsImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.function.Supplier;

public abstract class TPCServer<T> implements Server<T> {

    private final int port;
    private final Supplier<BidiMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;
    private ConnectionsImpl<String> connections;
    private Integer IDCounter=1;

    public TPCServer(
            int port,
            Supplier<BidiMessagingProtocol<T>> BidiprotocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {

        this.port = port;
        this.protocolFactory = BidiprotocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
		this.connections=new ConnectionsImpl<String>();
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();

                BidiMessagingProtocol thisProtocol=protocolFactory.get();

                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<T>(
                        clientSock,
                        encdecFactory.get(),
                        thisProtocol);

                connections.addConnection(IDCounter,handler);
                thisProtocol.start(IDCounter,connections);
                IDCounter++;
                execute(handler);

            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);

}
