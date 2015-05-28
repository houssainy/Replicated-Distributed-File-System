package main_package;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import rmi_interface_package.MasterServerInterface;

public class DfsMaster extends UnicastRemoteObject implements
		MasterServerInterface, PrimaryToMasterInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private HashMap<String, String> tempFiles;
	private HashMap<String, String> metaDataHash;
	private HashMap<Long, String> transactions;
	private String[] ips;
	private HashMap<String, MasterToPrimaryInterface> replicaServers;
	private long id = 0;
	private Log log;

	public DfsMaster() throws IOException {
		super();

		BufferedReader metaData = new BufferedReader(new FileReader("conf/MetaData"));
		tempFiles = new HashMap<>();
		metaDataHash = new HashMap<>();
		transactions = new HashMap<>();
		replicaServers = new HashMap<>();

		log = Log.createInstance();

		String line;
		while ((line = metaData.readLine()) != null) {
			String[] splittedLine = line.split(":");
			metaDataHash.put(splittedLine[0], splittedLine[1]);
		}
		metaData.close();

		System.out.println("Reading Replica Servers ips.");
		BufferedReader br = new BufferedReader(new FileReader(
				"conf/replicaServers"));
		int noOfIps = Integer.parseInt(br.readLine());
		ips = new String[noOfIps];
		for (int i = 0; i < noOfIps; i++)
			ips[i] = br.readLine();
		br.close();
		System.out.println("Master Machine Started and Working.");
		log.write("Master Machine Started and Working.");
	}

	@Override
	public void initiateReplicaServerObject(String replicaIp)
			throws RemoteException, NotBoundException {
		System.out.println("Initiating Replica Server RMI interfaces with ip "
				+ replicaIp);
		log.write("Initiating Replica Server RMI interfaces with ip "
				+ replicaIp);

		System.setProperty("java.rmi.server.hostname", replicaIp);
		Registry registry = LocateRegistry.getRegistry(replicaIp,
				Constants.RMI_REGISTRY_PORT);
		MasterToPrimaryInterface replicaServer = (MasterToPrimaryInterface) registry
				.lookup(Constants.RMI_REPLICA_NAME);
		
		replicaServers.put(replicaIp, replicaServer);
		
		for (String ip : replicaServers.keySet())
			replicaServers.get(ip).newReplicaServer(replicaIp);
		
		System.out.println("Connected to RelicaServr using RMI interface.");
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

			String primaryServerIp = metaDataHash.get(fileName);
			replicaServers.get(primaryServerIp).newTransaction(id, fileName);
			return (id++) + "," + metaDataHash.get(fileName);
		} else {
			Random rand = new Random();

			String primReplica = "";
			if (ips.length <= 1) {
				primReplica = ips[0];
			} else {
				primReplica = ips[rand.nextInt(ips.length - 1)];
			}
			tempFiles.put(fileName, primReplica);
			transactions.put(id, fileName);
			log.write("File not found. Creating new file. Created new Transaction with id: "
					+ id + "for file: " + fileName);

			String primaryServerIp = tempFiles.get(fileName);
			replicaServers.get(primaryServerIp).newTransaction(id, fileName);
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
	public void commitTansaction(long txnID) throws IOException {
		String fileName = transactions.get(txnID);
		transactions.remove(txnID);

		if (tempFiles.containsKey(fileName)) {
			metaDataHash.put(fileName, tempFiles.get(fileName));
			
			PrintWriter out = new PrintWriter(new FileWriter("conf/MetaData", true));
			out.append(fileName + ":" + tempFiles.get(fileName));
			out.close();
			
			tempFiles.remove(fileName);
			log.write("Transaction commited. New file " + fileName + " saved.");
		}
	}

	public static void main(String[] args) throws IOException {
		Scanner fileReader = new Scanner(new File("conf/master_ip"));
		String masterIp = fileReader.next();
		fileReader.close();

		System.out.println("Master Ip = " + masterIp);

		// RMI conf
		System.setProperty("java.rmi.server.hostname", masterIp);
		DfsMaster masterServr = new DfsMaster();
		LocateRegistry.createRegistry(Constants.RMI_REGISTRY_PORT).rebind(
				Constants.RMI_MASTER_NAME, masterServr);

		System.out.println("DfsMaster Registred to Registry Server...");
	}

}
