package Protocols;

/**
 * @author Robin Duda
 */
public interface Request {

    void error();

    void unauthorized();

    void write(Object object);

    void accept();

    void missing();

    void conflict();
}
