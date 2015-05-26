import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterServerInterface extends Remote {
	final static int ACK = 100;
	final static int ACK_RSND = 101;
	/**
	 * Invalid transaction ID. Sent by the server if the client had sent a
	 * message that included an invalid transaction ID, i.e., a transaction ID
	 * that the server does not remember.
	 */
	final static int INVALID_TRANSACTION_ID = 201;
	/**
	 * Invalid operation. Sent by the server if the client attempts to execute
	 * an invalid operation - i.e., write as part of a transaction that had been
	 * committed
	 * 
	 */
	final static int INVALID_OPERATION = 202;
	/**
	 * Wrong message format. Sent by the server if the message sent by the
	 * client does not follow the specified message format.
	 * 
	 */
	final static int WORNG_MSG_FORMAT = 204;

	/**
	 * Read file from server
	 * 
	 * @param fileName
	 * @return File primary replica location
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws RemoteException
	 */
	public String read(String fileName) throws FileNotFoundException,
			IOException, RemoteException;

	/**
	 * Start a new transaction
	 * 
	 * @param fileName
	 * @return comma separated message the new transaction ID, time stamp,
	 *         primary replica loc
	 * @throws RemoteException
	 * @throws IOException
	 */
	public String newTxn(String fileName) throws RemoteException, IOException;

}
