package com.android.Common;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import com.android.Base.ImagePHash;
import com.google.gson.Gson;

public class Tools {
	private static Process p = null;

	public static String ExecCmd(String cmd) {
		return ExecCmd(cmd, true);
	}

	/**
	 * true 这是最后摧毁进程，false则不摧毁
	 */
	public static String ExecCmd(String cmd, boolean destory) {
		String msg = ExecCmdV(cmd);
		if (destory) {
			Destory();
		}

		return msg;
	}

	/**
	 * 执行命令行
	 */
	@SuppressWarnings("finally")
	public static String ExecCmdV(String cmd) {
		StringBuilder tv_result = new StringBuilder("");
		try {
			p = Runtime.getRuntime().exec(cmd);
			InputStream is = p.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				tv_result.append(line + "\n");
			}
			p.waitFor();
			is.close();
			reader.close();
		} catch (IOException e) {
			p = null;
			tv_result = new StringBuilder("");
		} catch (InterruptedException e) {
			p = null;
			tv_result = new StringBuilder("");
		} finally {
			Wait(200);
			return tv_result.toString().trim();
		}
	}

	/**
	 * 终结进程
	 */
	public static void Destory(Process p2) {
		p2.destroy();
	}

	public static void Destory() {
		Destory(p);
	}

	public static Process loadProcess() {
		return p;
	}

	/**
	 * 获取 文件路径
	 */
	public static String LoadPath(String[] strs) {
		return LoadPath(strs, false);
	}

	public static String LoadPath(String[] strs, boolean create) {
		File classpathRoot = new File(System.getProperty("user.dir"));
		File tmp = classpathRoot;
		for (int i = 0; i < strs.length; i++) {
			tmp = new File(tmp, strs[i]);
			String str = strs[i];
			if (create && !str.contains(".") && !str.endsWith("png") && !tmp.exists()) {
				tmp.mkdir();
			}
		}
		return tmp.getAbsolutePath();
	}

	/**
	 * 等待
	 */
	public static void Wait(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * http get 请求 返回的 json 转化为一个对象 T
	 */
	public static <T> T HGet(String url, Class<T> type) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			result = "{}";
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
		Gson gson = new Gson();
		return gson.fromJson(result, type);
	}

	/**
	 * 下载文件
	 */
	public static void downLoadFromUrl(String urlStr, String fileName, String savePath) throws Exception {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(3 * 1000);
		conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		InputStream inputStream = conn.getInputStream();
		byte[] getData = readInputStream(inputStream);
		File saveDir = new File(savePath);
		if (!saveDir.exists()) {
			saveDir.mkdir();
		}
		File file = new File(saveDir + File.separator + fileName);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(getData);
		if (fos != null) {
			fos.close();
		}
		if (inputStream != null) {
			inputStream.close();
		}
	}

	public static byte[] readInputStream(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;
		int len_all = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
			len_all += len;
//			System.out.println("len --> " + len_all);
		}
		bos.close();
		return bos.toByteArray();
	}

	/**
	 * 校验图片的相识度
	 * 
	 * @throws Exception
	 * @throws FileNotFoundException
	 */

	public static boolean isSimilar(String addr1, String addr2) throws Exception {
		ImagePHash p = new ImagePHash();
		String image1;
		String image2;
		image1 = p.getHash(new FileInputStream(new File(addr1)));
		image2 = p.getHash(new FileInputStream(new File(addr2)));
		if (p.distance(image1, image2) > 10) {
			return false;
		}
		return true;
	}
	
	public static boolean isInSimilar(String addr1, String[] addrs) throws Exception{
		for (int i = 0;i<addrs.length;i++){
			if (isSimilar(addr1,addrs[i])){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 拷贝
	 */
	public static void copyDirAndFile(String[] strs,String director){
		if (director == ""){
			return ;
		}
		String[] tmp = new String[strs.length+1];
		for (int i=0;i<strs.length;i++){
			tmp[i] = strs[i];
		}
		tmp[tmp.length-1] = director;
		String addr = LoadPath(tmp, false);
		File f_from  = new File(addr);
		if (f_from.exists() == false || f_from.isDirectory() == false){
			return ;
		}
		
		String director2 = Util.NowFormate(director);
		tmp[tmp.length-1] = director2;
		String addr2 = LoadPath(tmp, true);
		
		copy(f_from.getPath(),new File(addr2).getPath());
	}
	
	
	/**
	 * 拷贝
	 */
	 private static void copyFile(String src, String des) {  
	      
	        BufferedReader br=null;  
	        PrintStream ps=null;  
	          
	        try {  
	            br=new BufferedReader(new InputStreamReader(new FileInputStream(src)));  
	            ps=new PrintStream(new FileOutputStream(des));  
	            String s=null;  
	            while((s=br.readLine())!=null){  
	                ps.println(s);  
	                ps.flush();  
	            }  
	              
	        } catch (FileNotFoundException e) {  
	            // TODO Auto-generated catch block  
	            e.printStackTrace();  
	        } catch (IOException e) {  
	            // TODO Auto-generated catch block  
	            e.printStackTrace();  
	        }finally{  
	                try {  
	                    if(br!=null)  br.close();  
	                    if(ps!=null)  ps.close();  
	                } catch (IOException e) {  
	                    // TODO Auto-generated catch block  
	                    e.printStackTrace();  
	                }  
	                  
	        }  
	            
	    }  
	  
	 
	 /**
	  * 拷贝
	  */
	 private static void copy(String src, String des) {  
	        File file1=new File(src);  
	        File[] fs=file1.listFiles();  
	        File file2=new File(des);  
	        if(!file2.exists()){  
	            file2.mkdirs();  
	        }  
	        for (File f : fs) {  
	            if(f.isFile()){  
	            	copyFile(f.getPath(),des+File.separator+f.getName());  
	            }else if(f.isDirectory()){  
	            	copy(f.getPath(),des+File.separator+f.getName());  
	            }  
	        }  
	          
	    } 
	 
	 private static ArrayList<Double> searchLocationFromImage(String from,String to,double rate) throws IOException{
		 BufferedImage fromImage = getBfImageFromPath(makeImageColorToBlackWhite(from));
		 BufferedImage keyImage = getBfImageFromPath(makeImageColorToBlackWhite(to));
		 int[][] fromImageRGBData = getImageGRB(fromImage);
		 int[][] keyImageRGBData = getImageGRB(keyImage);
		 int fromImgWidth = fromImage.getWidth();
		 int fromImgHeight = fromImage.getHeight();
		 int keyImgWidth = keyImage.getWidth();
		 int keyImgHeight = keyImage.getHeight();
		 ArrayList<Double> ar = new ArrayList<Double>();
		 
		 for (int y = 0; y < fromImgHeight - keyImgHeight; y ++) {
				for (int x = 0; x < fromImgWidth - keyImgWidth; x++) {
					// 根据目标图的尺寸，得到目标图四个角映射到屏幕截图上的四个点，
					// 判断截图上对应的四个点与图B的四个角像素点的值是否相同，
					// 如果相同就将屏幕截图上映射范围内的所有的点与目标图的所有的点进行比较。
					if ((keyImageRGBData[0][0] ^ fromImageRGBData[y][x]) == 0
							&& (keyImageRGBData[0][keyImgWidth - 1] ^ fromImageRGBData[y][x + keyImgWidth - 1]) == 0
							&& (keyImageRGBData[keyImgHeight - 1][keyImgWidth - 1] ^ fromImageRGBData[y + keyImgHeight - 1][x + keyImgWidth - 1]) == 0
							&& (keyImageRGBData[keyImgHeight - 1][0] ^ fromImageRGBData[y + keyImgHeight - 1][x]) == 0) {
						boolean isFinded = isMatchAll(y, x,fromImgHeight,fromImgWidth,keyImgWidth,keyImgHeight,fromImageRGBData,keyImageRGBData,rate);
						if (isFinded) {
							ar.add(((double)x)/((double)fromImgWidth));
							ar.add(((double)y)/((double)fromImgHeight));
							ar.add(((double)keyImgWidth)/((double)fromImgWidth));
							ar.add(((double)keyImgHeight)/((double)fromImgHeight));
							 return ar;
						}
						
						
						
						
					}
					
				}
			}
		 return ar;
	 }
	 
	 public static ArrayList<Double> searchLocationFromImage(String from,String[] tos,double rate) throws Exception{
		 for (int i= 0;i< tos.length;i++){
			 ArrayList<Double> tmp = searchLocationFromImage( from, tos[i],rate);
			 if (tmp.size() == 4){
				 return tmp;
			 }
		 }
		 return new ArrayList<Double>();
	 }
	 
	 
	 private static boolean isMatchAll(int y, int x,int fromImgHeight,int fromImgWidth, int keyImgWidth,int keyImgHeight,
			 int[][] fromImageRGBData,int[][] keyImageRGBData,double rate) {
			int biggerY = 0;
			int biggerX = 0;
			int count  = 0;
			int total = (int)(keyImgWidth*keyImgHeight * (1- rate));
			for (int smallerY = 0; smallerY < keyImgHeight; smallerY++) {
				biggerY = y + smallerY;
				for (int smallerX = 0; smallerX < keyImgWidth; smallerX++) {
					biggerX = x + smallerX;
					if (biggerY >= fromImgHeight || biggerX >= fromImgWidth) {
						return false;
					}
					int xor = keyImageRGBData[smallerY][smallerX] ^ fromImageRGBData[biggerY][biggerX];
					if (xor != 0) {
						count ++ ;
					}
					if (count > total){
						return false;
					}
				}
				biggerX = x;
			}
			return true;
		}
	
	 
	 private static BufferedImage getBfImageFromPath(String path) throws IOException {
			return ImageIO.read(new File(path));
	 }
	 
	 private static int[][] getImageGRB(BufferedImage bfImage) {
			int width = bfImage.getWidth();
			int height = bfImage.getHeight();
			int[][] result = new int[height][width];
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					// 使用getRGB(w,h)获取该点的颜色值是ARGB，而在实际应用中使用的是RGB，
					//所以需要将ARGB转化成RGB，即bufImg.getRGB(w,h) & 0xFFFFFF。
					result[h][w] = bfImage.getRGB(w, h) & 0xFFFFFF;
				}
			}
			return result;
		}
	 
	 @SuppressWarnings("finally")
	public static String makeImageColorToBlackWhite(String imagePath) throws IOException {
		 	String newAddr = imagePath.replace(".png", "suffix.png");
			int[][] result = getImageGRB(getBfImageFromPath(imagePath));
			int[] rgb = new int[3];
			BufferedImage bi = new BufferedImage(result[0].length, result.length, BufferedImage.TYPE_INT_RGB);
			for (int i = 0; i < result.length; i++) {
				for (int j = 0; j < result[i].length; j++) {
					rgb[0] = (result[i][j] & 0xff0000) >> 16;
					rgb[1] = (result[i][j] & 0xff00) >> 8;
					rgb[2] = (result[i][j] & 0xff);
					int color = (int) (rgb[0] * 0.3 + rgb[1] * 0.59 + rgb[2] * 0.11);
					bi.setRGB(j, i, (color << 16) | (color << 8) | color);
				}
			}
			try {
				ImageIO.write(bi, "png", new File(newAddr));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				return newAddr;
			}
		}
}