/**
 * Created by kyle on 3/20/2017.
 */
public class ClientServe {
    String message;
    Timestamp ts;
    ClientServe(String message, Timestamp ts){
        this.ts = ts;
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }

    public Timestamp getTimestamp(){
        return this.ts;
    }

}
