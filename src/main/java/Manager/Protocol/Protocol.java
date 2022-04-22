package Manager.Protocol;

public abstract class Protocol<T> {
    public abstract Runnable process(T req) throws RequestUnknownException;
    public abstract boolean shouldTerminate();
}
