package cn.hehewocao_Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ListIterator;

import javax.swing.JOptionPane;

import cn.hehewocao_ServerTools.ServerTools;
import cn.hehewocao_User.UserThread;
import cn.hehewocao_Windows.WindowClientChatRoom;
import cn.hehewocao_Windows.WindowServer;

public class ServerReciveThread implements Runnable {

	private Socket socket;

	public ServerReciveThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {

		Socket s = socket;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String ns = ServerAcceptThread.i + "=" + br.readLine();
			ServerAcceptThread.arrayUser.add(ns);
			ServerAcceptThread.i++;
			
			ServerTools.ServerSendMessage(ServerAcceptThread.arraySocket);
			
			// 开启用户连接线程
			UserThread ut = new UserThread(ServerAcceptThread.arraySocket);
			Thread t = new Thread(ut);
			t.start();

			String Messagestr = null;
			while ((Messagestr = br.readLine()) != null) {
				String[] close = Messagestr.split("=");
				//多增加一个判断，防止下标越界异常
				if (close.length!=1 && close[1].equals("Socket is closed!")) {

					ServerAcceptThread.arraySocket.remove(s);
					ListIterator<String> lit = ServerAcceptThread.arrayUser.listIterator();
					while (lit.hasNext()) {
						String[] userinfo = lit.next().split("=");
						if (userinfo[1].equals(close[0])) {
							lit.remove();
						}
					}

					UserThread ut1 = new UserThread(ServerAcceptThread.arraySocket);
					Thread t1 = new Thread(ut1);
					t1.start();
					return;
				}
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
				String time = df.format(new Date());
				String IP = s.getInetAddress().getHostAddress();
				String displayMessagestr = time + "  " + Messagestr;

				Messagestr = time + "=" + IP + "=" + Messagestr;
				writerFile(Messagestr);

				System.out.println("收到客户端数据：" + Messagestr);

				WindowServer.infortextArea.append(displayMessagestr);
				WindowServer.infortextArea.append("\n");
				//设置光标在末尾
				WindowServer.infortextArea.setCaretPosition(WindowServer.infortextArea.getText().length());
				ServerTools.ServerSendMessage(ServerAcceptThread.arraySocket);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "接收数据失败！");
		}
	}

	public boolean writerFile(String str) {

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("MessageRecord.txt", true));
			bw.write(str);
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
