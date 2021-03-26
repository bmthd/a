package jp.co.vsn.training.java.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

/**
 *サーバークラスにリクエストを問い合わせ帰ってきた商品情報を表示するクラスです。
 */
public class Client {
    
    Item item;

    
	/**
	 * TCP通信で引数のバイト配列を送信するメソッドです。
	 * @param バイト配列
	 * @throws IOException 
	 */ 
	public void sendByte(byte[] data) throws IOException {
		try {
			System.out.println("クライアントから送信します");
			Socket sock = new Socket("localhost",5002);
			OutputStream out = sock.getOutputStream();
			out.write(data);
			System.out.println("送信成功");
			out.close();
			sock.close();
		} catch (IOException e) {
			throw e;
		} 
	}
    
	/**
	 * TCP通信でバイトデータを戻り値として受けとるメソッドです。
	 * @return byte配列
	 */
	public byte[] receiveByte() {
	
		System.out.println("クライアントで受信します");
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
     * 引数のバイト配列をアイテムに変換して戻り値として出力するメソッドです。
     * @param byte配列
     * @return Item
     */
    public Item toItem(byte[] data) {
    	Item result = null;
    	try {
    	ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			result = (Item)ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
		} catch (IOException e) {
		}
    	return result;
    }
    
    /**
	 * メンバ変数のアイテムオブジェクトの中身を表示するメソッドです。
	 * 
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
	 * テスト用
	 * @param String
	 */
	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		Client cl = new Client();
		
		
		System.out.println("リクエストを「FIND#xxx」形式で入力");
		String req=sc.next();
		System.out.println("リクエストは"+req);
		sc.close();
		
		try {
			cl.sendByte(req.getBytes("UTF-8"));		//リクエストをバイトに変換して送信
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			System.out.println("送信に失敗しました");
			e.printStackTrace();
		}
			cl.item=cl.toItem(cl.receiveByte());
			cl.itemShow();

	}

}
