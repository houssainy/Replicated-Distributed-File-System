package main_package;

import java.util.Scanner;

public class Client {
	private final static int READ = 1;
	private final static int WRITE = 2;
	private final static int CLOSE = 3;

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		boolean running = true;
		do {
			switch (in.nextInt()) {
			case READ:
				readRemoteFile();
				break;
			case WRITE:
				writDataToRemoteFile();
				break;
			case CLOSE:
				running = false;
				break;
			default:
				System.out.println("Invalid Input.");
			}
		} while (running);
		in.close();
	}

	private static void writDataToRemoteFile() {
		// TODO Auto-generated method stub

	}

	private static void readRemoteFile() {
		// TODO Auto-generated method stub

	}
}
