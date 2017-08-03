package senduo.com.blelibdemo;

public class CommonTool {
	
	public final static char[] hexArray = "0123456789ABCDEF".toCharArray();  
	
	public static String bytesToHex(byte[] bytes,int len) {  
	    char[] hexChars = new char[len * 2];  
	    for ( int j = 0; j < len; j++ ) {  
	        int v = bytes[j] & 0xFF;  
	        hexChars[j * 2] = hexArray[v >>> 4];  
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];  
	    }  
	    String str =  new String(hexChars);  
	    StringBuffer sb = new StringBuffer();
	    for(int i = 0 ; i < (str.length() / 2) ; i ++){
	    	sb.append("0x" + str.substring(i*2, (i+1)*2) + " ");
	    }
	    return sb.toString().substring(0,sb.length() - 1);
	}

	/**
	 * 将类似的 "EF EB EC EA ED E0 F0 FF F9 FE 01 10 A0 0A 0F AB ae 9e 9c 2c 30 ef ff"
	 * "30 31 32 33 34 35 36 37 38 39 61 62 63 64 65 66 ab a0 ae ef ab ab ae 9e 9c 2c 30 ef ff "
	 * 的字符串形式表示的十六进制数给转换成byte数组,数组的内容是:{0xEF, 0X3B}的类似形式
	 * 将字符串形式表示的十六进制数转换为byte数组
	 */
	public static byte[] hexStringToBytes(String hexString) {
		hexString = hexString.toUpperCase();
		String[] hexStrings = hexString.split(" ");
		byte[] bytes = new byte[hexStrings.length];
		for (int i = 0; i < hexStrings.length; i++)
		{
			char[] hexChars = hexStrings[i].toCharArray();
			bytes[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
		}
		return bytes;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

}
