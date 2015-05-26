package main_package;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ReplicaToReplicaInterface extends Remote {

	public void propagateData(String fileName, String data) throws IOException, RemoteException;
}
