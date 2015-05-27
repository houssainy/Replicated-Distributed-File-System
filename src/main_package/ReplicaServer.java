package main_package;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import rmi_interface_package.MessageNotFoundException;
import rmi_interface_package.ReplicaServerInterface;

public class ReplicaServer extends UnicastRemoteObject implements
		ReplicaServerInterface, MasterToPrimaryInterface,
		ReplicaToReplicaInterface {

	// private ArrayList<String> listOfReplicas; // TODO

	private static final long serialVersionUID = 1L;

	private String dfsDir = "";
	private HashMap<Long, String> transactionMap = new HashMap<Long, String>(); // Key:TxnID,
																				// value:fileName
	private HashMap<String, Long> mapFileToOwnerTransaction = new HashMap<String, Long>(); // Key:fileName,
																							// Value:txn
	private HashMap<String, StringBuilder> writtenFileData = new HashMap<String, StringBuilder>(); // Key:filename,
																									// value:data
																									// appended
	private HashSet<String> fileUsed = new HashSet<>(); // Key:fileName, if it
														// is used by any write
														// transaction
	private HashMap<String, Lock> fileLock = new HashMap<>(); // Key:fileName,
																// Value:the
																// lock
																// specified for
																// this file
																// which locks
																// transactions.

	private final ReadWriteLock transactionMapLocker = new ReentrantReadWriteLock();
	private final ReadWriteLock mapFileToOwnerTransactionLocker = new ReentrantReadWriteLock();
	private final ReadWriteLock writenFileDataLocker = new ReentrantReadWriteLock();
	private final ReadWriteLock fileUsedLocker = new ReentrantReadWriteLock();
	private final ReadWriteLock fileLockLocker = new ReentrantReadWriteLock();

	private final Lock transactionMap_readLock = transactionMapLocker
			.readLock();
	private final Lock mapFileToOwnerTransaction_readLock = mapFileToOwnerTransactionLocker
			.readLock();
	private final Lock writtenFileData_readLock = writenFileDataLocker
			.readLock();
	private final Lock fileUsed_readLock = fileUsedLocker.readLock();
	private final Lock fileLock_readLock = fileLockLocker.readLock();

	private final Lock transactionMap_writeLock = transactionMapLocker
			.writeLock();
	private final Lock mapFileToOwnerTransaction_writeLock = mapFileToOwnerTransactionLocker
			.writeLock();
	private final Lock writtenFileData_writeLock = writenFileDataLocker
			.writeLock();
	private final Lock fileUsed_writeLock = fileUsedLocker.writeLock();
	private final Lock fileLock_writeLock = fileLockLocker.writeLock();

	protected ReplicaServer() throws RemoteException {
		super();
	}

	@Override
	public void newTransaction(long txnID, String fileName) {
		transactionMap_writeLock.lock();
		transactionMap.put(txnID, fileName);
		transactionMap_writeLock.unlock();
		if (!fileLock.containsKey(fileName)) { // new file to the system
			fileLock.put(fileName, new ReentrantReadWriteLock().writeLock());
		}
	}

	@Override
	public String read(String fileName) throws IOException, RemoteException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(dfsDir + fileName)));

		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		br.close();

		return sb.toString();
	}

	@Override
	public int write(long txnID, long msgSeqNum, String data)
			throws RemoteException, IOException {
		transactionMap_readLock.lock();
		boolean x = transactionMap.containsKey(txnID);
		transactionMap_readLock.unlock();
		if (!x)
			return INVALID_TRANSACTION_ID;

		transactionMap_readLock.lock();
		String fileName = transactionMap.get(txnID);
		transactionMap_readLock.unlock();

		// no transaction is writing now in this file
		// OR
		// if this isn't the allowed transaction (check if this is the txn which
		// writes in the file now)
		fileUsed_readLock.lock();
		x = fileUsed.contains(fileName);
		fileUsed_readLock.unlock();

		mapFileToOwnerTransaction_readLock.lock();
		boolean y = mapFileToOwnerTransaction.get(fileName) != txnID;
		mapFileToOwnerTransaction_readLock.unlock();

		if (!x || y) {
			// get access to this file
			fileLock_readLock.lock();
			Lock lock = fileLock.get(fileName);
			fileLock_readLock.unlock();

			lock.lock();

			fileUsed_writeLock.lock();
			fileUsed.add(fileName);
			fileUsed_writeLock.unlock();

			mapFileToOwnerTransaction_writeLock.lock();
			mapFileToOwnerTransaction.put(fileName, txnID);
			mapFileToOwnerTransaction_writeLock.unlock();

			writtenFileData_writeLock.lock();
			writtenFileData.put(fileName, new StringBuilder());
			writtenFileData_writeLock.unlock();
		}

		writtenFileData_readLock.lock();
		writtenFileData.get(fileName).append(data);
		writtenFileData_readLock.unlock();

		return ACK;
	}

	// what is the benefit from numOfMsgs ?
	// when to throw MessageNotFoundException ?
	@Override
	public int commit(long txnID, long numOfMsgs)
			throws MessageNotFoundException, RemoteException {
		transactionMap_readLock.lock();
		String fileName = transactionMap.get(txnID);
		transactionMap_readLock.unlock();

		writtenFileData_readLock.lock();
		String propagatedData = writtenFileData.get(fileName).toString();
		writtenFileData_readLock.unlock();

		terminateTransaction(txnID);

		// save data at this machine, flush, close
		try {
			PrintWriter pr = new PrintWriter(new File(dfsDir + fileName));
			pr.append(propagatedData);
			pr.flush();
			pr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// TODO call commitTansaction to master
		// TODO send other replicas to commit this transaction.
		// "propagatedData", "fileName"
		return ACK;
	}

	@Override
	public int abort(long txnID) throws RemoteException {
		terminateTransaction(txnID);

		// TODO call abortTansaction to master
		// TODO roll back in this machine
		// TODO send other replicas to roll back this transaction
		return 0;
	}

	public void terminateTransaction(long txnID) {
		transactionMap_readLock.lock();
		String fileName = transactionMap.get(txnID);
		transactionMap_readLock.unlock();

		fileLock.get(fileName).unlock();

		fileLock_readLock.lock();
		boolean x = fileLock.get(fileName).tryLock();
		if (!x)
			fileLock.get(fileName).unlock();
		fileLock_readLock.unlock();

		if (x) { // check if no more transactions are requesting write on this
					// file.
			fileUsed_writeLock.lock();
			fileUsed.remove(fileName);
			fileUsed_writeLock.unlock();

			fileLock_writeLock.lock();
			fileLock.remove(fileName);
			fileLock_writeLock.unlock();
		}

		writtenFileData_writeLock.lock();
		writtenFileData.remove(fileName);
		writtenFileData_writeLock.unlock();

		mapFileToOwnerTransaction_writeLock.lock();
		mapFileToOwnerTransaction.remove(fileName);
		mapFileToOwnerTransaction_writeLock.unlock();

		transactionMap_writeLock.lock();
		transactionMap.remove(txnID);
		transactionMap_writeLock.unlock();
	}

	public void propagateData(String fileName, String data) throws IOException,
			RemoteException {
		PrintWriter out = new PrintWriter(new File(fileName));
		out.append(data);
		out.close();
	}

	public static void main(String[] args) throws AccessException,
			RemoteException, FileNotFoundException {
		Scanner fileReader = new Scanner(new File("conf/master_ip"));
		String masterIp = fileReader.next();
		fileReader.close();

		System.setProperty("java.rmi.server.hostname", masterIp);

		ReplicaServer masterServr = new ReplicaServer();

		LocateRegistry.createRegistry(Constants.RMI_REGISTRY_PORT).rebind(
				Constants.RMI_REPLICA_NAME, masterServr);

		System.out.println("ReplicaServer Registred to Registry Server...");
	}
}
