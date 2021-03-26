package java.base;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *送られてきたリクエストの数値に該当する商品情報を返すサーバークラスです。
 */
public class Server {
	private HashMap<Integer, Item> itemData=new HashMap();
	String filePath = "C:\\Users\\user01\\Desktop\\src\\DevJavaTraining\\res\\item.csv";
    final String delimiter = ",";
    boolean firstLine=false;
    private Item item;
    byte[] data;
    static ServerSocket svSock;
    static Socket sock;

	/**
	 * csvデータを設定します。
	 */
	public Server() {
		setData();
	}

	/**
	 * csvデータを設定します。
	 */
	public void setData() {
		try {
        	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"Shift-JIS"));
    		String line;
    		System.out.println(br.readLine());
        	while((line =br.readLine()) != null) {
        		Item item = new Item();
        		String[] token = line.split(delimiter);
        		System.out.println(token[0] + " | "+ token[1]+ " | "+ token[2]+ " | "+ token[3]+" | "+ token[4]+" | "+ token[5]+" | "+ token[6]);
        		item.setItemId(Integer.valueOf(token[0]));
        		item.setOriginalId(token[1]);
        		item.setItemname(token[2]);
	            item.setCategoryCode(token[3]);
	            item.setPrice(Integer.valueOf(token[4]));
	            item.setExplanation(token[5]);
	            item.setImageName(token[6]);
	            this.itemData.put(item.getItemId(), item);
        	}
        	br.close();
        }catch(IOException e) {
        	e.printStackTrace();
        }
	}

	/**
	 * 商品IDだけを抜きだすメソッドです。
	 * @return result 商品ID
	 * @throws IllegalArgumentException 引数の文字列が"FIND#"形式でない場合IAEをthrowします。
	 */
	public int analyze(String req) {
		int result = 0;
		Pattern pattern= Pattern.compile("FIND#");
		Matcher matcher=pattern.matcher(req);
		if (matcher.find()) {
			result = Integer.parseInt(matcher.replaceAll(""));
		}
		else {
			throw new IllegalArgumentException();
		}
		return result;
	}

	/**
	 * IDからアイテムオブジェクトを探します。
	 * @return result Itemオブジェクト 無ければnull
	 */
	public Item toItem(int itemid) {
		Item result = null;
		if (itemData.containsKey(itemid)) {
			 result = itemData.get(itemid);
		}
		this.item=result;
		return result;
	}

	/**
	 * アイテムオブジェクトの中身を表示します。
	 */
	public void itemShow() {
		if(item==null) {
			System.out.println("アイテムがありません");
		}
		else {
			System.out.println("アイテムの中身は以下です");
			System.out.println("");
			System.out.println(this.item.getItemId());
			System.out.println(this.item.getOriginalId());
			System.out.println(this.item.getItemName());
			System.out.println(this.item.getCategoryCode());
			System.out.println(this.item.getPrice());
			System.out.println(this.item.getExplanation());
			System.out.println(this.item.getImageName());
		}
	}

	/**
	 * アイテムをバイト配列に変換します。
	 * @param Item アイテムオブジェクト
	 * @return byte配列
	 */
	public byte[] toByte(Item item) {
	ByteArrayOutputStream baos= new ByteArrayOutputStream();
	try {
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(item);

		baos.close();
		oos.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	return baos.toByteArray();

	}

	/**
	 * TCP通信でバイト配列を送信します。
	 * @param data バイト配列
	 */
	public void sendByte(byte[] data) {

		try {
			System.out.println("サーバーから送信します");
			Socket sock = new Socket("localhost",5002);
			OutputStream out = sock.getOutputStream();
			out.write(data);
			System.out.println("送信成功");
			out.close();
			sock.close();
		} catch (IOException e) {
			System.out.println("送信に失敗しました");
		}
	}


	/**
	 * TCP通信でバイトデータを受け取ります。
	 * @return data byte配列
	 */
	public byte[] receiveByte() {

		System.out.println("サーバーで受信を待機します");
		byte[] data =null;
		try {
			ServerSocket svSock = new ServerSocket(5002);	//ポート番号5002番で通信を待機しデータを受信
			Socket sock = svSock.accept();
			data = new byte[1024];
			InputStream in = sock.getInputStream();
			int readSize = in.read(data);
		    data = Arrays.copyOf(data, readSize);
			System.out.println("受信しました");
			in.close();
			svSock.close();
		} catch (IOException e) {
			System.out.println("受信に失敗しました");
		}
		return data;
	}

	/**
	 * バイト配列をUTF-8でエンコードします。
	 * @param data バイト配列
	 * @return result エンコード結果の文字列
	 */
	public String encode(byte[] data) {
		String result = null;
		try {
			result = new String(data,"UTF-8");
			}catch (UnsupportedEncodingException e) {
				System.out.println("エンコードに失敗しました");
			}
		return result;
	}


	/**
	 * @param args
	 * テスト用
	 */
	public static void main(String[] args) {

		Server sv = new Server();

		System.out.println("サーバーで受信を待機します");
		byte[] data =null;
		try {
			Server.svSock = new ServerSocket(5002);	//ポート番号5002番で通信を待機しデータを受信
			Server.sock = svSock.accept();
			boolean b=false;
			while(b==true) {
				data = new byte[1024];
				InputStream in = Server.sock.getInputStream();
				int readSize = in.read(data);
			    data = Arrays.copyOf(data, readSize);
				System.out.println("受信しました");
				if(sv.encode(data)=="FIN") {
					b=true;
				}
				else {
				sv.sendByte(sv.toByte(sv.toItem(sv.analyze(sv.encode(data)))));
				}
			}
		} catch (IOException e) {
			System.out.println("受信に失敗しました");
			e.printStackTrace();
		} finally {
			try {
				Server.svSock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//			sv.toItem(sv.analyze(sv.encode(sv.receiveByte())));
//			sv.itemShow();
//			sv.data=sv.toByte(sv.item);
//
//			sv.sendByte(sv.data);

	}

}
