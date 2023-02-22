import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * �����ɵ�����дΪJSON��CSV�ļ�
 * @author zycheng4
 */
public class FileIO {
	/**
	 * �����ɵ�����дΪJSON�ļ�
	 * @param entityInfo �����û�����Ϣ
	 * @param path JSON�ļ���ַ
	 * @throws IOException �ļ����쳣���ļ�д���쳣
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
	 * ������д��CSV�ļ�����Ҫ���ڵ���Neo4j�������ݵĿ��ӻ���CSV�ļ�Ĭ�ϴ������Ŀ��Ŀ¼�µ�csv/�ļ����ڡ�
	 * ��ʵ�塢��ϵ�ֱ�д��6��csv�ļ��У�
	 * <p> user.csv����û�id���������ˡ��绰���롢�����˻���������Ϣ��</p>
	 * <p> phone.csv����ֻ����룻</p>
	 * <p> account.csv��������˻���</p>
	 * <p> userPhone.csv�����û������ֻ����룻</p>
	 * <p> userAccount.csv�����û����������˻���</p>
	 * <p> userGuarantor.csv���ӽӵ����뵣���ˡ�</p>
	 * <p> Neo4j�������д������£�
	 * 	bin\neo4j-admin import --ignore-duplicate-nodes=true --ignore-missing-nodes=true --nodes=import/user.csv
	 * 		--nodes=import/phone.csv --nodes=import/account.csv --relationships=import/userPhone.csv
	 * 		--relationships=import/userAccount.csv --relationships=import/userGuarantor.csv
	 * </p>
	 *
	 * @param entityInfo �����û�����Ϣ
	 * @throws IOException �ļ����쳣���ļ�д���쳣
	 */
	static void writeToCSV(HashMap<String, String[]> entityInfo) throws IOException {
		// ���ļ�
		PrintWriter pwUser = new PrintWriter(new File("csv/user.csv"), StandardCharsets.UTF_8);
		PrintWriter pwPhone = new PrintWriter(new File("csv/phone.csv"), StandardCharsets.UTF_8);
		PrintWriter pwAccount = new PrintWriter(new File("csv/account.csv"), StandardCharsets.UTF_8);
		PrintWriter pwUserPhone =
				new PrintWriter(new File("csv/userPhone.csv"), StandardCharsets.UTF_8);
		PrintWriter pwUserAccount =
				new PrintWriter(new File("csv/userAccount.csv"), StandardCharsets.UTF_8);
		PrintWriter pwUserGuarantor =
				new PrintWriter(new File("csv/userGuarantor.csv"), StandardCharsets.UTF_8);

		// д���ͷ
		String[] description = { "id:ID", "Name", "Age", "Gender", "Income", "LoanAmount", "TrustLevel",
				"LoanStatus", "LoanPurpose", ":LABEL"};
		pwUser.write(String.join(",", description)+'\n');
		pwPhone.write("PhoneNumber:ID,:LABEL\n");
		pwAccount.write("BankAccount:ID,:LABEL\n");
		for (PrintWriter pw : new PrintWriter[] {pwUserPhone, pwUserAccount, pwUserGuarantor})
			pw.write(String.join(",",	new String[] {":START_ID", ":END_ID", ":TYPE"})+'\n');

		// д������
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
		// �ر�д����
		PrintWriter[] pwLst = {pwUser, pwPhone, pwAccount, pwUserPhone, pwUserAccount, pwUserGuarantor};
		for (PrintWriter pw : pwLst)
			pw.close();
	}
}
