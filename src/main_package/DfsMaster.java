package main_package;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Random;

import rmi_interface_package.MasterServerInterface;

public class DfsMaster implements MasterServerInterface,
		PrimaryToMasterInterface {

	private BufferedReader metaData;
	private HashMap<String, String> tempFiles;
	private HashMap<String, String> metaDataHash;
	private HashMap<Long, String> transactions;
	private String[] ips;
	private long id = 0;
	private Log log;

	public DfsMaster() throws IOException {
		metaData = new BufferedReader(new FileReader("conf/MetaData"));
		tempFiles = new HashMap<>();
		metaDataHash = new HashMap<>();
		transactions = new HashMap<>();
		log = Log.createInstance();

		String line;
		while ((line = metaData.readLine()) != null) {
			String[] splittedLine = line.split(":");
			metaDataHash.put(splittedLine[0], splittedLine[1]);
		}

		BufferedReader br = new BufferedReader(new FileReader(
				"conf/replicaServers"));
		int noOfIps = Integer.parseInt(br.readLine());
		ips = new String[noOfIps];
		for (int i = 0; i < noOfIps; i++)
			ips[i] = br.readLine();
		br.close();

		log.write("Master Machine Started and Working.");
	}

	@Override
	public String read(String fileName) throws FileNotFoundException,
			IOException, RemoteException {
		if (metaDataHash.containsKey(fileName)) {
			log.write("Reading file " + fileName);
			return metaDataHash.get(fileName);
		} else {
			log.write("ERROR: File " + fileName + " Not found.");
			throw new FileNotFoundException();
		}
	}

	@Override
	public String newTxn(String fileName) throws RemoteException, IOException {
		if (metaDataHash.containsKey(fileName)) {
			transactions.put(id, fileName);
			log.write("Created new Transaction with id: " + id + "for file: "
					+ fileName);
			return (id++) + "," + metaDataHash.get(fileName);
		} else {
			Random rand = new Random();
			String primReplica = ips[rand.nextInt(ips.length - 1)];
			tempFiles.put(fileName, primReplica);
			transactions.put(id, fileName);
			log.write("File not found. Creating new file. Created new Transaction with id: "
					+ id + "for file: " + fileName);
			return (id++) + "," + tempFiles.get(fileName);
		}
	}

	@Override
	public void abortTransaction(long txnID) {
		String fileName = transactions.get(txnID);
		transactions.remove(txnID);

		if (tempFiles.containsKey(fileName)) {
			tempFiles.remove(fileName);
			log.write("Transaction aborted. Temp file " + fileName);
		} else
			log.write("Transaction aborted.");
	}

	@Override
	public void commitTansaction(long txnID) {
		String fileName = transactions.get(txnID);
		transactions.remove(txnID);

		if (tempFiles.containsKey(fileName)) {
			metaDataHash.put(fileName, tempFiles.get(fileName));
			tempFiles.remove(fileName);
			log.write("Transaction commited. New file " + fileName + " saved.");
		}
	}
}
