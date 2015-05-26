import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ReplicaServerInterface extends Remote {
	final static int ACK = 100;
	final static int ACK_RSND = 101; //TODO delete
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
	final static int INVALID_OPERATION = 202; //TODO delete
	/**
	 * Wrong message format. Sent by the server if the message sent by the
	 * client does not follow the specified message format.
	 * 
	 */
	final static int WORNG_MSG_FORMAT = 204; //TODO delete

	/**
	* Read file from server
	* 
	* @param fileName
	* @return File data
	* @throws IOException
	* @throws RemoteException
	*/
	public String read(String fileName) throws IOException, RemoteException;

	/**
	 * 
	 * @param txnID
	 *            : the ID of the transaction to which this message relates
	 * @param msgSeqNum
	 *            : the message sequence number. Each transaction starts with
	 *            message sequence number 1.
	 * @param data
	 *            : data to write in the file
	 * @return ACK or ACK_RSND
	 * @throws IOException
	 * @throws RemoteException
	 */
	public int write(long txnID, long msgSeqNum, String data)
			throws RemoteException, IOException;

	/**
	 * 
	 * @param txnID
	 *            : the ID of the transaction to which this message relates
	 * @param numOfMsgs
	 *            : Number of messages sent to the server
	 * @return ACK or through MessageNotFoundException calling for missing data.
	 * @throws MessageNotFoundException
	 * @throws RemoteException
	 */
	public int commit(long txnID, long numOfMsgs)
			throws MessageNotFoundException, RemoteException;

	/**
	 * 
	 * @param txnID
	 *            : the ID of the transaction to which this message relates
	 * @return
	 * @throws RemoteException
	 */
	public int abort(long txnID) throws RemoteException;
}
