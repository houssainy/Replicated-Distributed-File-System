package main_package;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import ssh.Jssh;

public class Administrator {
	private final static String USER_NAME = "houssainy";

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("Project Path is Missing!");
			return;
		}

		Scanner in = new Scanner(System.in);

		String input = in.nextLine();
		in.close();

		String[] temp = input.split(" ");

		/**
		 * Expected input: server -ip [ip address string] -port [port number]
		 * -dir <directory path>
		 */
		String masterIp = temp[1];
		int port = Integer.parseInt(temp[2]);
		String dir = temp[3];

		// Start Master Server
		String command = "cd " + args[0]
				+ " && javac main_package/RMIMasterServer.java"
				+ " && java main_package.RMIMasterServer " + dir;
		Jssh ssh = new Jssh();
		ssh.doCommand(USER_NAME, masterIp, port, "Mohamed1992", command);

		// Start Replica Servers
		command = "cd " + args[0]
				+ " && javac main_package/RMIReplicaServer.java"
				+ " && java main_package.RMIReplicaServer";

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File("conf/replica_servers_ssh"))));
		String line;
		while ((line = br.readLine()) != null) {
			temp = line.split(" ");
			ssh.doCommand(USER_NAME, temp[0], Integer.parseInt(temp[1]),
					temp[2], command);
		}
		br.close();
	}
}
