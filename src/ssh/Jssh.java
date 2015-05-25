package ssh;

import com.jcraft.jsch.*;

import java.io.*;

public class Jssh {

	public void doCommand(final String userName, final String ip, final int port,
			final String password, final String command) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					JSch jsch = new JSch();
					Session session = jsch.getSession(userName, ip, port);
					session.setPassword(password);
					session.setConfig("StrictHostKeyChecking", "no");
					session.setX11Host(ip);
					session.setX11Port(6000);
					session.connect();

					ChannelExec channel = (ChannelExec) session.openChannel("exec");
					channel.setCommand(command);
					channel.setErrStream(System.err);
					channel.connect();
					InputStream in = channel.getInputStream();

					byte[] tmp = new byte[1024];
					while (true) {
						while (in.available() > 0) {
							int i = in.read(tmp, 0, 1024);
							if (i < 0)
								break;
							System.out.print(new String(tmp, 0, i));
						}
						if (channel.isClosed()) {
							if (in.available() > 0)
								continue;
							System.out.println("exit-status: "
									+ channel.getExitStatus());
							break;
						}
					}
					System.out.println("********************");
					channel.disconnect();
				} catch (IOException | JSchException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}