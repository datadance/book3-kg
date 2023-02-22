import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 将生成的数据写为JSON或CSV文件
 * @author zycheng4
 */
public class FileIO {
	/**
	 * 将生成的数据写为JSON文件
	 * @param entityInfo 所有用户的信息
	 * @param path JSON文件地址
	 * @throws IOException 文件打开异常或文件写入异常
	 */
	static void writeToJSON(@NotNull HashMap<String, String[]> entityInfo, String path) throws IOException {
		Gson gson = new Gson();
		JsonObject jsonObject = new JsonObject();
		for (HashMap.Entry<String, String[]> entry: entityInfo.entrySet()) {
			String id = entry.getKey();
			String[] info = entry.getValue();
			JsonArray data = new JsonArray();
//			Helper.printInfo(id, info, true, true);

			for (String s : info) data.add(s);
			jsonObject.add(id, data);
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));
			gson.toJson(jsonObject, bw);
			bw.flush();
			bw.close();
		}
	}

	/**
	 * 将数据写入CSV文件，主要用于导入Neo4j进行数据的可视化。CSV文件默认存放在项目根目录下的csv/文件夹内。
	 * 按实体、关系分别写入6个csv文件中：
	 * <p> user.csv存放用户id及出担保人、电话号码、银行账户的所有信息；</p>
	 * <p> phone.csv存放手机号码；</p>
	 * <p> account.csv存放银行账户；</p>
	 * <p> userPhone.csv连接用户与其手机号码；</p>
	 * <p> userAccount.csv连接用户与其银行账户；</p>
	 * <p> userGuarantor.csv连接接到人与担保人。</p>
	 * <p> Neo4j的命令行代码如下：
	 * 	bin\neo4j-admin import --ignore-duplicate-nodes=true --ignore-missing-nodes=true --nodes=import/user.csv
	 * 		--nodes=import/phone.csv --nodes=import/account.csv --relationships=import/userPhone.csv
	 * 		--relationships=import/userAccount.csv --relationships=import/userGuarantor.csv
	 * </p>
	 *
	 * @param entityInfo 所有用户的信息
	 * @throws IOException 文件打开异常或文件写入异常
	 */
	static void writeToCSV(HashMap<String, String[]> entityInfo) throws IOException {
		// 打开文件
		PrintWriter pwUser = new PrintWriter(new File("csv/user.csv"), StandardCharsets.UTF_8);
		PrintWriter pwPhone = new PrintWriter(new File("csv/phone.csv"), StandardCharsets.UTF_8);
		PrintWriter pwAccount = new PrintWriter(new File("csv/account.csv"), StandardCharsets.UTF_8);
		PrintWriter pwUserPhone =
				new PrintWriter(new File("csv/userPhone.csv"), StandardCharsets.UTF_8);
		PrintWriter pwUserAccount =
				new PrintWriter(new File("csv/userAccount.csv"), StandardCharsets.UTF_8);
		PrintWriter pwUserGuarantor =
				new PrintWriter(new File("csv/userGuarantor.csv"), StandardCharsets.UTF_8);

		// 写入表头
		String[] description = { "id:ID", "Name", "Age", "Gender", "Income", "LoanAmount", "TrustLevel",
				"LoanStatus", "LoanPurpose", ":LABEL"};
		pwUser.write(String.join(",", description)+'\n');
		pwPhone.write("PhoneNumber:ID,:LABEL\n");
		pwAccount.write("BankAccount:ID,:LABEL\n");
		for (PrintWriter pw : new PrintWriter[] {pwUserPhone, pwUserAccount, pwUserGuarantor})
			pw.write(String.join(",",	new String[] {":START_ID", ":END_ID", ":TYPE"})+'\n');

		// 写入数据
		for (HashMap.Entry<String, String[]> entry : entityInfo.entrySet()) {
			String id = entry.getKey();
			String[] info = Helper.printInfo(id, entry.getValue(), false, false);
			ArrayList<String> idInfo = new ArrayList<>();
			idInfo.add(id);
			for (int i = 0; i < info.length-3; i++) if (i < 5 || i > 6) idInfo.add(info[i]);
			pwUser.write(String.join(",", idInfo) + ",user\n");
			pwPhone.write(info[5] + ",phone\n");
			pwAccount.write(info[6] + ",bank\n");
			pwUserPhone.write(String.join(",", new String[] {id, info[5], "PhoneNumber"})+'\n');
			pwUserAccount.write(String.join(",", new String[] {id, info[6], "BankAccount"})+'\n');
			pwUserGuarantor.write(String.join(",",
					new String[] {id, info[info.length-2], "Guarantor"})+'\n');
		}
		// 关闭写入器
		PrintWriter[] pwLst = {pwUser, pwPhone, pwAccount, pwUserPhone, pwUserAccount, pwUserGuarantor};
		for (PrintWriter pw : pwLst)
			pw.close();
	}
}
