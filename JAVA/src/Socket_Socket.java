//package test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

//import org.json.JSONException;
//import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import org.json.*;

public class Socket_Socket {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Thread(new ServerOutput()).start();
		new Thread(new ServerInput()).start();
		new Thread(new TestServer()).start();
	}

}

/**
 * 
 * @author Administrator
 * 此类是处理java服务器的输出流的类，需要设备或PHP端建立长连接
 */
class ServerOutput implements Runnable
{

    public static final int PORT = 8000;//监听的端口号
    
    public static HashMap<Integer, Socket> clientList = new HashMap<>();//输出流连接的列表
	
	public void run() {
		// TODO Auto-generated method stub
		ServerSocket serverSocket = null;//定义一个ServerSocket 
		try   
        {    
            serverSocket = new ServerSocket(PORT);//新建一个ServerSocket对象    
            while (true)   
            {   
            	//不断等待新的客户端连接   
                Socket client = serverSocket.accept();
                  
                //客户端连接后，首先读取客户端发来的客户号
                try 
                {
                	 DataInputStream dis = new DataInputStream(client.getInputStream());
                	 DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                	 byte[] buffer = new byte[1024];
                	 dis.read(buffer);
                	 System.out.println(new String(buffer).trim());
                	 JSONObject jsonInfo = new JSONObject(new String(buffer).trim());
                	 Integer clientNum = Integer.parseInt(jsonInfo.getString("id"));
                	 
                	 /* 检测clientList中是否存在此对象，若存在，则替换，否则加入 */
                	 if(ServerOutput.clientList.containsKey(clientNum))
                	 {
                		 ServerOutput.clientList.remove(clientNum);
                		 ServerOutput.clientList.put(clientNum, client);
                	 }
                	 else
                		 ServerOutput.clientList.put(clientNum, client);
                	 
                	 System.out.println("Output: 客户端 " + clientNum.toString() + " 连接成功");
                	 dos.write("ok".getBytes());
                	 
                	 /* 0 是本机PHP程序的客户号，此处进行相应的函数处理 */
                	 if(clientNum == 0)
                	 {
                		 
                	 }
                	 
                	 /* 终端发送的消息，此处进行相应的函数处理 */
                	 else 
                	 {						 
                		if(CommandQueue.processFlag.containsKey(clientNum))
                		{
                			CommandQueue.processFlag.remove(clientNum);               			 
                		}
                		CommandQueue.processFlag.put(clientNum, false);
                		System.out.println("process flag: "+CommandQueue.processFlag.get(clientNum));
                	 }
				} 
                catch (Exception e) 
                {
					// TODO: handle exception
				} 
            }    
        }   
        catch (Exception e)   
        {    
            e.printStackTrace();    
        }   
        finally  
        {  
            try   
            {  
                if(serverSocket != null)  
                {  
                    serverSocket.close();  
                }  
            }   
            catch (Exception e)//原来是IOException  
            {  
                e.printStackTrace();  
            }  
        }
	}	
}

/**
 * 本类处理 java端发送到本地 PHP端的消息，发送格式JSON
 * @param deviceNum: 发送过来的设备编号，将设备编号发往PHP端处理
 * @param command: 发送到PHP端的命令
 * @return
 */
class OutputlocalInfoProcess implements Runnable
{
	public Socket local;
	public Integer deviceNum;
	public String command;
	public String filepath;
	public JSONObject localInfo;
	
	public OutputlocalInfoProcess(Socket local, JSONObject localInfo) {
		// TODO Auto-generated constructor stub
		this.local = local;
		try 
		{
			System.out.println("local:1");
			this.deviceNum = localInfo.getInt("deviceNum");
			this.command = localInfo.getString("command");
			
			this.localInfo = localInfo;
			
//			if(command.equals("0x27"))
//			{
//				filepath = localInfo.getString("filename");
//			}
		} 
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Socket client = ServerOutput.clientList.get(0);
		try 
		{
			DataInputStream dis = new DataInputStream(client.getInputStream());
			DataOutputStream dos = new DataOutputStream(client.getOutputStream());
			
			/**********/
			/* 测试专用 */
//			JSONObject jsonInfo = new JSONObject();
//			jsonInfo.put("deviceNum", deviceNum);
//			jsonInfo.put("command", command);
//			jsonInfo.put("filename", filepath);
			/**********/
			
			dos.write(localInfo.toString().getBytes());
			
			/********************/
			/* 测试专用，非json格式时 */
//			dos.write(("deviceNum:"+deviceNum+"|command:"+command).getBytes());
			/********************/
			
			/****************/
			/* 测试专用，显示信息 */
//			System.out.println(deviceNum + ":" + command);
			/****************/
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}

/**
 * 本类处理发送到远程设备的消息
 * @param deviceNum: 要发送的设备编号，通过此设备编号寻找发往对应连接的socket
 * @param command: 发往设备的命令
 * @return
 */
class OutputdeviceInfoProcess implements Runnable
{
	public Socket device;
	public Integer deviceNum;
	public String command;
	public JSONObject deviceInfo;
	
	public OutputdeviceInfoProcess(Socket device, JSONObject deviceInfo) {
		// TODO Auto-generated constructor stub
		if (deviceInfo == null) {
			return;
		}
		
		this.device = device;
		this.deviceInfo = deviceInfo;
		try 
		{
			this.deviceNum = deviceInfo.getInt("deviceNum");
			this.command = deviceInfo.getString("command");
		} 
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		/* 此处先检测玩具是否上线 */
		if( (!(ServerOutput.clientList.containsKey(deviceNum))) || (!(ServerOutput.clientList.get(deviceNum).isConnected())))
		{
			System.out.println("device "+deviceNum+" offline");
			return;
		}
		/* 这里使用发送紧急字节来检测远端是否上线 */
		try {
			ServerOutput.clientList.get(deviceNum).sendUrgentData(0XFF);
		} catch (IOException e) {
			// TODO: handle exception
			try {
				Thread.sleep(3000);
				ServerOutput.clientList.get(deviceNum).sendUrgentData(0XFF);
			} catch (IOException e1) {
				// TODO Auto-generated catch block				
				e1.printStackTrace();
				System.out.println("device "+deviceNum+" offline");
				return;
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();				
			}
			
		}
		
		/* 若玩具已上线 */
		Socket client = ServerOutput.clientList.get(deviceNum);
		try 
		{
			DataInputStream dis = new DataInputStream(client.getInputStream());
			DataOutputStream dos = new DataOutputStream(client.getOutputStream());
			
			Thread.sleep(5000);
			
//			JSONObject jsonInfo = new JSONObject();
//			jsonInfo.put("id", deviceNum);
//			jsonInfo.put("command", command);
//			
//			System.out.println(jsonInfo.toString());
			
			/* 检测玩具是否空闲 */
			JSONObject check = new JSONObject();
			JSONObject temp;
			byte[] buffer = new byte[1024];
			check.put("id", deviceNum);
			check.put("command", "0xff");
			dos.write(check.toString().getBytes());
			dos.flush();
			while (dis.read(buffer) != -1) {
				temp = new JSONObject(new String(buffer));
				System.out.println("---------------  test state  ----------");
				System.out.println(new String(buffer));
				System.out.println("---------------  test state end ----------");
				if(temp.get("command").equals("0x23")){
					Thread.sleep(3000);					
					dos.write(check.toString().getBytes());
					dos.flush();
					continue;
				}
				if(temp.get("command").equals("0x21"))
				{
					break;
				}
			}
			
			/* 此处根据相应的command，进行相应处理 */
			if(command.equals("0x01") || command.equals("0x03") ||
				command.equals("0x11") || command.equals("0x05") || 
				command.equals("0x07") || command.equals("0x09") || 
				command.equals("0x0b") || command.equals("0x13") || 
				command.equals("0x2d") || command.equals("0x0d") || 
				command.equals("0x0f") || command.equals("0x85") ||
				command.equals("0x87") || command.equals("0x89") ||
				command.equals("0x8b"))
			{
				dos.write(deviceInfo.toString().getBytes());
				client.shutdownOutput();
				client.close();
			}
			
			/* 0x2f: 文字转语音 */
			if(command.equals("0x2f"))
			{
				String content = URLDecoder.decode(deviceInfo.getString("content"), "utf-8");
				System.out.println(content);
				deviceInfo.remove("content");
				deviceInfo.put("content", content);
				System.out.println(deviceInfo.toString());
				dos.write(deviceInfo.toString().getBytes());
				client.shutdownOutput();
				client.close();
			}
			
			/* 0x1f: 语音留言 */
			if(command.equals("0x1f"))
			{
				String filepath = deviceInfo.getString("filename");
				String filename = filepath.split("/")[filepath.split("/").length -1];
				deviceInfo.remove("filename");
				deviceInfo.put("filename", filename);
				dos.write(deviceInfo.toString().getBytes());
				dis.read();
				
				FileInputStream voiceFile = new FileInputStream(new File(filepath));
				buffer = new byte[1024];
				int num = voiceFile.read(buffer);
				while(num != -1)
				{
					dos.write(buffer, 0, num);
					dos.flush();
					buffer = new byte[1024];
					num = voiceFile.read(buffer);
				}
				Thread.sleep(1000);
				client.shutdownOutput();
				client.close();
			}
			
			/* 0x19: 下载儿歌 */
			if(command.equals("0x19"))
			{
				String filepath = deviceInfo.getString("filename");
				filepath = URLDecoder.decode(filepath, "utf-8");
				String filename = filepath.split("/")[filepath.split("/").length -1];
				System.out.println(filepath);
				System.out.println(filename);
				deviceInfo.remove("filename");
				deviceInfo.put("filename", filename);
				dos.write(deviceInfo.toString().getBytes());
				dis.read();
				
				FileInputStream songFile = new FileInputStream(new File(filepath));
				buffer = new byte[1024];
				int num = songFile.read(buffer);
				while(num != -1)
				{
					dos.write(buffer, 0, num);
					dos.flush();
					buffer = new byte[1024];
					num = songFile.read(buffer);
				}
				Thread.sleep(5000);
				client.shutdownOutput();
				client.close();
				System.out.println("------------- send over -------------");
			}
			
			/* 0x1d: 下载广播列表 */
			if(command.equals("0x1d"))
			{
				String filepath = deviceInfo.getString("filename");
				filepath = URLDecoder.decode(filepath, "utf-8");
				String filename = filepath.split("/")[filepath.split("/").length -1];
				System.out.println(filepath);
				System.out.println(filename);
				deviceInfo.remove("filename");
				deviceInfo.put("filename", filename);
				dos.write(deviceInfo.toString().getBytes());
				dis.read();
				
				FileInputStream songFile = new FileInputStream(new File(filepath));
				buffer = new byte[1024];
				int num = songFile.read(buffer);
				while(num != -1)
				{
					dos.write(buffer, 0, num);
					dos.flush();
					buffer = new byte[1024];
					num = songFile.read(buffer);
				}
				Thread.sleep(2000);
				client.shutdownOutput();
				client.close();
				System.out.println("------------- send over -------------");
			}
			
			/* 0x31: 删除歌曲 */
			if(command.equals("0x31"))
			{
				String filename = URLDecoder.decode(deviceInfo.getString("filename"), "utf-8");
				System.out.println(filename);
				deviceInfo.remove("filename");
				deviceInfo.put("filename", filename);
				System.out.println(deviceInfo.toString());
				dos.write(deviceInfo.toString().getBytes());
				client.shutdownOutput();
				client.close();
			}
			
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} 
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

/**
 * 此类是主要用于socket输入流连接
 * 此链接不需要长连接，只有当客户端有需要传输信息时才连接
 */
class ServerInput implements Runnable
{

    public static final int PORT = 8001;//监听的端口号  

//    public static HashMap<Integer, Socket> clientList = new HashMap<>();
	
	public void run() {
		// TODO Auto-generated method stub
		ServerSocket serverSocket = null;//定义一个ServerSocket  
        try   
        {    
            serverSocket = new ServerSocket(PORT);//新建一个ServerSocket对象    
            while (true)   
            {   
            	//不断等待客户端连接   
                Socket client = serverSocket.accept();
                  
                //客户端连接后，首先读取客户端发来的客户号
                try 
                {
                	 DataInputStream dis = new DataInputStream(client.getInputStream());
                	 DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                	 byte[] buffer = new byte[1024];
                	 dis.read(buffer);
                	 System.out.println(new String(buffer));
                	 JSONObject info = new JSONObject(new String(buffer, "utf-8").trim());
                	 Integer clientNum = Integer.parseInt(info.getString("id"));
//                	 ServerInput.clientList.put(clientNum, client);
                	 System.out.println("Input: 客户端 " + clientNum + " 连接成功");
                	 dos.write("ok".getBytes());
                	 
                	 /* 0 是本机PHP程序的客户号，此处进行相应的函数处理 */
                	 if(clientNum == 0)
                	 {
                     	 new Thread(new InputLocalInfoProcess(client)).start();
                	 }
                	 
                	 /* 终端发送的消息，此处进行相应的函数处理 */
                	 else 
                	 {
                		 new Thread(new InputDeviceInfoProcess(client, clientNum)).start();
                	 }
				} 
                catch (Exception e) 
                {
					// TODO: handle exception
                	e.printStackTrace();
				} 
            }    
        }   
        catch (Exception e)   
        {    
            e.printStackTrace();    
        }   
        finally  
        {  
            try   
            {  
                if(serverSocket != null)  
                {  
                    serverSocket.close();  
                }  
            }   
            catch (Exception e) 
            {  
                e.printStackTrace();  
            }  
        }
	}
	
}

/**
 * 本类处理 本地 PHP端发送过来的消息，并通过输出流方法通知远程设备进行处理
 * @param deviceNum: 本地的PHP端连接
 * @param command: 发送到PHP端的命令
 * @return
 */
class InputLocalInfoProcess implements Runnable
{
	private Socket local;
	
	public InputLocalInfoProcess(Socket local) {
		// TODO Auto-generated constructor stub
		this.local = local;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try 
		{
			DataInputStream dis = new DataInputStream(local.getInputStream());
			DataOutputStream dos = new DataOutputStream(local.getOutputStream());
			byte[] buffer = new byte[1024];
			
			// 接受本地端口的指令
			dis.read(buffer);
			String info = (new String(buffer, "utf-8")).trim();
			System.out.println(info);
			JSONObject jsonInfo = new JSONObject(info); // 本地PHP发送过来的指令			
			Integer deviceNum = new Integer(jsonInfo.getString("deviceNum"));

			JSONObject outputDeviceInfo = new JSONObject();	// 发送到 OutputdeviceInfoProcess 的指令		

			outputDeviceInfo = process(jsonInfo);
			
			/********************/
			/* 测试专用，非JSON格式时 */
//			Integer deviceNum = Integer.parseInt((info.split(":")[0]));
//			String command = info.split(":")[1];
//			System.out.println(info);
			/********************/
			
			/* 使用输出流通知相应设备进行相应处理  */
//			new Thread(new OutputdeviceInfoProcess(ServerOutput.clientList.get(deviceNum), outputDeviceInfo)).start();
			
			/**************************************/
			if((!CommandQueue.commandQueue.containsKey(deviceNum)) || (CommandQueue.commandQueue.get(deviceNum)==null))
			{
				System.out.println("!commandQueue");
				CommandQueue.commandQueue.put(deviceNum, new LinkedList<JSONObject>());
			}
			if ( (CommandQueue.commandQueue.get(deviceNum).isEmpty()) && 
					(CommandQueue.processFlag.get(deviceNum) == false) ) {
				System.out.println("new commandQueue");
				CommandQueue.commandQueue.get(deviceNum).add(outputDeviceInfo);
				new Thread(new CommandQueue(deviceNum)).start();
			}
			
			else {
				System.out.println("add commandQueue");
				CommandQueue.commandQueue.get(deviceNum).add(outputDeviceInfo);
			}
			/**************************************/
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public JSONObject process(JSONObject jsonInfo)
	{
		JSONObject processJSONInfo = new JSONObject();
		try 
		{
			Integer deviceNum = jsonInfo.getInt("deviceNum");
			String command = jsonInfo.getString("command");
			
			if(command.equals("story"))
			{
				command = "0x05";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("song"))
			{
				command = "0x07";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("music"))
			{
				command = "0x09";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("broadcast"))
			{
				command = "0x0b";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
				processJSONInfo.put("radio_num", jsonInfo.getString("radioNum"));
			}
			if(command.equals("take_photo"))
			{
				command = "0x11";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("leave_message"))
			{
				command = "0x1f";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
				processJSONInfo.put("filename", jsonInfo.getString("filename"));
			}
			if(command.equals("video"))
			{
				command = "0x13";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("set_volume_4"))
			{
				command = "0x2d";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("volume_up"))
			{
				command = "0x0d";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("volume_down"))
			{
				command = "0x0f";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("content"))
			{
				command = "0x2f";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
				processJSONInfo.put("content", jsonInfo.getString("content"));
			}
			if(command.equals("download_song"))
			{
				command = "0x19";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
				processJSONInfo.put("filename", jsonInfo.getString("filename"));
			}
			if(command.equals("download_radiolist"))
			{
				command = "0x1d";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
				processJSONInfo.put("filename", jsonInfo.getString("filename"));
			}
			if(command.equals("delete_song"))
			{
				command = "0x31";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
				processJSONInfo.put("filename", jsonInfo.getString("filename"));
			}
			if(command.equals("previous"))
			{
				command = "0x01";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("next"))
			{
				command = "0x03";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("pause_story"))
			{
				command = "0x85";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("pause_song"))
			{
				command = "0x87";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("pause_music"))
			{
				command = "0x89";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			if(command.equals("pause_broadcast"))
			{
				command = "0x8b";
				processJSONInfo.put("deviceNum", deviceNum);
				processJSONInfo.put("command", command);
			}
			
		} 
		catch (JSONException e) 
		{
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return processJSONInfo;
	}
}


/**
 * 本类处理远程设备发送过来的消息，并通过输出流方法通知PHP端进行处理
 * @param device: 发送过来的设备的socket连接
 * @param deviceNum: 发往设备的编号
 */
class InputDeviceInfoProcess implements Runnable
{
	private Socket device;
	private Integer deviceNum;
	private static final String preFileName = "/var/www/html/ljc/resource/device/";
	private JSONObject localInfo;
	
	public InputDeviceInfoProcess(Socket device, Integer deviceNum) {
		// TODO Auto-generated constructor stub
		this.device = device;
		this.deviceNum = deviceNum;
		localInfo = new JSONObject();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String command = "0x00";
		try 
		{
			/*****************/
			/* 测试专用，键盘输入流 */
//			DataInputStream dis = new DataInputStream(System.in);
			/****************/
			DataInputStream dis = new DataInputStream(device.getInputStream());
			DataOutputStream dos = new DataOutputStream(device.getOutputStream());
			byte[] buffer = new byte[1024];
			dis.read(buffer);
			System.out.println("3");
			String info = new String(buffer, "utf-8").trim();
			buffer = new byte[1024];
			JSONObject jsonInfo = new JSONObject(info);
			deviceNum = Integer.parseInt(jsonInfo.getString("id"));
			command = jsonInfo.getString("command");
			/********************/
			/* 测试专用，非JSON格式时 */
//			deviceNum = Integer.parseInt(info.split(":")[0]);
//			String command = info.split(":")[1];
			/********************/
			dos.write("ok".getBytes());
			System.out.println("4");
			
			/* 此处根据相应的command，进行相应处理 */
//			JSONObject localInfo = new JSONObject();
			localInfo.put("deviceNum", deviceNum);
			localInfo.put("command", command);
			
			process(jsonInfo);
			
			/* 处理完后，使用输出流通知本地进行相应处理 */
			/****************/
			/* 测试专用，显示信息 */
//			System.out.println(jsonInfo.toString());
			/****************/
			
//			new Thread(new OutputlocalInfoProcess(ServerOutput.clientList.get("0"), localInfo)).start();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			if(command.equals("0x27") || command.equals("0x2b") || command.equals("0x29"))
			{
				new Thread(new OutputlocalInfoProcess(ServerOutput.clientList.get(0), localInfo)).start();
			}
			e.printStackTrace();
		} 
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void process(JSONObject jsonInfo) throws IOException
	{
		try 
		{
			DataInputStream dis = new DataInputStream(device.getInputStream());
			DataOutputStream dos = new DataOutputStream(device.getOutputStream());
			String command = jsonInfo.getString("command");
			byte[] buffer = new byte[1024];
			
			/* 0x27:接收玩具传输过来的图片文件 */
			if(command.equals("0x27"))
			{
				File filePath = new File(preFileName + deviceNum);
				if(!(filePath.isDirectory()))
				{
					filePath.mkdir();
				}
				
				/* 解析获得文件名 */
				String filename = jsonInfo.getString("filename").trim();		
				File file = new File(filePath, filename);
				file.createNewFile();
				RandomAccessFile rf = new RandomAccessFile(file, "rw");
				buffer = new byte[1024];
				dos.write("ok".getBytes());
				System.out.println("6");
				
				localInfo.put("filename", file.getAbsolutePath());
				
				/*开始下载文件*/				
				int num = dis.read(buffer);//从此输入流中将最多 buf.length个字节的数据读入一个 buf数组中。返回：读入缓冲区的字节总数			
				while(num != -1)//如果因为已经到达文件末尾而没有更多的数据，则返回 -1
				{
					rf.write(buffer, 0, num);//将buf数组中从0 开始的num个字节写入此文件输出流
					buffer = new byte[1024];
					rf.skipBytes(num);//跳过num个字节数
					num = dis.read(buffer);//从此输入流中将最多 buf.length个字节的数据读入一个 buf数组中。返回：读入缓冲区的字节总数
				}
	//			dos.write("ok".getBytes());
	
			}
			
			/* 0x2b:接收玩具传输过来的录音文件 */
			if(command.equals("0x2b"))
			{
				File filePath = new File(preFileName + deviceNum);
				if(!(filePath.isDirectory()))
				{
					filePath.mkdir();
				}
				
				/* 解析获得文件名 */
				String filename = jsonInfo.getString("filename").trim();		
				File file = new File(filePath, filename);
				file.createNewFile();
				RandomAccessFile rf = new RandomAccessFile(file, "rw");
				buffer = new byte[1024];
				dos.write("ok".getBytes());
				System.out.println("6");
				
				localInfo.put("filename", file.getAbsolutePath());
				
				/*开始下载文件*/				
				int num = dis.read(buffer);//从此输入流中将最多 buf.length个字节的数据读入一个 buf数组中。返回：读入缓冲区的字节总数			
				while(num != -1)//如果因为已经到达文件末尾而没有更多的数据，则返回 -1
				{
					rf.write(buffer, 0, num);//将buf数组中从0 开始的num个字节写入此文件输出流
					buffer = new byte[1024];
					rf.skipBytes(num);//跳过num个字节数
					num = dis.read(buffer);//从此输入流中将最多 buf.length个字节的数据读入一个 buf数组中。返回：读入缓冲区的字节总数
				}
	//			dos.write("ok".getBytes());
			}
			
			/* 0x29:接收玩具传输过来的视频文件 */
			if(command.equals("0x29"))
			{
				File filePath = new File(preFileName + deviceNum);
				if(!(filePath.isDirectory()))
				{
					filePath.mkdir();
				}
				
				/* 解析获得文件名 */
				String filename = jsonInfo.getString("filename").trim();		
				File file = new File(filePath, filename);
				file.createNewFile();
				RandomAccessFile rf = new RandomAccessFile(file, "rw");
				buffer = new byte[1024];
				dos.write("ok".getBytes());
				System.out.println("6");
				
				localInfo.put("filename", file.getAbsolutePath());
				
				/*开始下载文件*/				
				int num = dis.read(buffer);//从此输入流中将最多 buf.length个字节的数据读入一个 buf数组中。返回：读入缓冲区的字节总数			
				while(num != -1)//如果因为已经到达文件末尾而没有更多的数据，则返回 -1
				{
					rf.write(buffer, 0, num);//将buf数组中从0 开始的num个字节写入此文件输出流
					buffer = new byte[1024];
					rf.skipBytes(num);//跳过num个字节数
					num = dis.read(buffer);//从此输入流中将最多 buf.length个字节的数据读入一个 buf数组中。返回：读入缓冲区的字节总数
				}
	//			dos.write("ok".getBytes());
			}
			
			/* 0x15:接收玩具传输过来的歌曲列表文件 */
			if(command.equals("0x15"))
			{
				File songlist = new File(preFileName + deviceNum + "/songlist.txt");
				if(songlist.exists())
				{
					songlist.delete();
				}
				songlist.createNewFile();
				
				System.out.println(songlist.getAbsolutePath());
				RandomAccessFile rf = new RandomAccessFile(songlist, "rw");
				buffer = new byte[1024];
				dos.write("ok".getBytes());
				System.out.println("6");
				
				/*开始下载文件*/				
				int num = dis.read(buffer);//从此输入流中将最多 buf.length个字节的数据读入一个 buf数组中。返回：读入缓冲区的字节总数			
				while(num != -1)//如果因为已经到达文件末尾而没有更多的数据，则返回 -1
				{
					rf.write(buffer, 0, num);//将buf数组中从0 开始的num个字节写入此文件输出流
					buffer = new byte[1024];
					rf.skipBytes(num);//跳过num个字节数
					num = dis.read(buffer);//从此输入流中将最多 buf.length个字节的数据读入一个 buf数组中。返回：读入缓冲区的字节总数
				}

			}
		} 
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

/**
 * 命令队列方法的实现
 * @param deviceId: 设备ID
 * @param processFlag: 设备处理标志位，当设备每次连接服务器时，自动将此标志位设为false
 * @param deviceCommandQueue: 设备命令处理队列，将对应命令存入该设备的命令队列中
 *
 */
class CommandQueue implements Runnable
{

	public Integer deviceId;
	public static HashMap<Integer, Boolean> processFlag = new HashMap<>();
	public static HashMap<Integer, LinkedList<JSONObject>> commandQueue = new HashMap<>();
	
	public CommandQueue()
	{}
	
	public CommandQueue(Integer deviceId) {
		// TODO Auto-generated constructor stub
		this.deviceId = deviceId;
	}
	
	@Override
	public void run() {
		int i = 1;
		// TODO Auto-generated method stub
		while ( (commandQueue.get(deviceId).peek() != null) || (processFlag.get(deviceId) == true) ) {
			if (processFlag.get(deviceId) == false) {
				processFlag.remove(deviceId);
				processFlag.put(deviceId, true);
				i = 1;
				System.out.println("commandQueue start");
				new Thread(new OutputdeviceInfoProcess(ServerOutput.clientList.get(deviceId), commandQueue.get(deviceId).poll())).start();
			}
			if (processFlag.get(deviceId) == true) {
				System.out.println("commandQueue wait");
				try {
					if (i <= 5)
					{
						Thread.sleep(10000);
						i++;
					}
					else {
						CommandQueue.commandQueue.remove(deviceId);
						break;
					}					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}

/**
 * 在线检测端口的监听
 * @author Administrator
 *
 */
class TestServer implements Runnable
{
	public static final int PORT = 8002;
	public static HashMap<Integer, Socket> onlineClientList = new HashMap<>();
	public ServerSocket server;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try 
		{
			server = new ServerSocket(PORT);
			JSONObject state;
			while (true) {
				Socket client = server.accept();
				client.setSoTimeout(500);
				DataInputStream dis = new DataInputStream(client.getInputStream());
				DataOutputStream dos = new DataOutputStream(client.getOutputStream());
				
				byte[] buffer = new byte[1024];
           	 	dis.read(buffer);
           	 	System.out.println("8002: "+new String(buffer).trim());
           	 	JSONObject jsonInfo = new JSONObject(new String(buffer).trim());
           	 	Integer clientNum = Integer.parseInt(jsonInfo.getString("id"));
           	 	
	           	 /* 检测clientList中是否存在此对象，若存在，则替换，否则加入 */
	           	 if(TestServer.onlineClientList.containsKey(clientNum))
	           	 {
	           		 TestServer.onlineClientList.remove(clientNum);
	           		 TestServer.onlineClientList.put(clientNum, client);
	           	 }
	           	 else
	           	 {
	           		 state = new JSONObject();
	           		 state.put("deviceNum", clientNum);
	           		 state.put("command", "0x33");
	           		 new Thread(new OutputlocalInfoProcess(ServerOutput.clientList.get(0), state)).start();
	           		 TestServer.onlineClientList.put(clientNum, client);
	           	 }

	           	 
	           	 System.out.println("Output: 客户端 " + clientNum.toString() + " 连接成功");
	           	 dos.write("ok".getBytes());
	           	 
	           	 new Thread(new TestOnline()).start();
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}

/**
 * 在线检测方法的实现
 * @param it: 对于每个连接到8002端口的socket连接的迭代器
 * @param state: 封装发送到PHP端消息的JSON对象
 *
 */
class TestOnline implements Runnable
{
	public byte[] buffer;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Iterator<Integer> it;
		JSONObject state;
		while (true) {
			try 
			{
				// 每隔3分钟，遍历 clientList , 获取断开连接的 client 端口
				Thread.sleep(1*60*1000);
				System.out.println("Test Online Start");
				if(TestServer.onlineClientList.isEmpty())
				{
					break;
				}
				it = TestServer.onlineClientList.keySet().iterator();
				while (it.hasNext()) {
					Integer deviceId = (Integer) it.next();
					/* 这里使用发送紧急字节来检测远端是否上线 */
					try {
						if(deviceId != 0){
							System.out.println("Test client: "+deviceId);
							System.out.println("SoTimeout :"+TestServer.onlineClientList.get(deviceId).getSoTimeout());
							DataOutputStream dos = new DataOutputStream(TestServer.onlineClientList.get(deviceId).getOutputStream());
							DataInputStream dis = new DataInputStream(TestServer.onlineClientList.get(deviceId).getInputStream());
							dos.write("ok".getBytes());
							dos.flush();
							buffer = new byte[10];
							dis.read(buffer);
							System.out.println(new String(buffer));
						}
					} catch (Exception e) {
						// TODO: handle exception
						try {
							// 移除列表中的连接
							System.out.println("remove client: "+deviceId);
							ServerOutput.clientList.remove(deviceId);
							if(CommandQueue.commandQueue.containsKey(deviceId))
							{
								CommandQueue.commandQueue.remove(deviceId);
							}
							if(CommandQueue.processFlag.containsKey(deviceId))
							{
								CommandQueue.processFlag.remove(deviceId);
							}
							it.remove();
							// 将断开连接的 client 端口发送至 PHP 端，将设备的状态设为“离线”
							state = new JSONObject();
							state.put("deviceNum", deviceId);
							state.put("command", "0x35");
							new Thread(new OutputlocalInfoProcess(ServerOutput.clientList.get(0), state)).start();
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}